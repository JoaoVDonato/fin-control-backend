package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.infrastructure.entities.RecurringRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringRuleRepository extends JpaRepository<RecurringRuleEntity, Long> {

    List<RecurringRuleEntity> findAllByUserIdAndActive(Long userId, boolean active);

    List<RecurringRuleEntity> findAllByUserId(Long userId);

    Optional<RecurringRuleEntity> findByIdAndUserId(Long id, Long userId);
}
