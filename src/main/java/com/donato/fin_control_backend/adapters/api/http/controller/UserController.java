package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.request.CreateUserDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.LoginDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.SetUserDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.response.ApiResponse;
import com.donato.fin_control_backend.adapters.api.http.dto.response.LoginResponse;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.usecases.commands.CreateUserCommand;
import com.donato.fin_control_backend.core.usecases.commands.LoginCommand;
import com.donato.fin_control_backend.core.usecases.commands.SetUserCommand;
import com.donato.fin_control_backend.infrastructure.security.TokenJwtServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(name = "/finControl/user")
public class UserController {

    private AuthenticationManager authenticationManager;
    private TokenJwtServiceImpl tokenService;
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginDTO loginDTO) {
        LoginCommand loginCommand = LoginCommand.from(loginDTO);
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(
                loginCommand.email(),
                loginCommand.password()
        );
        Authentication authenticate = this.authenticationManager.authenticate(usernamePassword);
        User user = (User) authenticate.getPrincipal();
        String token = tokenService.generateToken(user); //check user
        LoginResponse loginResponse = new LoginResponse(token);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping
    public User getUser() { return null;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody @Valid CreateUserDTO createUserDTO) {

        CreateUserCommand createUserCommand = CreateUserCommand.from(createUserDTO);
        userService.createUser(createUserCommand);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public User alterUser(
            @RequestBody @Valid SetUserDTO setUserDTO,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = extractToken(authorizationHeader);
        User userByToken = userService.findUserByToken(token);
        SetUserCommand setUserCommand = SetUserCommand.from(setUserDTO);







        return null;
    }

    @DeleteMapping
    public void deleteUser() {
    }

    private String extractToken(String token){
        return token.replace("Bearer", "").trim();
    }
}
