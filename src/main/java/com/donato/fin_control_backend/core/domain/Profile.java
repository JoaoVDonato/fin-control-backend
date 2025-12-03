package com.donato.fin_control_backend.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    // Identificador do perfil
    private Long id;
    // Usuário ao qual o perfil pertence
    private User user;
    // Nome completo exibido em relatórios
    private String fullName;
    // Nome curto mostrado na interface
    private String displayName;
    // Data de nascimento do usuário
    private LocalDate birthdate;
    // Código de localidade preferido (ex.: pt-BR)
    private String locale;
    // Fuso horário preferido
    private String timezone;
    // Moeda preferencial para saldos
    private String currency;
    // Telefone de contato
    private String phone;
    // URL do avatar do usuário
    private String avatarUrl;
    // Data de criação do perfil
    private LocalDateTime createdAt;
    // Data da última atualização do perfil
    private LocalDateTime updatedAt;
}
