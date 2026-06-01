package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.InvestmentDTO;
import com.donato.fin_control_backend.adapters.api.http.handler.ResourceNotFoundException;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.ports.outbound.*;
import com.donato.fin_control_backend.infrastructure.entities.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finControl/investments")
public class InvestmentController {

    private final InvestmentRepository investmentRepository;
    private final InvestmentMovementRepository movementRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<InvestmentDTO> create(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));

        AccountEntity account = null;
        if (body.get("accountId") != null) {
            Long accId = ((Number) body.get("accountId")).longValue();
            account = accountRepository.findByIdAndUserId(accId, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        }

        InvestmentEntity inv = InvestmentEntity.builder()
                .user(user).account(account)
                .name((String) body.get("name"))
                .type((String) body.get("type"))
                .createdAt(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(investmentRepository.save(inv)));
    }

    @GetMapping
    public ResponseEntity<List<InvestmentDTO>> list(@RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        return ResponseEntity.ok(
                investmentRepository.findAllByUserId(user.getId()).stream().map(this::toDTO).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentDTO> get(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        InvestmentEntity inv = investmentRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
        List<InvestmentMovementEntity> movements = movementRepository
                .findAllByInvestmentIdOrderByDateDesc(inv.getId());
        return ResponseEntity.ok(toDTO(inv, movements));
    }

    @PostMapping("/{id}/movements")
    public ResponseEntity<InvestmentDTO> addMovement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        InvestmentEntity inv = investmentRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));

        String movType = (String) body.get("movementType");
        if (!List.of("CONTRIBUTION", "WITHDRAWAL", "YIELD").contains(movType)) {
            throw new IllegalArgumentException("movementType must be CONTRIBUTION, WITHDRAWAL or YIELD");
        }

        InvestmentMovementEntity movement = InvestmentMovementEntity.builder()
                .investment(inv)
                .date(LocalDate.parse((String) body.get("date")))
                .movementType(movType)
                .amount(new BigDecimal(body.get("amount").toString()))
                .createdAt(LocalDateTime.now())
                .build();
        movementRepository.save(movement);

        List<InvestmentMovementEntity> movements = movementRepository
                .findAllByInvestmentIdOrderByDateDesc(inv.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(inv, movements));
    }

    @DeleteMapping("/movements/{movementId}")
    public ResponseEntity<Void> deleteMovement(
            @PathVariable Long movementId,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        InvestmentMovementEntity movement = movementRepository
                .findByIdAndInvestmentUserId(movementId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Movement not found"));
        movementRepository.delete(movement);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/consolidated")
    public ResponseEntity<List<Map<String, Object>>> consolidated(
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        List<Object[]> rows = investmentRepository.consolidatedByType(user.getId());
        List<Map<String, Object>> result = rows.stream()
                .map(r -> Map.<String, Object>of(
                        "type", r[0] != null ? r[0] : "Outros",
                        "total", r[1] != null ? r[1] : BigDecimal.ZERO))
                .toList();
        return ResponseEntity.ok(result);
    }

    private InvestmentDTO toDTO(InvestmentEntity inv) {
        return toDTO(inv, List.of());
    }

    private InvestmentDTO toDTO(InvestmentEntity inv, List<InvestmentMovementEntity> movements) {
        BigDecimal balance = investmentRepository.calculateBalance(inv.getId());
        List<InvestmentDTO.MovementDTO> movDTOs = movements.stream()
                .map(m -> InvestmentDTO.MovementDTO.builder()
                        .id(m.getId()).date(m.getDate())
                        .movementType(m.getMovementType()).amount(m.getAmount())
                        .createdAt(m.getCreatedAt()).build())
                .toList();
        return InvestmentDTO.builder()
                .id(inv.getId()).name(inv.getName()).type(inv.getType())
                .accountId(inv.getAccount() != null ? inv.getAccount().getId() : null)
                .accountName(inv.getAccount() != null ? inv.getAccount().getName() : null)
                .balance(balance).movements(movDTOs).createdAt(inv.getCreatedAt())
                .build();
    }

    private String extractToken(String auth) {
        return auth.startsWith("Bearer ") ? auth.substring(7) : auth.trim();
    }

    private UserEntity resolve(User user) {
        if (user.getId() != null) return userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
