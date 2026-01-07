package com.donato.fin_control_backend.core.usecases.commands;

import com.donato.fin_control_backend.adapters.api.http.dto.request.LoginDTO;
import com.donato.fin_control_backend.core.domain.Email;
import com.donato.fin_control_backend.core.domain.Password;
import lombok.Value;

/**
 * Represents validated login input ready for use case handling.
 */
@Value
public class LoginCommand {

    Email email;
    Password password;

    public static LoginCommand from(LoginDTO loginDTO) {
        if (loginDTO == null) {
            throw new IllegalArgumentException("LoginDTO cannot be null");
        }
        return new LoginCommand(
                Email.of(loginDTO.getEmail()),
                Password.of(loginDTO.getPasswordHash())
        );
    }
}
