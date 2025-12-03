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
public class Category {

    // Identificador da categoria
    private Long id;
    // Usuário que criou a categoria
    private User user;
    // Nome da categoria (ex.: Alimentação)
    private String name;
    // Categoria pai (quando existe hierarquia)
    private Category parent;
    // Data de criação da categoria
    private LocalDateTime createdAt;
}
