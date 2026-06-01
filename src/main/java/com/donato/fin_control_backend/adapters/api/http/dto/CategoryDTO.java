package com.donato.fin_control_backend.adapters.api.http.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoryDTO {
    private Long id;
    private String name;
    private String type;
    private Long parentId;
    private String parentName;
    private String color;
    private String icon;
    private boolean active;
    private LocalDateTime createdAt;
}
