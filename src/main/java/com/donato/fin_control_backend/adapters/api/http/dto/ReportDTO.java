package com.donato.fin_control_backend.adapters.api.http.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class ReportDTO {

    @Data
    @Builder
    public static class MonthlyReportEntry {
        private int year;
        private int month;
        private String label;
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal net;
    }

    @Data
    @Builder
    public static class CategoryReportEntry {
        private Long categoryId;
        private String categoryName;
        private String type;
        private BigDecimal total;
        private long count;
    }

    @Data
    @Builder
    public static class AccountReportEntry {
        private Long accountId;
        private String accountName;
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal net;
    }
}
