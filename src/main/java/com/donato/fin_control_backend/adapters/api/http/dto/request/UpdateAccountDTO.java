package com.donato.fin_control_backend.adapters.api.http.dto.request;

import lombok.Data;

@Data
public class UpdateAccountDTO {
    private String name;
    private String type;
    private String currency;
    private String institution;
    private String color;
    private String icon;
}
