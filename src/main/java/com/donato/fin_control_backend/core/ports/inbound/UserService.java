package com.donato.fin_control_backend.core.ports.inbound;

import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.usecases.commands.CreateUserCommand;
import com.donato.fin_control_backend.core.usecases.commands.LoginCommand;
import com.donato.fin_control_backend.core.usecases.commands.SetUserCommand;

public interface UserService {

    void createUser(CreateUserCommand createUserCommand);

    User alterUser(SetUserCommand setUserCommand, User user);

    User findUserByToken(String token);

    String login(LoginCommand loginCommand);

    void deleteUser(User user, String password);

    void logout(String token);
}
