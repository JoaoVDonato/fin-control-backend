package com.donato.fin_control_backend.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value object used to validate and transport user e-mail addresses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email {

    private String value;

    public static Email of(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        return Email.builder()
                .value(email)
                .build();
    }
}
