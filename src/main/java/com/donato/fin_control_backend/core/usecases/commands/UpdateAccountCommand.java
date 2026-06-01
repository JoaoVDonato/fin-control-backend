package com.donato.fin_control_backend.core.usecases.commands;

import com.donato.fin_control_backend.adapters.api.http.dto.request.UpdateAccountDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateAccountCommand {
    private String name;
    private String type;
    private String currency;
    private String institution;
    private String color;
    private String icon;

    public static UpdateAccountCommand from(UpdateAccountDTO dto) {
        return UpdateAccountCommand.builder()
                .name(dto.getName())
                .type(dto.getType())
                .currency(dto.getCurrency())
                .institution(dto.getInstitution())
                .color(dto.getColor())
                .icon(dto.getIcon())
                .build();
    }
}
