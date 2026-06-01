package com.donato.fin_control_backend.adapters.api.http.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardSummaryDTO {
    private BigDecimal totalBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netResult;
    private BigDecimal previousPeriodIncome;
    private BigDecimal previousPeriodExpense;
    private List<AccountBalanceDTO> balanceByAccount;
    private List<CategoryExpenseDTO> expenseByCategory;
    private List<TransactionDTO> recentTransactions;

    @Data
    @Builder
    public static class AccountBalanceDTO {
        private Long id;
        private String name;
        private String type;
        private BigDecimal balance;
        private String currency;
    }

    @Data
    @Builder
    public static class CategoryExpenseDTO {
        private Long categoryId;
        private String categoryName;
        private BigDecimal total;
        private double percentage;
    }
}
