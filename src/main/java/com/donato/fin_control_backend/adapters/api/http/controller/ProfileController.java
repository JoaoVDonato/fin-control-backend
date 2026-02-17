package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.ProfileDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.CreateProfileDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.UpdateProfileDTO;
import com.donato.fin_control_backend.core.domain.Profile;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.ProfileService;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.usecases.commands.CreateProfileCommand;
import com.donato.fin_control_backend.core.usecases.commands.UpdateProfileCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finControl/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ProfileDTO> createProfile(
            @RequestBody @Valid CreateProfileDTO createProfileDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        User user = userService.findUserByToken(extractToken(authorizationHeader));
        Profile created = profileService.createProfile(CreateProfileCommand.from(createProfileDTO), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toProfileDTO(created));
    }

    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile(@RequestHeader("Authorization") String authorizationHeader) {
        User user = userService.findUserByToken(extractToken(authorizationHeader));
        Profile profile = profileService.getProfile(user);
        return ResponseEntity.ok(toProfileDTO(profile));
    }

    @PutMapping
    public ResponseEntity<ProfileDTO> updateProfile(
            @RequestBody @Valid UpdateProfileDTO updateProfileDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        User user = userService.findUserByToken(extractToken(authorizationHeader));
        Profile updated = profileService.updateProfile(UpdateProfileCommand.from(updateProfileDTO), user);
        return ResponseEntity.ok(toProfileDTO(updated));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(@RequestHeader("Authorization") String authorizationHeader) {
        User user = userService.findUserByToken(extractToken(authorizationHeader));
        profileService.deleteProfile(user);
        return ResponseEntity.noContent().build();
    }

    private String extractToken(String token){
        return token.replace("Bearer", "").trim();
    }

    private ProfileDTO toProfileDTO(Profile profile) {
        return ProfileDTO.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .displayName(profile.getDisplayName())
                .birthdate(profile.getBirthdate())
                .locale(profile.getLocale())
                .timezone(profile.getTimezone())
                .currency(profile.getCurrency())
                .phone(profile.getPhone())
                .avatarUrl(profile.getAvatarUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
