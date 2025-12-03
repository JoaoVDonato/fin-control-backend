package com.donato.fin_control_backend.infrastructure.mappers;

import com.donato.fin_control_backend.core.domain.Investment;
import com.donato.fin_control_backend.core.domain.InvestmentMovement;
import com.donato.fin_control_backend.infrastructure.entities.InvestmentEntity;

import java.util.Collections;
import java.util.List;

public final class InvestmentMapper {

    private InvestmentMapper() {
    }

    public static Investment toDomain(InvestmentEntity entity) {
        return toDomain(entity, Collections.emptyList());
    }

    public static Investment toDomain(InvestmentEntity entity, List<InvestmentMovement> movements) {
        if (entity == null) {
            return null;
        }
        return Investment.builder()
                .id(entity.getId())
                .user(UserMapper.toDomain(entity.getUser()))
                .account(AccountMapper.toDomain(entity.getAccount()))
                .name(entity.getName())
                .type(entity.getType())
                .createdAt(entity.getCreatedAt())
                .movements(movements == null ? Collections.emptyList() : movements)
                .build();
    }

    public static InvestmentEntity toEntity(Investment domain) {
        if (domain == null) {
            return null;
        }
        return InvestmentEntity.builder()
                .id(domain.getId())
                .user(UserMapper.toEntity(domain.getUser()))
                .account(AccountMapper.toEntity(domain.getAccount()))
                .name(domain.getName())
                .type(domain.getType())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
