package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.infrastructure.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<TransactionEntity, Long> {

    @Query("""
            SELECT EXTRACT(YEAR FROM t.date), EXTRACT(MONTH FROM t.date), t.type, SUM(t.amount)
            FROM TransactionEntity t
            WHERE t.user.id = :userId
              AND t.status = 'PAID'
              AND t.type IN ('INCOME', 'EXPENSE')
              AND (:dateFrom IS NULL OR t.date >= :dateFrom)
              AND (:dateTo IS NULL OR t.date <= :dateTo)
            GROUP BY EXTRACT(YEAR FROM t.date), EXTRACT(MONTH FROM t.date), t.type
            ORDER BY EXTRACT(YEAR FROM t.date), EXTRACT(MONTH FROM t.date)
            """)
    List<Object[]> monthlyReport(@Param("userId") Long userId,
                                 @Param("dateFrom") LocalDate dateFrom,
                                 @Param("dateTo") LocalDate dateTo);

    @Query("""
            SELECT t.category.id, t.category.name, t.type, SUM(t.amount), COUNT(t)
            FROM TransactionEntity t
            WHERE t.user.id = :userId
              AND t.status = 'PAID'
              AND t.category IS NOT NULL
              AND (:dateFrom IS NULL OR t.date >= :dateFrom)
              AND (:dateTo IS NULL OR t.date <= :dateTo)
            GROUP BY t.category.id, t.category.name, t.type
            ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> reportByCategory(@Param("userId") Long userId,
                                    @Param("dateFrom") LocalDate dateFrom,
                                    @Param("dateTo") LocalDate dateTo);

    @Query("""
            SELECT t.account.id, t.account.name, t.type, SUM(t.amount), COUNT(t)
            FROM TransactionEntity t
            WHERE t.user.id = :userId
              AND t.status = 'PAID'
              AND t.account IS NOT NULL
              AND (:dateFrom IS NULL OR t.date >= :dateFrom)
              AND (:dateTo IS NULL OR t.date <= :dateTo)
            GROUP BY t.account.id, t.account.name, t.type
            ORDER BY t.account.name
            """)
    List<Object[]> reportByAccount(@Param("userId") Long userId,
                                   @Param("dateFrom") LocalDate dateFrom,
                                   @Param("dateTo") LocalDate dateTo);

    @Query("""
            SELECT t
            FROM TransactionEntity t
            WHERE t.user.id = :userId
              AND (:accountId IS NULL OR t.account.id = :accountId)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:type IS NULL OR t.type = :type)
              AND (:status IS NULL OR t.status = :status)
              AND (:dateFrom IS NULL OR t.date >= :dateFrom)
              AND (:dateTo IS NULL OR t.date <= :dateTo)
            ORDER BY t.date DESC
            """)
    List<TransactionEntity> findAllForExport(@Param("userId") Long userId,
                                             @Param("accountId") Long accountId,
                                             @Param("categoryId") Long categoryId,
                                             @Param("type") String type,
                                             @Param("status") String status,
                                             @Param("dateFrom") LocalDate dateFrom,
                                             @Param("dateTo") LocalDate dateTo);
}
