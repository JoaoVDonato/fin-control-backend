package com.donato.fin_control_backend.core.usecases.commands;

import com.donato.fin_control_backend.adapters.api.http.dto.request.CreateCategoryDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateCategoryCommand {
    private String name;
    private String type;
    private Long parentId;
    private String color;
    private String icon;

    public static CreateCategoryCommand from(CreateCategoryDTO dto) {
        return CreateCategoryCommand.builder()
                .name(dto.getName())
                .type(dto.getType())
                .parentId(dto.getParentId())
                .color(dto.getColor())
                .icon(dto.getIcon())
                .build();
    }
}
