package com.donato.fin_control_backend.infrastructure.mappers;

import com.donato.fin_control_backend.core.domain.Category;
import com.donato.fin_control_backend.infrastructure.entities.CategoryEntity;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static Category toDomain(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return Category.builder()
                .id(entity.getId())
                .user(UserMapper.toDomain(entity.getUser()))
                .name(entity.getName())
                .parent(toDomain(entity.getParent()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static CategoryEntity toEntity(Category domain) {
        if (domain == null) {
            return null;
        }
        return CategoryEntity.builder()
                .id(domain.getId())
                .user(UserMapper.toEntity(domain.getUser()))
                .name(domain.getName())
                .parent(toEntity(domain.getParent()))
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
