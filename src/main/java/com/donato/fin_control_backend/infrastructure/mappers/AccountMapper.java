package com.donato.fin_control_backend.infrastructure.mappers;

import com.donato.fin_control_backend.core.domain.Account;
import com.donato.fin_control_backend.infrastructure.entities.AccountEntity;

public final class AccountMapper {

    private AccountMapper() {}

    public static Account toDomain(AccountEntity entity) {
        if (entity == null) return null;
        return Account.builder()
                .id(entity.getId())
                .user(UserMapper.toDomain(entity.getUser()))
                .name(entity.getName())
                .type(entity.getType())
                .balance(entity.getBalance())
                .initialBalance(entity.getInitialBalance())
                .currency(entity.getCurrency())
                .archived(entity.isArchived())
                .institution(entity.getInstitution())
                .color(entity.getColor())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static AccountEntity toEntity(Account domain) {
        if (domain == null) return null;
        return AccountEntity.builder()
                .id(domain.getId())
                .user(UserMapper.toEntity(domain.getUser()))
                .name(domain.getName())
                .type(domain.getType())
                .balance(domain.getBalance())
                .initialBalance(domain.getInitialBalance())
                .currency(domain.getCurrency())
                .archived(domain.isArchived())
                .institution(domain.getInstitution())
                .color(domain.getColor())
                .icon(domain.getIcon())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
