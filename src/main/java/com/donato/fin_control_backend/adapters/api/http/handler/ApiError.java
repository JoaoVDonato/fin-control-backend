package com.donato.fin_control_backend.adapters.api.http.handler;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiError {

    private final int status;
    private final String error;
    private final String message;
    private final LocalDateTime timestamp;
}
