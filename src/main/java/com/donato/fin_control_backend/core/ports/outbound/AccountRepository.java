package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.infrastructure.entities.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    List<AccountEntity> findAllByUserIdAndArchived(Long userId, boolean archived);

    List<AccountEntity> findAllByUserId(Long userId);

    Optional<AccountEntity> findByIdAndUserId(Long id, Long userId);
}
