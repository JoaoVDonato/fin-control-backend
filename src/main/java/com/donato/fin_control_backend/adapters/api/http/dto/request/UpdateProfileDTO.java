package com.donato.fin_control_backend.adapters.api.http.dto.request;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UpdateProfileDTO {

    private String fullName;
    private String displayName;
    private LocalDate birthdate;
    private String locale;
    private String timezone;
    private String currency;
    private String phone;
    private String avatarUrl;
}
