package com.donato.fin_control_backend.infrastructure.mappers;

import com.donato.fin_control_backend.core.domain.TransactionTag;
import com.donato.fin_control_backend.infrastructure.entities.TransactionTagEntity;
import com.donato.fin_control_backend.infrastructure.entities.TransactionTagId;

public final class TransactionTagMapper {

    private TransactionTagMapper() {
    }

    public static TransactionTag toDomain(TransactionTagEntity entity) {
        if (entity == null) {
            return null;
        }
        return TransactionTag.builder()
                .transaction(TransactionMapper.toDomain(entity.getTransaction()))
                .tag(TagMapper.toDomain(entity.getTag()))
                .build();
    }

    public static TransactionTagEntity toEntity(TransactionTag domain) {
        if (domain == null) {
            return null;
        }
        TransactionTagId id = new TransactionTagId(
                domain.getTransaction() != null ? domain.getTransaction().getId() : null,
                domain.getTag() != null ? domain.getTag().getId() : null
        );
        return TransactionTagEntity.builder()
                .id(id)
                .transaction(TransactionMapper.toEntity(domain.getTransaction()))
                .tag(TagMapper.toEntity(domain.getTag()))
                .build();
    }
}
