package com.donato.fin_control_backend.infrastructure.mappers;

import com.donato.fin_control_backend.core.domain.Tag;
import com.donato.fin_control_backend.infrastructure.entities.TagEntity;

public final class TagMapper {

    private TagMapper() {
    }

    public static Tag toDomain(TagEntity entity) {
        if (entity == null) {
            return null;
        }
        return Tag.builder()
                .id(entity.getId())
                .user(UserMapper.toDomain(entity.getUser()))
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static TagEntity toEntity(Tag domain) {
        if (domain == null) {
            return null;
        }
        return TagEntity.builder()
                .id(domain.getId())
                .user(UserMapper.toEntity(domain.getUser()))
                .name(domain.getName())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
