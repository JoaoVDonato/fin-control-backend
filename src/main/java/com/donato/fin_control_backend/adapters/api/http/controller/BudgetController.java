package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.BudgetDTO;
import com.donato.fin_control_backend.adapters.api.http.handler.BusinessRuleException;
import com.donato.fin_control_backend.adapters.api.http.handler.ResourceNotFoundException;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.ports.outbound.BudgetRepository;
import com.donato.fin_control_backend.core.ports.outbound.CategoryRepository;
import com.donato.fin_control_backend.core.ports.outbound.UserRepository;
import com.donato.fin_control_backend.infrastructure.entities.BudgetEntity;
import com.donato.fin_control_backend.infrastructure.entities.CategoryEntity;
import com.donato.fin_control_backend.infrastructure.entities.UserEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finControl/budgets")
public class BudgetController {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<BudgetDTO> create(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));

        Long categoryId = ((Number) body.get("categoryId")).longValue();
        int month = ((Number) body.get("month")).intValue();
        int year = ((Number) body.get("year")).intValue();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());

        CategoryEntity category = categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        budgetRepository.findByUserIdAndCategoryIdAndPeriodMonthAndPeriodYear(user.getId(), categoryId, month, year)
                .ifPresent(b -> { throw new BusinessRuleException("Budget already exists for this category and period"); });

        BudgetEntity budget = BudgetEntity.builder()
                .user(user).category(category)
                .periodMonth(month).periodYear(year)
                .amount(amount).createdAt(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(budgetRepository.save(budget), user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<BudgetDTO>> list(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        YearMonth now = YearMonth.now();
        int m = month != null ? month : now.getMonthValue();
        int y = year != null ? year : now.getYear();
        List<BudgetDTO> result = budgetRepository
                .findAllByUserIdAndPeriodMonthAndPeriodYear(user.getId(), m, y)
                .stream().map(b -> toDTO(b, user.getId())).toList();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDTO> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        BudgetEntity budget = budgetRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        if (body.containsKey("amount")) {
            budget.setAmount(new BigDecimal(body.get("amount").toString()));
        }
        return ResponseEntity.ok(toDTO(budgetRepository.save(budget), user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        BudgetEntity budget = budgetRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budgetRepository.delete(budget);
        return ResponseEntity.noContent().build();
    }

    private BudgetDTO toDTO(BudgetEntity b, Long userId) {
        BigDecimal spent = budgetRepository.sumSpentByCategory(userId, b.getCategory().getId(),
                b.getPeriodMonth(), b.getPeriodYear());
        double pct = b.getAmount().compareTo(BigDecimal.ZERO) == 0 ? 0 :
                spent.divide(b.getAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
        return BudgetDTO.builder()
                .id(b.getId())
                .categoryId(b.getCategory().getId())
                .categoryName(b.getCategory().getName())
                .categoryType(b.getCategory().getType())
                .periodMonth(b.getPeriodMonth())
                .periodYear(b.getPeriodYear())
                .amount(b.getAmount())
                .spent(spent)
                .percentUsed(pct)
                .exceeded(pct > 100)
                .createdAt(b.getCreatedAt())
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
