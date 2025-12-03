package com.donato.fin_control_backend.infrastructure.mappers;

import com.donato.fin_control_backend.core.domain.Session;
import com.donato.fin_control_backend.infrastructure.entities.SessionEntity;

public final class SessionMapper {

    private SessionMapper() {
    }

    public static Session toDomain(SessionEntity entity) {
        if (entity == null) {
            return null;
        }
        return Session.builder()
                .id(entity.getId())
                .user(UserMapper.toDomain(entity.getUser()))
                .jti(entity.getJti())
                .tokenHash(entity.getTokenHash())
                .createdAt(entity.getCreatedAt())
                .lastUsedAt(entity.getLastUsedAt())
                .expiresAt(entity.getExpiresAt())
                .ip(entity.getIp())
                .userAgent(entity.getUserAgent())
                .revoked(entity.isRevoked())
                .build();
    }

    public static SessionEntity toEntity(Session domain) {
        if (domain == null) {
            return null;
        }
        return SessionEntity.builder()
                .id(domain.getId())
                .user(UserMapper.toEntity(domain.getUser()))
                .jti(domain.getJti())
                .tokenHash(domain.getTokenHash())
                .createdAt(domain.getCreatedAt())
                .lastUsedAt(domain.getLastUsedAt())
                .expiresAt(domain.getExpiresAt())
                .ip(domain.getIp())
                .userAgent(domain.getUserAgent())
                .revoked(domain.isRevoked())
                .build();
    }
}
