package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.TagDTO;
import com.donato.fin_control_backend.core.domain.Tag;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.TagService;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finControl/tags")
public class TagController {

    private final TagService tagService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<TagDTO> create(
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String auth) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        User user = userService.findUserByToken(extractToken(auth));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(tagService.createTag(name, user)));
    }

    @GetMapping
    public ResponseEntity<List<TagDTO>> list(@RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        return ResponseEntity.ok(tagService.getTags(user).stream().map(this::toDTO).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> get(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        return ResponseEntity.ok(toDTO(tagService.getTag(id, user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        tagService.deleteTag(id, user);
        return ResponseEntity.noContent().build();
    }

    private String extractToken(String auth) {
        return auth.startsWith("Bearer ") ? auth.substring(7) : auth.trim();
    }

    private TagDTO toDTO(Tag t) {
        return TagDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
