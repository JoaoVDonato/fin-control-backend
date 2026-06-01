package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.TagDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.TransactionDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.CreateTransactionDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.UpdateTransactionDTO;
import com.donato.fin_control_backend.core.domain.Transaction;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.TransactionService;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.usecases.commands.CreateTransactionCommand;
import com.donato.fin_control_backend.core.usecases.commands.UpdateTransactionCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finControl/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<List<TransactionDTO>> create(
            @RequestBody @Valid CreateTransactionDTO dto,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        List<TransactionDTO> result = transactionService
                .createTransaction(CreateTransactionCommand.from(dto), user)
                .stream().map(this::toDTO).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionDTO>> list(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                transactionService.listTransactions(user, accountId, categoryId, type,
                        status, dateFrom, dateTo, search, pageable)
                        .map(this::toDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> get(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        return ResponseEntity.ok(toDTO(transactionService.getTransaction(id, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTransactionDTO dto,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        return ResponseEntity.ok(toDTO(transactionService.updateTransaction(id, UpdateTransactionCommand.from(dto), user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        transactionService.deleteTransaction(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TransactionDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String auth) {
        String status = body.get("status");
        if (status == null || status.isBlank()) throw new IllegalArgumentException("status is required");
        User user = userService.findUserByToken(extractToken(auth));
        return ResponseEntity.ok(toDTO(transactionService.updateStatus(id, status, user)));
    }

    private String extractToken(String auth) {
        return auth.startsWith("Bearer ") ? auth.substring(7) : auth.trim();
    }

    private TransactionDTO toDTO(Transaction t) {
        return TransactionDTO.builder()
                .id(t.getId())
                .accountId(t.getAccount() != null ? t.getAccount().getId() : null)
                .accountName(t.getAccount() != null ? t.getAccount().getName() : null)
                .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                .categoryName(t.getCategory() != null ? t.getCategory().getName() : null)
                .categoryType(t.getCategory() != null ? t.getCategory().getType() : null)
                .date(t.getDate())
                .settlementDate(t.getSettlementDate())
                .type(t.getType())
                .description(t.getDescription())
                .notes(t.getNotes())
                .amount(t.getAmount())
                .status(t.getStatus())
                .installment(t.isInstallment())
                .installmentTotal(t.getInstallmentTotal())
                .installmentIndex(t.getInstallmentIndex())
                .transferPeerId(t.getTransferPeerId())
                .tags(t.getTags() != null ? t.getTags().stream()
                        .map(tag -> TagDTO.builder().id(tag.getId()).name(tag.getName()).build())
                        .collect(Collectors.toSet()) : null)
                .createdAt(t.getCreatedAt())
                .build();
    }
}
