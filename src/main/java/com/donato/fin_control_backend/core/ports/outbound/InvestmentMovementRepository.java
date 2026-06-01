package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.infrastructure.entities.InvestmentMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentMovementRepository extends JpaRepository<InvestmentMovementEntity, Long> {

    List<InvestmentMovementEntity> findAllByInvestmentIdOrderByDateDesc(Long investmentId);

    Optional<InvestmentMovementEntity> findByIdAndInvestmentUserId(Long id, Long userId);
}
