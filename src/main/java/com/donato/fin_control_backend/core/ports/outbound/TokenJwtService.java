package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.core.domain.User;

public interface TokenJwtService {

     String generateToken(User userLogin);

    String extractEmail(String token);

    String extractJti(String token);

    java.time.Instant extractExpiration(String token);

}
