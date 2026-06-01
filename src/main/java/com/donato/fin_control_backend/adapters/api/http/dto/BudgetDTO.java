package com.donato.fin_control_backend.adapters.api.http.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BudgetDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryType;
    private int periodMonth;
    private int periodYear;
    private BigDecimal amount;
    private BigDecimal spent;
    private double percentUsed;
    private boolean exceeded;
    private LocalDateTime createdAt;
}
