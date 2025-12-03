package com.donato.fin_control_backend.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investment {

    // Identificador do investimento
    private Long id;
    // Usuário dono do investimento
    private User user;
    // Conta usada para registrar o ativo
    private Account account;
    // Nome do investimento (ex.: Tesouro Selic)
    private String name;
    // Tipo de ativo (CDB, AÇÃO, etc.)
    private String type;
    // Data de criação do registro
    private LocalDateTime createdAt;
    // Movimentações (aportes, resgates, rendimentos)
    private List<InvestmentMovement> movements;
}
