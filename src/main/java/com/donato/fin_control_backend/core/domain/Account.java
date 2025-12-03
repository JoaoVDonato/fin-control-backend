package com.donato.fin_control_backend.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    // Identificador da conta financeira
    private Long id;
    // Dono da conta
    private User user;
    // Nome amigável da conta (ex.: Conta Corrente)
    private String name;
    // Tipo da conta (corrente, cartão, etc.)
    private String type;
    // Saldo atual registrado para a conta
    private BigDecimal balance;
    // Código da moeda do saldo
    private String currency;
    // Data de criação da conta no sistema
    private LocalDateTime createdAt;
}
