package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.RecurringRuleDTO;
import com.donato.fin_control_backend.adapters.api.http.handler.BusinessRuleException;
import com.donato.fin_control_backend.adapters.api.http.handler.ResourceNotFoundException;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.TransactionService;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.ports.outbound.*;
import com.donato.fin_control_backend.core.usecases.commands.CreateTransactionCommand;
import com.donato.fin_control_backend.infrastructure.entities.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finControl/recurrences")
public class RecurrenceController {

    private final RecurringRuleRepository ruleRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<RecurringRuleDTO> create(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));

        String type = (String) body.get("type");
        String freq = (String) body.get("frequency");
        if (!List.of("INCOME", "EXPENSE").contains(type))
            throw new IllegalArgumentException("type must be INCOME or EXPENSE");
        if (!List.of("MONTHLY", "WEEKLY", "YEARLY").contains(freq))
            throw new IllegalArgumentException("frequency must be MONTHLY, WEEKLY or YEARLY");

        AccountEntity account = null;
        if (body.get("accountId") != null) {
            Long accId = ((Number) body.get("accountId")).longValue();
            account = accountRepository.findByIdAndUserId(accId, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        }

        CategoryEntity category = null;
        if (body.get("categoryId") != null) {
            Long catId = ((Number) body.get("categoryId")).longValue();
            category = categoryRepository.findByIdAndUserId(catId, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        RecurringRuleEntity rule = RecurringRuleEntity.builder()
                .user(user).account(account).category(category)
                .type(type).amount(new BigDecimal(body.get("amount").toString()))
                .description((String) body.get("description"))
                .frequency(freq)
                .dayOfPeriod(body.get("dayOfPeriod") != null ? ((Number) body.get("dayOfPeriod")).intValue() : null)
                .startDate(LocalDate.parse((String) body.get("startDate")))
                .endDate(body.get("endDate") != null ? LocalDate.parse((String) body.get("endDate")) : null)
                .active(true).createdAt(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(ruleRepository.save(rule)));
    }

    @GetMapping
    public ResponseEntity<List<RecurringRuleDTO>> list(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        List<RecurringRuleEntity> rules = activeOnly
                ? ruleRepository.findAllByUserIdAndActive(user.getId(), true)
                : ruleRepository.findAllByUserId(user.getId());
        return ResponseEntity.ok(rules.stream().map(this::toDTO).toList());
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<RecurringRuleDTO> pause(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        RecurringRuleEntity rule = ruleRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recurring rule not found"));
        rule.setActive(false);
        return ResponseEntity.ok(toDTO(ruleRepository.save(rule)));
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<RecurringRuleDTO> resume(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        RecurringRuleEntity rule = ruleRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recurring rule not found"));
        rule.setActive(true);
        return ResponseEntity.ok(toDTO(ruleRepository.save(rule)));
    }

    /**
     * Manually generate transactions for a rule up to a given date.
     * Preserves historical records (RB-010): only creates future instances.
     */
    @PostMapping("/{id}/generate")
    public ResponseEntity<Map<String, Object>> generate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        UserEntity userEntity = resolve(user);
        RecurringRuleEntity rule = ruleRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recurring rule not found"));

        if (!rule.isActive()) throw new BusinessRuleException("Cannot generate from an inactive rule");
        if (rule.getAccount() == null) throw new BusinessRuleException("Rule has no account configured");

        LocalDate upTo = LocalDate.parse((String) body.get("upTo"));
        List<LocalDate> dates = generateDates(rule, upTo);
        int generated = 0;
        for (LocalDate date : dates) {
            CreateTransactionCommand cmd = CreateTransactionCommand.builder()
                    .accountId(rule.getAccount().getId())
                    .amount(rule.getAmount())
                    .date(date)
                    .type(rule.getType())
                    .description(rule.getDescription())
                    .categoryId(rule.getCategory() != null ? rule.getCategory().getId() : null)
                    .status("PENDING")
                    .build();
            transactionService.createTransaction(cmd, user);
            generated++;
        }

        return ResponseEntity.ok(Map.of("generated", generated, "upTo", upTo.toString()));
    }

    private List<LocalDate> generateDates(RecurringRuleEntity rule, LocalDate upTo) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = rule.getStartDate();
        while (!cursor.isAfter(upTo)) {
            dates.add(cursor);
            cursor = switch (rule.getFrequency()) {
                case "MONTHLY" -> cursor.plusMonths(1);
                case "WEEKLY" -> cursor.plusWeeks(1);
                case "YEARLY" -> cursor.plusYears(1);
                default -> upTo.plusDays(1); // stop
            };
            if (rule.getEndDate() != null && cursor.isAfter(rule.getEndDate())) break;
        }
        return dates;
    }

    private RecurringRuleDTO toDTO(RecurringRuleEntity r) {
        return RecurringRuleDTO.builder()
                .id(r.getId())
                .accountId(r.getAccount() != null ? r.getAccount().getId() : null)
                .accountName(r.getAccount() != null ? r.getAccount().getName() : null)
                .categoryId(r.getCategory() != null ? r.getCategory().getId() : null)
                .categoryName(r.getCategory() != null ? r.getCategory().getName() : null)
                .type(r.getType()).amount(r.getAmount()).description(r.getDescription())
                .frequency(r.getFrequency()).dayOfPeriod(r.getDayOfPeriod())
                .startDate(r.getStartDate()).endDate(r.getEndDate())
                .active(r.isActive()).createdAt(r.getCreatedAt())
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
