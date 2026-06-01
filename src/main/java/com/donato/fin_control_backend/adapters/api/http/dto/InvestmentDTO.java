package com.donato.fin_control_backend.adapters.api.http.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InvestmentDTO {
    private Long id;
    private String name;
    private String type;
    private Long accountId;
    private String accountName;
    private BigDecimal balance;
    private List<MovementDTO> movements;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class MovementDTO {
        private Long id;
        private LocalDate date;
        private String movementType;
        private BigDecimal amount;
        private LocalDateTime createdAt;
    }
}
