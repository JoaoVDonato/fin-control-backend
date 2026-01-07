package com.donato.fin_control_backend.infrastructure.mappers;

import com.donato.fin_control_backend.core.domain.Password;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.infrastructure.entities.UserEntity;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(mapToPassword(entity.getPasswordHash()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return UserEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .passwordHash(mapToHash(domain.getPasswordHash()))
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private static Password mapToPassword(String hash) {
        return hash == null ? null : Password.of(hash);
    }

    private static String mapToHash(Password password) {
        return password == null ? null : password.getValue();
    }
}
