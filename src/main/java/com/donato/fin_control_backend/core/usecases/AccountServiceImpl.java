package com.donato.fin_control_backend.core.usecases;

import com.donato.fin_control_backend.adapters.api.http.handler.ResourceNotFoundException;
import com.donato.fin_control_backend.core.domain.Account;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.AccountService;
import com.donato.fin_control_backend.core.ports.outbound.AccountRepository;
import com.donato.fin_control_backend.core.ports.outbound.UserRepository;
import com.donato.fin_control_backend.core.usecases.commands.CreateAccountCommand;
import com.donato.fin_control_backend.core.usecases.commands.UpdateAccountCommand;
import com.donato.fin_control_backend.infrastructure.entities.AccountEntity;
import com.donato.fin_control_backend.infrastructure.entities.UserEntity;
import com.donato.fin_control_backend.infrastructure.mappers.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Account createAccount(CreateAccountCommand command, User user) {
        UserEntity userEntity = resolveUser(user);

        AccountEntity account = AccountEntity.builder()
                .user(userEntity)
                .name(command.getName())
                .type(command.getType())
                .balance(command.getInitialBalance())
                .initialBalance(command.getInitialBalance())
                .currency(command.getCurrency())
                .archived(false)
                .institution(command.getInstitution())
                .color(command.getColor())
                .icon(command.getIcon())
                .createdAt(LocalDateTime.now())
                .build();

        return AccountMapper.toDomain(accountRepository.save(account));
    }

    @Override
    @Transactional
    public Account updateAccount(Long id, UpdateAccountCommand command, User user) {
        UserEntity userEntity = resolveUser(user);
        AccountEntity account = accountRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (command.getName() != null) account.setName(command.getName());
        if (command.getType() != null) account.setType(command.getType());
        if (command.getCurrency() != null) account.setCurrency(command.getCurrency());
        if (command.getInstitution() != null) account.setInstitution(command.getInstitution());
        if (command.getColor() != null) account.setColor(command.getColor());
        if (command.getIcon() != null) account.setIcon(command.getIcon());

        return AccountMapper.toDomain(accountRepository.save(account));
    }

    @Override
    public Account getAccount(Long id, User user) {
        UserEntity userEntity = resolveUser(user);
        AccountEntity account = accountRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return AccountMapper.toDomain(account);
    }

    @Override
    public List<Account> getAccounts(User user, boolean includeArchived) {
        UserEntity userEntity = resolveUser(user);
        List<AccountEntity> entities = includeArchived
                ? accountRepository.findAllByUserId(userEntity.getId())
                : accountRepository.findAllByUserIdAndArchived(userEntity.getId(), false);
        return entities.stream().map(AccountMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void archiveAccount(Long id, User user) {
        UserEntity userEntity = resolveUser(user);
        AccountEntity account = accountRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        account.setArchived(true);
        accountRepository.save(account);
    }

    private UserEntity resolveUser(User user) {
        if (user.getId() != null) {
            return userRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
