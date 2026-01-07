package com.donato.fin_control_backend.adapters.api.http.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginDTO {

    private String email;
    private String passwordHash;
}
