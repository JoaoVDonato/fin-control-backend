package com.donato.fin_control_backend.core.usecases.commands;

import com.donato.fin_control_backend.adapters.api.http.dto.request.CreateProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfileCommand {

    private String fullName;
    private String displayName;
    private LocalDate birthdate;
    private String locale;
    private String timezone;
    private String currency;
    private String phone;
    private String avatarUrl;

    public static CreateProfileCommand from(CreateProfileDTO dto) {
        return CreateProfileCommand.builder()
                .fullName(dto.getFullName())
                .displayName(dto.getDisplayName())
                .birthdate(dto.getBirthdate())
                .locale(dto.getLocale())
                .timezone(dto.getTimezone())
                .currency(dto.getCurrency())
                .phone(dto.getPhone())
                .avatarUrl(dto.getAvatarUrl())
                .build();
    }
}
