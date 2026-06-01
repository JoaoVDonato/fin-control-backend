package com.donato.fin_control_backend.adapters.api.http.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class TransactionDTO {
    private Long id;
    private Long accountId;
    private String accountName;
    private Long categoryId;
    private String categoryName;
    private String categoryType;
    private LocalDate date;
    private LocalDate settlementDate;
    private String type;
    private String description;
    private String notes;
    private BigDecimal amount;
    private String status;
    private boolean installment;
    private Integer installmentTotal;
    private Integer installmentIndex;
    private Long transferPeerId;
    private Set<TagDTO> tags;
    private LocalDateTime createdAt;
}
