package com.donato.fin_control_backend.core.ports.inbound;

import com.donato.fin_control_backend.core.domain.Category;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.usecases.commands.CreateCategoryCommand;
import com.donato.fin_control_backend.core.usecases.commands.UpdateCategoryCommand;

import java.util.List;

public interface CategoryService {

    Category createCategory(CreateCategoryCommand command, User user);

    Category updateCategory(Long id, UpdateCategoryCommand command, User user);

    Category getCategory(Long id, User user);

    List<Category> getCategories(User user, String type, boolean includeInactive);

    void archiveCategory(Long id, User user);
}
