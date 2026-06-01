package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.AccountDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.CreateAccountDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.UpdateAccountDTO;
import com.donato.fin_control_backend.core.domain.Account;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.AccountService;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.usecases.commands.CreateAccountCommand;
import com.donato.fin_control_backend.core.usecases.commands.UpdateAccountCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finControl/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<AccountDTO> create(
            @RequestBody @Valid CreateAccountDTO dto,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        Account account = accountService.createAccount(CreateAccountCommand.from(dto), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(account));
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> list(
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        List<AccountDTO> accounts = accountService.getAccounts(user, includeArchived)
                .stream().map(this::toDTO).toList();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> get(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        return ResponseEntity.ok(toDTO(accountService.getAccount(id, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateAccountDTO dto,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        Account updated = accountService.updateAccount(id, UpdateAccountCommand.from(dto), user);
        return ResponseEntity.ok(toDTO(updated));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archive(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        accountService.archiveAccount(id, user);
        return ResponseEntity.noContent().build();
    }

    private String extractToken(String auth) {
        return auth.startsWith("Bearer ") ? auth.substring(7) : auth.trim();
    }

    private AccountDTO toDTO(Account a) {
        return AccountDTO.builder()
                .id(a.getId())
                .name(a.getName())
                .type(a.getType())
                .balance(a.getBalance())
                .initialBalance(a.getInitialBalance())
                .currency(a.getCurrency())
                .archived(a.isArchived())
                .institution(a.getInstitution())
                .color(a.getColor())
                .icon(a.getIcon())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
