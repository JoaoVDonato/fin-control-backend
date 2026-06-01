package com.donato.fin_control_backend.adapters.api.http.dto.request;

import lombok.Data;

@Data
public class UpdateCategoryDTO {
    private String name;
    private String color;
    private String icon;
}
