package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.UserDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.LoginDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.response.ApiResponse;
import com.donato.fin_control_backend.adapters.api.http.dto.response.LoginResponse;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.usecases.commands.LoginCommand;
import com.donato.fin_control_backend.infrastructure.security.TokenServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.token.TokenService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(name = "/finControl/user")
public class UserController {

    private AuthenticationManager authenticationManager;
    private TokenServiceImpl tokenService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginDTO loginDTO) {
        LoginCommand loginCommand = LoginCommand.from(loginDTO);
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(
                loginCommand.getEmail(),
                loginCommand.getPassword()
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
    public User createUser() {
        return null;
    }

    @PutMapping
    public User alterUser() {
        return null;
    }

    @DeleteMapping
    public void deleteUser() {
    }
}
