package com.donato.fin_control_backend.core.usecases;

import com.donato.fin_control_backend.adapters.api.http.handler.ResourceNotFoundException;
import com.donato.fin_control_backend.core.domain.Category;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.CategoryService;
import com.donato.fin_control_backend.core.ports.outbound.CategoryRepository;
import com.donato.fin_control_backend.core.ports.outbound.UserRepository;
import com.donato.fin_control_backend.core.usecases.commands.CreateCategoryCommand;
import com.donato.fin_control_backend.core.usecases.commands.UpdateCategoryCommand;
import com.donato.fin_control_backend.infrastructure.entities.CategoryEntity;
import com.donato.fin_control_backend.infrastructure.entities.UserEntity;
import com.donato.fin_control_backend.infrastructure.mappers.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Category createCategory(CreateCategoryCommand command, User user) {
        UserEntity userEntity = resolveUser(user);

        CategoryEntity parent = null;
        if (command.getParentId() != null) {
            parent = categoryRepository.findByIdAndUserId(command.getParentId(), userEntity.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        CategoryEntity category = CategoryEntity.builder()
                .user(userEntity)
                .name(command.getName())
                .type(command.getType())
                .parent(parent)
                .color(command.getColor())
                .icon(command.getIcon())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        return CategoryMapper.toDomain(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, UpdateCategoryCommand command, User user) {
        UserEntity userEntity = resolveUser(user);
        CategoryEntity category = categoryRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (command.getName() != null) category.setName(command.getName());
        if (command.getColor() != null) category.setColor(command.getColor());
        if (command.getIcon() != null) category.setIcon(command.getIcon());

        return CategoryMapper.toDomain(categoryRepository.save(category));
    }

    @Override
    public Category getCategory(Long id, User user) {
        UserEntity userEntity = resolveUser(user);
        return CategoryMapper.toDomain(
                categoryRepository.findByIdAndUserId(id, userEntity.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found")));
    }

    @Override
    public List<Category> getCategories(User user, String type, boolean includeInactive) {
        UserEntity userEntity = resolveUser(user);
        List<CategoryEntity> entities;

        if (type != null && !includeInactive) {
            entities = categoryRepository.findAllByUserIdAndTypeAndActive(userEntity.getId(), type, true);
        } else if (type != null) {
            entities = categoryRepository.findAllByUserIdAndType(userEntity.getId(), type);
        } else if (!includeInactive) {
            entities = categoryRepository.findAllByUserIdAndActive(userEntity.getId(), true);
        } else {
            entities = categoryRepository.findAllByUserId(userEntity.getId());
        }

        return entities.stream().map(CategoryMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void archiveCategory(Long id, User user) {
        UserEntity userEntity = resolveUser(user);
        CategoryEntity category = categoryRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setActive(false);
        categoryRepository.save(category);
    }

    private UserEntity resolveUser(User user) {
        if (user.getId() != null) {
            return userRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
