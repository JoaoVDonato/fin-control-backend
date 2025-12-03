package com.donato.fin_control_backend.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // Identificador único do usuário
    private Long id;
    // Nome de usuário usado no login e único no sistema
    private String username;
    // E-mail opcional para notificações e recuperação
    private String email;
    // Hash da senha (BCrypt ou similar)
    private String passwordHash;
    // Data de criação do registro
    private LocalDateTime createdAt;
    // Última atualização do registro
    private LocalDateTime updatedAt;
}
