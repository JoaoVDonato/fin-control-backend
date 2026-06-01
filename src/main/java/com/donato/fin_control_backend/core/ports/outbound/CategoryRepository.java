package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.infrastructure.entities.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    List<CategoryEntity> findAllByUserId(Long userId);

    List<CategoryEntity> findAllByUserIdAndActive(Long userId, boolean active);

    List<CategoryEntity> findAllByUserIdAndType(Long userId, String type);

    List<CategoryEntity> findAllByUserIdAndTypeAndActive(Long userId, String type, boolean active);

    Optional<CategoryEntity> findByIdAndUserId(Long id, Long userId);
}
