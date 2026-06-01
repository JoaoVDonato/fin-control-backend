package com.donato.fin_control_backend.adapters.api.http.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountDTO {
    private Long id;
    private String name;
    private String type;
    private BigDecimal balance;
    private BigDecimal initialBalance;
    private String currency;
    private boolean archived;
    private String institution;
    private String color;
    private String icon;
    private LocalDateTime createdAt;
}
