package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.infrastructure.entities.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {

    List<BudgetEntity> findAllByUserIdAndPeriodMonthAndPeriodYear(Long userId, int month, int year);

    List<BudgetEntity> findAllByUserId(Long userId);

    Optional<BudgetEntity> findByIdAndUserId(Long id, Long userId);

    Optional<BudgetEntity> findByUserIdAndCategoryIdAndPeriodMonthAndPeriodYear(
            Long userId, Long categoryId, int month, int year);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM TransactionEntity t
            WHERE t.category.id = :categoryId
              AND t.user.id = :userId
              AND t.type = 'EXPENSE'
              AND t.status = 'PAID'
              AND FUNCTION('YEAR', t.date) = :year
              AND FUNCTION('MONTH', t.date) = :month
            """)
    BigDecimal sumSpentByCategory(@Param("userId") Long userId,
                                  @Param("categoryId") Long categoryId,
                                  @Param("month") int month,
                                  @Param("year") int year);
}
