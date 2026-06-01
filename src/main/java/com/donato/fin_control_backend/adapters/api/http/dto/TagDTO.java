package com.donato.fin_control_backend.adapters.api.http.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TagDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
