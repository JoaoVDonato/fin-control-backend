package com.donato.fin_control_backend.adapters.api.http.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SetUserDTO {

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String nowPassword;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;
}
