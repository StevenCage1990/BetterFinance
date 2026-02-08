package com.finance.repository;

import com.finance.entity.AnnualIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnualIncomeRepository extends JpaRepository<AnnualIncome, Long> {
    List<AnnualIncome> findByAnnualPlanIdOrderBySortOrder(Long annualPlanId);
    List<AnnualIncome> findByAnnualPlanIdAndIsMonthlyTrue(Long annualPlanId);
}
