package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.infrastructure.entities.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<GoalEntity, Long> {

    List<GoalEntity> findAllByUserIdAndActive(Long userId, boolean active);

    List<GoalEntity> findAllByUserId(Long userId);

    Optional<GoalEntity> findByIdAndUserId(Long id, Long userId);
}
