package com.donato.fin_control_backend.adapters.api.http.handler;

public class UserNotLog extends RuntimeException {
    public UserNotLog(String message) {
        super(message);
    }
}
