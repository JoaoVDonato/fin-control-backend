package com.donato.fin_control_backend.core.usecases;

import com.donato.fin_control_backend.adapters.api.http.handler.PasswordValidationException;
import com.donato.fin_control_backend.adapters.api.http.handler.UserNotLog;
import com.donato.fin_control_backend.adapters.api.http.handler.DuplicateResourceException;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.ports.outbound.ProfileRepository;
import com.donato.fin_control_backend.core.ports.outbound.SessionRepository;
import com.donato.fin_control_backend.core.ports.outbound.TokenJwtService;
import com.donato.fin_control_backend.core.ports.outbound.UserRepository;
import com.donato.fin_control_backend.core.usecases.commands.CreateUserCommand;
import com.donato.fin_control_backend.core.usecases.commands.LoginCommand;
import com.donato.fin_control_backend.core.usecases.commands.SetUserCommand;
import com.donato.fin_control_backend.infrastructure.entities.ProfileEntity;
import com.donato.fin_control_backend.infrastructure.entities.SessionEntity;
import com.donato.fin_control_backend.infrastructure.entities.UserEntity;
import com.donato.fin_control_backend.infrastructure.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SessionRepository sessionRepository;
    private final BCryptPasswordEncoder encoder;
    private final TokenJwtService tokenService;

    @Override
    @Transactional
    public void createUser(CreateUserCommand createUserCommand) {

        String email = createUserCommand.getEmail();

        userRepository.findByEmail(email).ifPresent(user -> {
            throw new DuplicateResourceException("Usuário já existe");
        });

        UserEntity userEntity = builderUser(createUserCommand);
        UserEntity savedUser = userRepository.save(userEntity);

        if (shouldCreateProfile(createUserCommand)) {
            ProfileEntity profileEntity = builderProfile(createUserCommand);
            profileEntity.setUser(savedUser);
            profileRepository.save(profileEntity);
        }
    }

    @Transactional
    @Override
    public User alterUser(SetUserCommand setUserCommand, User user) {

        UserEntity userEntity = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UserNotLog("Usuário não está logado, verifique o token enviado."));

        ensureUserActive(userEntity);

        boolean emailChanged = isNotBlank(setUserCommand.getEmail()) && !setUserCommand.getEmail().equals(userEntity.getEmail());
        boolean passwordChanged = isNotBlank(setUserCommand.getNewPassword());
        validateUserUpdateRequest(setUserCommand, emailChanged, passwordChanged);
        validateCurrentPassword(setUserCommand.getNowPassword(), userEntity.getPasswordHash());

        if (emailChanged) {
            ensureEmailAvailable(setUserCommand.getEmail());
            userEntity.setEmail(setUserCommand.getEmail());
        }

        if (passwordChanged) {
            String encode = encoder.encode(setUserCommand.getNewPassword());
            userEntity.setPasswordHash(encode);
        }

        userEntity.setUpdatedAt(LocalDateTime.now());
        UserEntity updated = userRepository.save(userEntity);
        revokeSessionsForUser(updated.getId());

        return UserMapper.toDomain(updated);
    }

    @Override
    public User findUserByToken(String token){
        ensureSessionValid(token);
        UserEntity userEntity = userRepository.findByEmail(tokenService.extractEmail(token))
                .orElseThrow(() -> new UserNotLog("Usuário não está logado, verifique o token enviado."));
        ensureUserActive(userEntity);
        return UserMapper.toDomain(userEntity);
    }

    @Override
    public String login(LoginCommand loginCommand) {
        UserEntity userEntity = userRepository.findByEmail(loginCommand.email().getValue())
                .orElseThrow(() -> new UserNotLog("Credenciais inválidas."));

        ensureUserActive(userEntity);

        if (!encoder.matches(loginCommand.password().getValue(), userEntity.getPasswordHash())) {
            throw new PasswordValidationException("Credenciais inválidas.");
        }

        String token = tokenService.generateToken(UserMapper.toDomain(userEntity));
        saveSession(token, userEntity);
        return token;
    }

    @Override
    @Transactional
    public void deleteUser(User user, String password) {
        UserEntity userEntity = getUserEntity(user);
        ensureUserActive(userEntity);

        if (!encoder.matches(password, userEntity.getPasswordHash())) {
            throw new PasswordValidationException("A senha atual está incorreta");
        }

        userEntity.setActive(false);
        userEntity.setUpdatedAt(LocalDateTime.now());
        userRepository.save(userEntity);
        revokeSessionsForUser(userEntity.getId());
    }

    @Override
    @Transactional
    public void logout(String token) {
        String jti = tokenService.extractJti(token);
        if (jti.isBlank()) {
            return;
        }
        sessionRepository.findByJti(jti).ifPresent(session -> {
            session.setRevoked(true);
            session.setLastUsedAt(LocalDateTime.now());
            sessionRepository.save(session);
        });
    }


    private UserEntity builderUser(CreateUserCommand createUserCommand) {

        String passwordHash = encoder.encode(createUserCommand.getPassword());

        LocalDateTime now = LocalDateTime.now();
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(createUserCommand.getEmail());
        userEntity.setPasswordHash(passwordHash);
        userEntity.setActive(true);
        userEntity.setCreatedAt(now);
        userEntity.setUpdatedAt(now);
        return userEntity;
    }

    private ProfileEntity builderProfile(CreateUserCommand createUserCommand) {

        LocalDateTime now = LocalDateTime.now();
        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setPhone(createUserCommand.getPhone());
        profileEntity.setFullName(createUserCommand.getName());
        profileEntity.setCreatedAt(now);
        profileEntity.setUpdatedAt(now);
        return profileEntity;
    }

    private void ensureEmailAvailable(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new DuplicateResourceException("Usuário já existe");
        });
    }

    private void validateUserUpdateRequest(SetUserCommand setUserCommand, boolean emailChanged, boolean passwordChanged) {
        if (!emailChanged && !passwordChanged) {
            throw new IllegalArgumentException("Informe ao menos um campo para alterar");
        }
        if (!isNotBlank(setUserCommand.getNowPassword())) {
            throw new IllegalArgumentException("A senha atual é obrigatória para alterar email ou senha");
        }
    }

    private boolean shouldCreateProfile(CreateUserCommand createUserCommand) {
        return isNotBlank(createUserCommand.getName()) || isNotBlank(createUserCommand.getPhone());
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private void ensureUserActive(UserEntity userEntity) {
        if (!userEntity.isActive()) {
            throw new UserNotLog("Usuário desativado");
        }
    }

    private void validateCurrentPassword(String currentPassword, String currentPasswordHash) {
        if (!encoder.matches(currentPassword, currentPasswordHash)) {
            throw new PasswordValidationException("A senha atual está incorreta");
        }
    }

    private void revokeSessionsForUser(Long userId) {
        sessionRepository.findByUserId(userId).forEach(session -> {
            session.setRevoked(true);
            session.setLastUsedAt(LocalDateTime.now());
            sessionRepository.save(session);
        });
    }

    private void saveSession(String token, UserEntity userEntity) {
        String jti = tokenService.extractJti(token);
        if (jti.isBlank()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        SessionEntity session = new SessionEntity();
        session.setUser(userEntity);
        session.setJti(jti);
        session.setTokenHash(hashToken(token));
        session.setCreatedAt(now);
        session.setLastUsedAt(now);
        java.time.Instant expiresAt = tokenService.extractExpiration(token);
        session.setExpiresAt(expiresAt == null ? null : LocalDateTime.ofInstant(expiresAt, java.time.ZoneOffset.UTC));
        session.setRevoked(false);
        sessionRepository.save(session);
    }

    private void ensureSessionValid(String token) {
        String jti = tokenService.extractJti(token);
        if (jti.isBlank()) {
            throw new UserNotLog("Token inválido");
        }
        boolean valid = sessionRepository.findByJti(jti)
                .filter(session -> !session.isRevoked())
                .filter(session -> session.getTokenHash().equals(hashToken(token)))
                .isPresent();
        if (!valid) {
            throw new UserNotLog("Sessão expirada ou revogada");
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                String value = Integer.toHexString(0xff & b);
                if (value.length() == 1) {
                    hex.append('0');
                }
                hex.append(value);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Erro ao gerar hash do token", e);
        }
    }

    private UserEntity getUserEntity(User user) {
        if (user.getId() != null) {
            return userRepository.findById(user.getId())
                    .orElseThrow(() -> new UserNotLog("Usuário não encontrado"));
        }
        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UserNotLog("Usuário não encontrado"));
    }
}
