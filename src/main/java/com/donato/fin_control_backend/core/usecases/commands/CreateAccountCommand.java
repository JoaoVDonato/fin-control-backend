package com.donato.fin_control_backend.core.usecases.commands;

import com.donato.fin_control_backend.adapters.api.http.dto.request.CreateAccountDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreateAccountCommand {
    private String name;
    private String type;
    private BigDecimal initialBalance;
    private String currency;
    private String institution;
    private String color;
    private String icon;

    public static CreateAccountCommand from(CreateAccountDTO dto) {
        return CreateAccountCommand.builder()
                .name(dto.getName())
                .type(dto.getType())
                .initialBalance(dto.getInitialBalance() != null ? dto.getInitialBalance() : BigDecimal.ZERO)
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "BRL")
                .institution(dto.getInstitution())
                .color(dto.getColor())
                .icon(dto.getIcon())
                .build();
    }
}
