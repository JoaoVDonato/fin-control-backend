package com.donato.fin_control_backend.adapters.api.http.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class CreateTransactionDTO {

    @NotNull(message = "Account is required")
    private Long accountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private LocalDate settlementDate;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "INCOME|EXPENSE|TRANSFER", message = "Type must be INCOME, EXPENSE or TRANSFER")
    private String type;

    private String description;
    private String notes;
    private Long categoryId;
    private Set<Long> tagIds;

    @Pattern(regexp = "PAID|PENDING|CANCELLED|SCHEDULED", message = "Invalid status")
    private String status;

    // For TRANSFER: destination account
    private Long destinationAccountId;

    // For installments
    private boolean installment;
    private Integer installmentTotal;
}
