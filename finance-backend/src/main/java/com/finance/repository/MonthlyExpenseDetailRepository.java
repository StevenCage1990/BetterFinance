package com.finance.repository;

import com.finance.entity.MonthlyExpenseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MonthlyExpenseDetailRepository extends JpaRepository<MonthlyExpenseDetail, Long> {
    List<MonthlyExpenseDetail> findByMonthlyRecordIdOrderBySortOrder(Long monthlyRecordId);
    List<MonthlyExpenseDetail> findByAnnualExpenseId(Long annualExpenseId);
    
    @Query("SELECT SUM(d.amount) FROM MonthlyExpenseDetail d WHERE d.annualExpense.id = :expenseId")
    BigDecimal sumAmountByAnnualExpenseId(@Param("expenseId") Long expenseId);
    
    @Query("SELECT SUM(d.amount) FROM MonthlyExpenseDetail d WHERE d.annualExpense.id = :expenseId AND d.monthlyRecord.year = :year")
    BigDecimal sumAmountByAnnualExpenseIdAndYear(@Param("expenseId") Long expenseId, @Param("year") Integer year);
    
    @Modifying
    @Query("UPDATE MonthlyExpenseDetail d SET d.annualExpense = null WHERE d.annualExpense.id IN :expenseIds")
    void clearAnnualExpenseReferences(@Param("expenseIds") List<Long> expenseIds);
}
