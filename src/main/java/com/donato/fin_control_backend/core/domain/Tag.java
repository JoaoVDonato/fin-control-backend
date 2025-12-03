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
public class Tag {

    // Identificador da tag
    private Long id;
    // Usuário que definiu a tag
    private User user;
    // Texto da tag
    private String name;
    // Data em que a tag foi criada
    private LocalDateTime createdAt;
}
