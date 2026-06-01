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
                .type(entity.getType())
                .parent(entity.getParent() != null
                        ? Category.builder()
                                .id(entity.getParent().getId())
                                .name(entity.getParent().getName())
                                .type(entity.getParent().getType())
                                .build()
                        : null)
                .color(entity.getColor())
                .icon(entity.getIcon())
                .active(entity.isActive())
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
                .type(domain.getType())
                .color(domain.getColor())
                .icon(domain.getIcon())
                .active(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
