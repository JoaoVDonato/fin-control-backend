package com.donato.fin_control_backend.adapters.api.http.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateTransactionDTO {
    private Long accountId;
    private BigDecimal amount;
    private LocalDate date;
    private LocalDate settlementDate;
    private String description;
    private String notes;
    private Long categoryId;
    private Set<Long> tagIds;
    private String status;
}
