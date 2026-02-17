package com.donato.fin_control_backend.core.usecases;

import com.donato.fin_control_backend.core.domain.Profile;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.ProfileService;
import com.donato.fin_control_backend.core.ports.outbound.ProfileRepository;
import com.donato.fin_control_backend.core.ports.outbound.UserRepository;
import com.donato.fin_control_backend.core.usecases.commands.CreateProfileCommand;
import com.donato.fin_control_backend.core.usecases.commands.UpdateProfileCommand;
import com.donato.fin_control_backend.infrastructure.entities.ProfileEntity;
import com.donato.fin_control_backend.infrastructure.entities.UserEntity;
import com.donato.fin_control_backend.infrastructure.mappers.ProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Profile createProfile(CreateProfileCommand command, User user) {
        UserEntity userEntity = getUserEntity(user);

        profileRepository.findByUserId(userEntity.getId()).ifPresent(existing -> {
            throw new RuntimeException("Perfil já existe");
        });

        LocalDateTime now = LocalDateTime.now();
        ProfileEntity profile = new ProfileEntity();
        profile.setUser(userEntity);
        profile.setFullName(command.getFullName());
        profile.setDisplayName(command.getDisplayName());
        profile.setBirthdate(command.getBirthdate());
        profile.setLocale(command.getLocale());
        profile.setTimezone(command.getTimezone());
        profile.setCurrency(command.getCurrency());
        profile.setPhone(command.getPhone());
        profile.setAvatarUrl(command.getAvatarUrl());
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);

        return ProfileMapper.toDomain(profileRepository.save(profile));
    }

    @Override
    @Transactional
    public Profile updateProfile(UpdateProfileCommand command, User user) {
        UserEntity userEntity = getUserEntity(user);
        ProfileEntity profile = profileRepository.findByUserId(userEntity.getId())
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        if (command.getFullName() != null) {
            profile.setFullName(command.getFullName());
        }
        if (command.getDisplayName() != null) {
            profile.setDisplayName(command.getDisplayName());
        }
        if (command.getBirthdate() != null) {
            profile.setBirthdate(command.getBirthdate());
        }
        if (command.getLocale() != null) {
            profile.setLocale(command.getLocale());
        }
        if (command.getTimezone() != null) {
            profile.setTimezone(command.getTimezone());
        }
        if (command.getCurrency() != null) {
            profile.setCurrency(command.getCurrency());
        }
        if (command.getPhone() != null) {
            profile.setPhone(command.getPhone());
        }
        if (command.getAvatarUrl() != null) {
            profile.setAvatarUrl(command.getAvatarUrl());
        }

        profile.setUpdatedAt(LocalDateTime.now());
        return ProfileMapper.toDomain(profileRepository.save(profile));
    }

    @Override
    public Profile getProfile(User user) {
        UserEntity userEntity = getUserEntity(user);
        ProfileEntity profile = profileRepository.findByUserId(userEntity.getId())
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        return ProfileMapper.toDomain(profile);
    }

    @Override
    @Transactional
    public void deleteProfile(User user) {
        UserEntity userEntity = getUserEntity(user);
        profileRepository.findByUserId(userEntity.getId()).ifPresent(profileRepository::delete);
    }

    private UserEntity getUserEntity(User user) {
        if (user.getId() != null) {
            return userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        }
        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
