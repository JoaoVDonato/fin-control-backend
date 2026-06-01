package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.request.CreateUserDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.DeleteUserDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.LoginDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.SetUserDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.UserDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.response.ApiResponse;
import com.donato.fin_control_backend.adapters.api.http.dto.response.LoginResponse;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.usecases.commands.CreateUserCommand;
import com.donato.fin_control_backend.core.usecases.commands.LoginCommand;
import com.donato.fin_control_backend.core.usecases.commands.SetUserCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finControl/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginDTO loginDTO) {
        LoginCommand loginCommand = LoginCommand.from(loginDTO);
        String token = userService.login(loginCommand);
        LoginResponse loginResponse = new LoginResponse(token);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping
    public ResponseEntity<UserDTO> getUser(@RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        User user = userService.findUserByToken(token);
        return ResponseEntity.ok(toUserDTO(user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody @Valid CreateUserDTO createUserDTO) {

        CreateUserCommand createUserCommand = CreateUserCommand.from(createUserDTO);
        userService.createUser(createUserCommand);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<UserDTO> alterUser(
            @RequestBody @Valid SetUserDTO setUserDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        User userByToken = userService.findUserByToken(token);
        SetUserCommand setUserCommand = SetUserCommand.from(setUserDTO);
        User updatedUser = userService.alterUser(setUserCommand, userByToken);
        return ResponseEntity.ok(toUserDTO(updatedUser));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        userService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(
            @RequestBody @Valid DeleteUserDTO deleteUserDTO,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        User user = userService.findUserByToken(token);
        userService.deleteUser(user, deleteUserDTO.getPassword());
        return ResponseEntity.noContent().build();
    }

    private String extractToken(String token){
        return token.startsWith("Bearer ") ? token.substring(7) : token.trim();
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
