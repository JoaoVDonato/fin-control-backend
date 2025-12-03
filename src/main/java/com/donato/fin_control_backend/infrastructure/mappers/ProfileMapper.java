package com.donato.fin_control_backend.infrastructure.mappers;

import com.donato.fin_control_backend.core.domain.Profile;
import com.donato.fin_control_backend.infrastructure.entities.ProfileEntity;

public final class ProfileMapper {

    private ProfileMapper() {
    }

    public static Profile toDomain(ProfileEntity entity) {
        if (entity == null) {
            return null;
        }
        return Profile.builder()
                .id(entity.getId())
                .user(UserMapper.toDomain(entity.getUser()))
                .fullName(entity.getFullName())
                .displayName(entity.getDisplayName())
                .birthdate(entity.getBirthdate())
                .locale(entity.getLocale())
                .timezone(entity.getTimezone())
                .currency(entity.getCurrency())
                .phone(entity.getPhone())
                .avatarUrl(entity.getAvatarUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ProfileEntity toEntity(Profile domain) {
        if (domain == null) {
            return null;
        }
        return ProfileEntity.builder()
                .id(domain.getId())
                .user(UserMapper.toEntity(domain.getUser()))
                .fullName(domain.getFullName())
                .displayName(domain.getDisplayName())
                .birthdate(domain.getBirthdate())
                .locale(domain.getLocale())
                .timezone(domain.getTimezone())
                .currency(domain.getCurrency())
                .phone(domain.getPhone())
                .avatarUrl(domain.getAvatarUrl())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
