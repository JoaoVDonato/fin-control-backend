package com.donato.fin_control_backend.adapters.api.http.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Type is required")
    private String type;

    private BigDecimal initialBalance;
    private String currency;
    private String institution;
    private String color;
    private String icon;
}
