package com.donato.fin_control_backend.core.usecases.commands;

import com.donato.fin_control_backend.adapters.api.http.dto.request.SetUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetUserCommand {

    private String email;
    private String nowPassword;
    private String newPassword;

    public static SetUserCommand from(SetUserDTO setUserDTO) {
        return SetUserCommand.builder()
                .email(setUserDTO.getEmail())
                .nowPassword(setUserDTO.getNowPassword())
                .newPassword(setUserDTO.getNewPassword())
                .build();
    }
}
