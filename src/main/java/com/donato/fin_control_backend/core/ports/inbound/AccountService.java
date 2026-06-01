package com.donato.fin_control_backend.core.ports.inbound;

import com.donato.fin_control_backend.core.domain.Account;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.usecases.commands.CreateAccountCommand;
import com.donato.fin_control_backend.core.usecases.commands.UpdateAccountCommand;

import java.util.List;

public interface AccountService {

    Account createAccount(CreateAccountCommand command, User user);

    Account updateAccount(Long id, UpdateAccountCommand command, User user);

    Account getAccount(Long id, User user);

    List<Account> getAccounts(User user, boolean includeArchived);

    void archiveAccount(Long id, User user);
}
