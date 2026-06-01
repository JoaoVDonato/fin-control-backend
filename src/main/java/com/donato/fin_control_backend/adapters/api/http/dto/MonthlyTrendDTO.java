package com.donato.fin_control_backend.adapters.api.http.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MonthlyTrendDTO {
    private List<MonthEntry> entries;

    @Data
    @Builder
    public static class MonthEntry {
        private int year;
        private int month;
        private String label;
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal net;
    }
}
