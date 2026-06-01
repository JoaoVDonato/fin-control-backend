package com.donato.fin_control_backend.adapters.api.http.controller;

import com.donato.fin_control_backend.adapters.api.http.dto.CategoryDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.CreateCategoryDTO;
import com.donato.fin_control_backend.adapters.api.http.dto.request.UpdateCategoryDTO;
import com.donato.fin_control_backend.core.domain.Category;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.CategoryService;
import com.donato.fin_control_backend.core.ports.inbound.UserService;
import com.donato.fin_control_backend.core.usecases.commands.CreateCategoryCommand;
import com.donato.fin_control_backend.core.usecases.commands.UpdateCategoryCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finControl/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<CategoryDTO> create(
            @RequestBody @Valid CreateCategoryDTO dto,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDTO(categoryService.createCategory(CreateCategoryCommand.from(dto), user)));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> list(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        return ResponseEntity.ok(
                categoryService.getCategories(user, type, includeInactive)
                        .stream().map(this::toDTO).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> get(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        return ResponseEntity.ok(toDTO(categoryService.getCategory(id, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateCategoryDTO dto,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        return ResponseEntity.ok(toDTO(categoryService.updateCategory(id, UpdateCategoryCommand.from(dto), user)));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archive(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        User user = userService.findUserByToken(extractToken(auth));
        categoryService.archiveCategory(id, user);
        return ResponseEntity.noContent().build();
    }

    private String extractToken(String auth) {
        return auth.startsWith("Bearer ") ? auth.substring(7) : auth.trim();
    }

    private CategoryDTO toDTO(Category c) {
        return CategoryDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .type(c.getType())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .parentName(c.getParent() != null ? c.getParent().getName() : null)
                .color(c.getColor())
                .icon(c.getIcon())
                .active(c.isActive())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
