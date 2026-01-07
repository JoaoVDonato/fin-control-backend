package com.donato.fin_control_backend.adapters.api.http.dto.response;

import com.donato.fin_control_backend.adapters.api.http.dto.UserDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse implements ApiResponse {

    private String token;

}
