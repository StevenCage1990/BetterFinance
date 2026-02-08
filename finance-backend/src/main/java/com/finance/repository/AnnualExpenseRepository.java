package com.finance.repository;

import com.finance.entity.AnnualExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnualExpenseRepository extends JpaRepository<AnnualExpense, Long> {
    List<AnnualExpense> findByAnnualPlanIdOrderBySortOrder(Long annualPlanId);
}
