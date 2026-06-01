package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.infrastructure.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<TransactionEntity, Long> {

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM TransactionEntity t
            WHERE t.user.id = :userId
              AND t.type = :type
              AND t.status = 'PAID'
              AND t.date >= :dateFrom
              AND t.date <= :dateTo
            """)
    BigDecimal sumByTypeAndPeriod(@Param("userId") Long userId,
                                  @Param("type") String type,
                                  @Param("dateFrom") LocalDate dateFrom,
                                  @Param("dateTo") LocalDate dateTo);

    @Query("""
            SELECT t
            FROM TransactionEntity t
            WHERE t.user.id = :userId
            ORDER BY t.date DESC, t.createdAt DESC
            """)
    List<TransactionEntity> findRecentByUserId(@Param("userId") Long userId,
                                               org.springframework.data.domain.Pageable pageable);

    @Query("""
            SELECT t.category.id, t.category.name, SUM(t.amount)
            FROM TransactionEntity t
            WHERE t.user.id = :userId
              AND t.type = 'EXPENSE'
              AND t.status = 'PAID'
              AND t.date >= :dateFrom
              AND t.date <= :dateTo
              AND t.category IS NOT NULL
            GROUP BY t.category.id, t.category.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> sumExpenseByCategory(@Param("userId") Long userId,
                                        @Param("dateFrom") LocalDate dateFrom,
                                        @Param("dateTo") LocalDate dateTo);

    @Query("""
            SELECT EXTRACT(YEAR FROM t.date), EXTRACT(MONTH FROM t.date), t.type, SUM(t.amount)
            FROM TransactionEntity t
            WHERE t.user.id = :userId
              AND t.status = 'PAID'
              AND t.type IN ('INCOME', 'EXPENSE')
              AND t.date >= :dateFrom
            GROUP BY EXTRACT(YEAR FROM t.date), EXTRACT(MONTH FROM t.date), t.type
            ORDER BY EXTRACT(YEAR FROM t.date), EXTRACT(MONTH FROM t.date)
            """)
    List<Object[]> monthlyTrend(@Param("userId") Long userId,
                                @Param("dateFrom") LocalDate dateFrom);
}
