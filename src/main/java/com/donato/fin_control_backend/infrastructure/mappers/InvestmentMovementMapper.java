package com.donato.fin_control_backend.infrastructure.mappers;

import com.donato.fin_control_backend.core.domain.InvestmentMovement;
import com.donato.fin_control_backend.infrastructure.entities.InvestmentMovementEntity;

public final class InvestmentMovementMapper {

    private InvestmentMovementMapper() {
    }

    public static InvestmentMovement toDomain(InvestmentMovementEntity entity) {
        if (entity == null) {
            return null;
        }
        return InvestmentMovement.builder()
                .id(entity.getId())
                .investment(InvestmentMapper.toDomain(entity.getInvestment()))
                .date(entity.getDate())
                .movementType(entity.getMovementType())
                .amount(entity.getAmount())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static InvestmentMovementEntity toEntity(InvestmentMovement domain) {
        if (domain == null) {
            return null;
        }
        return InvestmentMovementEntity.builder()
                .id(domain.getId())
                .investment(InvestmentMapper.toEntity(domain.getInvestment()))
                .date(domain.getDate())
                .movementType(domain.getMovementType())
                .amount(domain.getAmount())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
