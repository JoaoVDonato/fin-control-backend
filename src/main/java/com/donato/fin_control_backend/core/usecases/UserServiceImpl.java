package com.donato.fin_control_backend.core.usecases;

import com.donato.fin_control_backend.adapters.api.http.handler.PasswordValidationException;
import com.donato.fin_control_backend.adapters.api.http.handler.UserNotLog;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.ports.outbound.ProfileRepository;
import com.donato.fin_control_backend.core.ports.outbound.UserRepository;
import com.donato.fin_control_backend.core.usecases.commands.CreateUserCommand;
import com.donato.fin_control_backend.core.usecases.commands.SetUserCommand;
import com.donato.fin_control_backend.infrastructure.entities.ProfileEntity;
import com.donato.fin_control_backend.infrastructure.entities.UserEntity;
import com.donato.fin_control_backend.infrastructure.mappers.UserMapper;
import com.donato.fin_control_backend.infrastructure.security.TokenJwtServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final BCryptPasswordEncoder encoder;
    private final TokenJwtServiceImpl tokenService;

    @Override
    @Transactional
    public void createUser(CreateUserCommand createUserCommand) {

        String email = createUserCommand.getEmail();

        userRepository.findByEmail(email).ifPresent(user -> {
            throw new RuntimeException("Usuário já existe");
        });

        UserEntity userEntity = builderUser(createUserCommand);
        UserEntity savedUser = userRepository.save(userEntity);

        ProfileEntity profileEntity = builderProfile(createUserCommand);
        profileEntity.setUser(savedUser);
        profileRepository.save(profileEntity);
    }

    @Transactional
    @Override
    public User alterUser(SetUserCommand setUserCommand, User user) {

        UserEntity userEntity = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UserNotLog("Usuário não está logado, verifique o token enviado."));

        boolean matches = encoder.matches(setUserCommand.getNowPassword(), userEntity.getPasswordHash());
        if (matches){
            String encode = encoder.encode(setUserCommand.getNewPassword());
            userEntity.setPasswordHash(encode);
        }else {
            throw new PasswordValidationException("A senha atual está incorreta");
        }

        return UserMapper.toDomain(userEntity);
    }

    @Override
    public User findUserByToken(String token){
        UserEntity userEntity = userRepository.findByEmail(tokenService.extractEmail(token))
                .orElseThrow(() -> new UserNotLog("Usuário não está logado, verifique o token enviado."));
        return UserMapper.toDomain(userEntity);
    }


    private UserEntity builderUser(CreateUserCommand createUserCommand) {

        String passwordHash = encoder.encode(createUserCommand.getPassword());

        LocalDateTime now = LocalDateTime.now();
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(createUserCommand.getEmail());
        userEntity.setPasswordHash(passwordHash);
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
}
