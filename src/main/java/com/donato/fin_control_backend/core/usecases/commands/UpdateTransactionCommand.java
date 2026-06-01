package com.donato.fin_control_backend.core.usecases.commands;

import com.donato.fin_control_backend.adapters.api.http.dto.request.UpdateTransactionDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class UpdateTransactionCommand {
    private Long accountId;
    private BigDecimal amount;
    private LocalDate date;
    private LocalDate settlementDate;
    private String description;
    private String notes;
    private Long categoryId;
    private Set<Long> tagIds;
    private String status;

    public static UpdateTransactionCommand from(UpdateTransactionDTO dto) {
        return UpdateTransactionCommand.builder()
                .accountId(dto.getAccountId())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .settlementDate(dto.getSettlementDate())
                .description(dto.getDescription())
                .notes(dto.getNotes())
                .categoryId(dto.getCategoryId())
                .tagIds(dto.getTagIds())
                .status(dto.getStatus())
                .build();
    }
}
