package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.*;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.AccountService;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.ports.outbound.AccountRepository;
import com.donato.fin_control_backend.core.ports.outbound.DashboardRepository;
import com.donato.fin_control_backend.core.ports.outbound.UserRepository;
import com.donato.fin_control_backend.infrastructure.entities.AccountEntity;
import com.donato.fin_control_backend.infrastructure.entities.TransactionEntity;
import com.donato.fin_control_backend.infrastructure.entities.UserEntity;
import com.donato.fin_control_backend.infrastructure.mappers.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finControl/dashboard")
public class DashboardController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final DashboardRepository dashboardRepository;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestHeader("Authorization") String auth) {

        User user = userService.findUserByToken(extractToken(auth));
        UserEntity userEntity = resolveUserEntity(user);

        LocalDate now = LocalDate.now();
        LocalDate dateFrom = from != null ? from : now.withDayOfMonth(1);
        LocalDate dateTo = to != null ? to : now.withDayOfMonth(now.lengthOfMonth());

        // previous period
        YearMonth prevMonth = YearMonth.from(dateFrom).minusMonths(1);
        LocalDate prevFrom = prevMonth.atDay(1);
        LocalDate prevTo = prevMonth.atEndOfMonth();

        BigDecimal income = dashboardRepository.sumByTypeAndPeriod(userEntity.getId(), "INCOME", dateFrom, dateTo);
        BigDecimal expense = dashboardRepository.sumByTypeAndPeriod(userEntity.getId(), "EXPENSE", dateFrom, dateTo);
        BigDecimal prevIncome = dashboardRepository.sumByTypeAndPeriod(userEntity.getId(), "INCOME", prevFrom, prevTo);
        BigDecimal prevExpense = dashboardRepository.sumByTypeAndPeriod(userEntity.getId(), "EXPENSE", prevFrom, prevTo);

        // total balance = sum of active account balances
        List<AccountEntity> accounts = accountRepository.findAllByUserIdAndArchived(userEntity.getId(), false);
        BigDecimal totalBalance = accounts.stream()
                .map(AccountEntity::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DashboardSummaryDTO.AccountBalanceDTO> balanceByAccount = accounts.stream()
                .map(a -> DashboardSummaryDTO.AccountBalanceDTO.builder()
                        .id(a.getId()).name(a.getName()).type(a.getType())
                        .balance(a.getBalance()).currency(a.getCurrency())
                        .build())
                .toList();

        // expense by category
        List<Object[]> catRows = dashboardRepository.sumExpenseByCategory(userEntity.getId(), dateFrom, dateTo);
        BigDecimal totalExpenseForPct = expense.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : expense;
        List<DashboardSummaryDTO.CategoryExpenseDTO> expenseByCategory = catRows.stream()
                .map(row -> {
                    BigDecimal catTotal = (BigDecimal) row[2];
                    double pct = catTotal.divide(totalExpenseForPct, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                    return DashboardSummaryDTO.CategoryExpenseDTO.builder()
                            .categoryId(((Number) row[0]).longValue())
                            .categoryName((String) row[1])
                            .total(catTotal)
                            .percentage(pct)
                            .build();
                }).toList();

        // recent transactions (last 5)
        List<TransactionEntity> recentEntities = dashboardRepository.findRecentByUserId(
                userEntity.getId(), PageRequest.of(0, 5));
        List<TransactionDTO> recentTransactions = recentEntities.stream()
                .map(tx -> {
                    var t = TransactionMapper.toDomain(tx, Collections.emptySet());
                    return TransactionDTO.builder()
                            .id(t.getId())
                            .accountId(t.getAccount() != null ? t.getAccount().getId() : null)
                            .accountName(t.getAccount() != null ? t.getAccount().getName() : null)
                            .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                            .categoryName(t.getCategory() != null ? t.getCategory().getName() : null)
                            .date(t.getDate())
                            .type(t.getType())
                            .description(t.getDescription())
                            .amount(t.getAmount())
                            .status(t.getStatus())
                            .createdAt(t.getCreatedAt())
                            .build();
                }).toList();

        return ResponseEntity.ok(DashboardSummaryDTO.builder()
                .totalBalance(totalBalance)
                .totalIncome(income)
                .totalExpense(expense)
                .netResult(income.subtract(expense))
                .previousPeriodIncome(prevIncome)
                .previousPeriodExpense(prevExpense)
                .balanceByAccount(balanceByAccount)
                .expenseByCategory(expenseByCategory)
                .recentTransactions(recentTransactions)
                .build());
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<MonthlyTrendDTO> monthlyTrend(
            @RequestParam(defaultValue = "6") int months,
            @RequestHeader("Authorization") String auth) {

        User user = userService.findUserByToken(extractToken(auth));
        UserEntity userEntity = resolveUserEntity(user);

        LocalDate dateFrom = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);
        List<Object[]> rows = dashboardRepository.monthlyTrend(userEntity.getId(), dateFrom);

        // build map: year+month -> {income, expense}
        Map<String, BigDecimal[]> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            String key = y + "-" + String.format("%02d", m);
            String type = (String) row[2];
            BigDecimal amount = (BigDecimal) row[3];
            map.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if ("INCOME".equals(type)) map.get(key)[0] = amount;
            else map.get(key)[1] = amount;
        }

        // fill missing months with zero
        List<MonthlyTrendDTO.MonthEntry> entries = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            String key = ym.getYear() + "-" + String.format("%02d", ym.getMonthValue());
            BigDecimal[] vals = map.getOrDefault(key, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR"))
                    + "/" + ym.getYear();
            entries.add(MonthlyTrendDTO.MonthEntry.builder()
                    .year(ym.getYear()).month(ym.getMonthValue()).label(label)
                    .income(vals[0]).expense(vals[1]).net(vals[0].subtract(vals[1]))
                    .build());
        }

        return ResponseEntity.ok(MonthlyTrendDTO.builder().entries(entries).build());
    }

    private String extractToken(String auth) {
        return auth.startsWith("Bearer ") ? auth.substring(7) : auth.trim();
    }

    private UserEntity resolveUserEntity(User user) {
        if (user.getId() != null) {
            return userRepository.findById(user.getId())
                    .orElseThrow(() -> new com.donato.fin_control_backend.adapters.api.http.handler.ResourceNotFoundException("User not found"));
        }
        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new com.donato.fin_control_backend.adapters.api.http.handler.ResourceNotFoundException("User not found"));
    }
}
