package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.GoalDTO;
import com.donato.fin_control_backend.adapters.api.http.handler.ResourceNotFoundException;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.ports.outbound.GoalRepository;
import com.donato.fin_control_backend.core.ports.outbound.UserRepository;
import com.donato.fin_control_backend.infrastructure.entities.GoalEntity;
import com.donato.fin_control_backend.infrastructure.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finControl/goals")
public class GoalController {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<GoalDTO> create(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        GoalEntity goal = GoalEntity.builder()
                .user(user)
                .name((String) body.get("name"))
                .targetAmount(new BigDecimal(body.get("targetAmount").toString()))
                .currentAmount(BigDecimal.ZERO)
                .deadline(body.get("deadline") != null ? LocalDate.parse((String) body.get("deadline")) : null)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(goalRepository.save(goal)));
    }

    @GetMapping
    public ResponseEntity<List<GoalDTO>> list(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        List<GoalEntity> goals = activeOnly
                ? goalRepository.findAllByUserIdAndActive(user.getId(), true)
                : goalRepository.findAllByUserId(user.getId());
        return ResponseEntity.ok(goals.stream().map(this::toDTO).toList());
    }

    @PatchMapping("/{id}/contribute")
    public ResponseEntity<GoalDTO> contribute(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        GoalEntity goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        BigDecimal contribution = new BigDecimal(body.get("amount").toString());
        goal.setCurrentAmount(goal.getCurrentAmount().add(contribution));
        return ResponseEntity.ok(toDTO(goalRepository.save(goal)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<GoalDTO> deactivate(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        GoalEntity goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        goal.setActive(false);
        return ResponseEntity.ok(toDTO(goalRepository.save(goal)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        UserEntity user = resolve(userService.findUserByToken(extractToken(auth)));
        GoalEntity goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        goalRepository.delete(goal);
        return ResponseEntity.noContent().build();
    }

    private GoalDTO toDTO(GoalEntity g) {
        double pct = g.getTargetAmount().compareTo(BigDecimal.ZERO) == 0 ? 0 :
                g.getCurrentAmount().divide(g.getTargetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
        return GoalDTO.builder()
                .id(g.getId()).name(g.getName())
                .targetAmount(g.getTargetAmount()).currentAmount(g.getCurrentAmount())
                .progressPercent(Math.min(pct, 100.0))
                .deadline(g.getDeadline()).active(g.isActive()).createdAt(g.getCreatedAt())
                .build();
    }

    private String extractToken(String auth) {
        return auth.startsWith("Bearer ") ? auth.substring(7) : auth.trim();
    }

    private UserEntity resolve(User user) {
        if (user.getId() != null) return userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
