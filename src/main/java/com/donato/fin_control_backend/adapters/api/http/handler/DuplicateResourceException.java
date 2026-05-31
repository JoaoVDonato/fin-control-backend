package com.donato.fin_control_backend.adapters.api.http.handler;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
