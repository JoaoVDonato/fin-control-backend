package com.donato.fin_control_backend.adapters.api.http.handler;

public class PasswordValidationException extends RuntimeException {
    public PasswordValidationException(String message) {
        super(message);
    }
}
