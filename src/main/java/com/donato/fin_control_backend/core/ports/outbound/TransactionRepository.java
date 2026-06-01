package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.infrastructure.entities.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    Optional<TransactionEntity> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT t FROM TransactionEntity t
            WHERE t.user.id = :userId
              AND (:accountId IS NULL OR t.account.id = :accountId)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:type IS NULL OR t.type = :type)
              AND (:status IS NULL OR t.status = :status)
              AND (:dateFrom IS NULL OR t.date >= :dateFrom)
              AND (:dateTo IS NULL OR t.date <= :dateTo)
              AND (:search IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY t.date DESC, t.createdAt DESC
            """)
    Page<TransactionEntity> findByFilters(
            @Param("userId") Long userId,
            @Param("accountId") Long accountId,
            @Param("categoryId") Long categoryId,
            @Param("type") String type,
            @Param("status") String status,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("search") String search,
            Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(
                CASE
                    WHEN t.type = 'INCOME' THEN t.amount
                    WHEN t.type = 'EXPENSE' THEN -t.amount
                    ELSE 0
                END
            ), 0)
            FROM TransactionEntity t
            WHERE t.account.id = :accountId
              AND t.status = 'PAID'
            """)
    BigDecimal calculateBalanceOffset(@Param("accountId") Long accountId);
}
