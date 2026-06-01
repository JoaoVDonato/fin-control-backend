package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.infrastructure.entities.InvestmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentRepository extends JpaRepository<InvestmentEntity, Long> {

    List<InvestmentEntity> findAllByUserId(Long userId);

    Optional<InvestmentEntity> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT COALESCE(SUM(
                CASE m.movementType
                    WHEN 'CONTRIBUTION' THEN m.amount
                    WHEN 'YIELD' THEN m.amount
                    WHEN 'WITHDRAWAL' THEN -m.amount
                    ELSE 0
                END
            ), 0)
            FROM InvestmentMovementEntity m
            WHERE m.investment.id = :investmentId
            """)
    BigDecimal calculateBalance(@Param("investmentId") Long investmentId);

    @Query("""
            SELECT i.type, SUM(
                CASE m.movementType
                    WHEN 'CONTRIBUTION' THEN m.amount
                    WHEN 'YIELD' THEN m.amount
                    WHEN 'WITHDRAWAL' THEN -m.amount
                    ELSE 0
                END
            )
            FROM InvestmentMovementEntity m
            JOIN m.investment i
            WHERE i.user.id = :userId
            GROUP BY i.type
            """)
    List<Object[]> consolidatedByType(@Param("userId") Long userId);
}
