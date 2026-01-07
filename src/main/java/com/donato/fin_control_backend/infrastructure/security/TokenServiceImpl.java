package com.donato.fin_control_backend.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.donato.fin_control_backend.core.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl {

    @Value("${api.security.token.secret}")
    private String secret;

    //private final TokenInvalidoRepository tokenInvalidoRepository;

    public String generateToken(User userLogin) {
        try {

            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("fin-control")
                    .withSubject(userLogin.getEmail())
                    .withClaim("role", userLogin.getPassword())
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);

        } catch (JWTCreationException | IllegalArgumentException jwtCreationException) {
            throw new RuntimeException("Erro ao gerar token", jwtCreationException);
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusDays(14).toInstant(ZoneOffset.of("-03:00"));
    }
}
