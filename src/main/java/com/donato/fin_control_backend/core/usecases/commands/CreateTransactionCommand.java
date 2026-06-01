package com.donato.fin_control_backend.core.usecases.commands;

import com.donato.fin_control_backend.adapters.api.http.dto.request.CreateTransactionDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class CreateTransactionCommand {
    private Long accountId;
    private BigDecimal amount;
    private LocalDate date;
    private LocalDate settlementDate;
    private String type;
    private String description;
    private String notes;
    private Long categoryId;
    private Set<Long> tagIds;
    private String status;
    private Long destinationAccountId;
    private boolean installment;
    private Integer installmentTotal;

    public static CreateTransactionCommand from(CreateTransactionDTO dto) {
        return CreateTransactionCommand.builder()
                .accountId(dto.getAccountId())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .settlementDate(dto.getSettlementDate())
                .type(dto.getType())
                .description(dto.getDescription())
                .notes(dto.getNotes())
                .categoryId(dto.getCategoryId())
                .tagIds(dto.getTagIds())
                .status(dto.getStatus() != null ? dto.getStatus() : "PENDING")
                .destinationAccountId(dto.getDestinationAccountId())
                .installment(dto.isInstallment())
                .installmentTotal(dto.getInstallmentTotal())
                .build();
    }
}
