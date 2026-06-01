package com.donato.fin_control_backend.core.usecases.commands;

import com.donato.fin_control_backend.adapters.api.http.dto.request.UpdateCategoryDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateCategoryCommand {
    private String name;
    private String color;
    private String icon;

    public static UpdateCategoryCommand from(UpdateCategoryDTO dto) {
        return UpdateCategoryCommand.builder()
                .name(dto.getName())
                .color(dto.getColor())
                .icon(dto.getIcon())
                .build();
    }
}
