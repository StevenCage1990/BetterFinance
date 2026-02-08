package com.finance.repository;

import com.finance.entity.LiabilityTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiabilityTargetRepository extends JpaRepository<LiabilityTarget, Long> {
    List<LiabilityTarget> findByAnnualPlanIdOrderBySortOrder(Long annualPlanId);
}
