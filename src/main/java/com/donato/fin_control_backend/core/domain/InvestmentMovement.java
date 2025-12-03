package com.donato.fin_control_backend.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentMovement {

    // Identificador da movimentação
    private Long id;
    // Investimento ao qual pertence
    private Investment investment;
    // Data do aporte/resgate/rendimento
    private LocalDate date;
    // Tipo da movimentação (APORTE, RESGATE, etc.)
    private String movementType;
    // Valor movimentado
    private BigDecimal amount;
    // Data de criação do registro
    private LocalDateTime createdAt;
}
