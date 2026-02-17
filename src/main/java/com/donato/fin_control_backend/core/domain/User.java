package com.donato.fin_control_backend.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    // Identificador único do usuário
    private Long id;
    // E-mail usado para login e comunicações
    private String email;
    // Hash da senha (BCrypt ou similar)
    private Password passwordHash;
    // Indica se o usuário está ativo
    private boolean active;
    // Data de criação do registro
    private LocalDateTime createdAt;
    // Última atualização do registro
    private LocalDateTime updatedAt;

    public User(String email, Password password){
        this.email = email;
        this.passwordHash = password;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordHash != null ? passwordHash.getValue() : null;

    }

    @Override
    public String getUsername() {
        return email;
    }
}
