package com.donato.fin_control_backend.adapters.api.http.dto.request;

import lombok.Getter;

@Getter
public class SetUserDTO {

    private String email;
    private String nowPassword;
    private String newPassword;
}
