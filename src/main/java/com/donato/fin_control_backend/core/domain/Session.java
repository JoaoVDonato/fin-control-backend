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
public class Session {

    // Identificador da sessão
    private Long id;
    // Usuário autenticado nesta sessão
    private User user;
    // Identificador JWT (jti) do token emitido
    private String jti;
    // Hash do token persistido para verificação
    private String tokenHash;
    // Data de criação da sessão
    private LocalDateTime createdAt;
    // Última vez em que o token foi utilizado
    private LocalDateTime lastUsedAt;
    // Data e hora de expiração do token
    private LocalDateTime expiresAt;
    // Endereço IP do dispositivo
    private String ip;
    // Assinatura do navegador/aplicativo
    private String userAgent;
    // Flag indicando se a sessão foi revogada
    private boolean revoked;
}
