package com.donato.fin_control_backend.infrastructure.mappers;

import com.donato.fin_control_backend.core.domain.Tag;
import com.donato.fin_control_backend.core.domain.Transaction;
import com.donato.fin_control_backend.infrastructure.entities.TransactionEntity;

import java.util.Collections;
import java.util.Set;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static Transaction toDomain(TransactionEntity entity) {
        return toDomain(entity, Collections.emptySet());
    }

    public static Transaction toDomain(TransactionEntity entity, Set<Tag> tags) {
        if (entity == null) {
            return null;
        }
        return Transaction.builder()
                .id(entity.getId())
                .user(UserMapper.toDomain(entity.getUser()))
                .account(AccountMapper.toDomain(entity.getAccount()))
                .category(CategoryMapper.toDomain(entity.getCategory()))
                .date(entity.getDate())
                .settlementDate(entity.getSettlementDate())
                .type(entity.getType())
                .description(entity.getDescription())
                .notes(entity.getNotes())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .installment(entity.isInstallment())
                .installmentTotal(entity.getInstallmentTotal())
                .installmentIndex(entity.getInstallmentIndex())
                .transferPeerId(entity.getTransferPeer() != null ? entity.getTransferPeer().getId() : null)
                .createdAt(entity.getCreatedAt())
                .tags(tags == null ? Collections.emptySet() : tags)
                .build();
    }

    public static TransactionEntity toEntity(Transaction domain) {
        if (domain == null) {
            return null;
        }
        return TransactionEntity.builder()
                .id(domain.getId())
                .user(UserMapper.toEntity(domain.getUser()))
                .account(AccountMapper.toEntity(domain.getAccount()))
                .category(CategoryMapper.toEntity(domain.getCategory()))
                .date(domain.getDate())
                .settlementDate(domain.getSettlementDate())
                .type(domain.getType())
                .description(domain.getDescription())
                .notes(domain.getNotes())
                .amount(domain.getAmount())
                .status(domain.getStatus())
                .installment(domain.isInstallment())
                .installmentTotal(domain.getInstallmentTotal())
                .installmentIndex(domain.getInstallmentIndex())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
