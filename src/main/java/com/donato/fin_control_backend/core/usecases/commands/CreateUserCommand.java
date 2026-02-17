package com.donato.fin_control_backend.core.usecases.commands;

import com.donato.fin_control_backend.adapters.api.http.dto.request.CreateUserDTO;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserCommand {

    private String email;
    private String password;
    private String phone;
    private String name;

    public static CreateUserCommand from(CreateUserDTO createUserDTO){
        return CreateUserCommand.builder()
                .email(createUserDTO.email())
                .password(createUserDTO.password())
                .phone(createUserDTO.phone())
                .name(createUserDTO.name())
                .build();
    }
}
