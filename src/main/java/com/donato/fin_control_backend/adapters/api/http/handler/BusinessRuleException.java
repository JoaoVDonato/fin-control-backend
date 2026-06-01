package com.donato.fin_control_backend.adapters.api.http.handler;

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
