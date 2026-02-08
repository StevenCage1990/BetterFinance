package com.finance.repository;

import com.finance.entity.AssetTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetTargetRepository extends JpaRepository<AssetTarget, Long> {
    List<AssetTarget> findByAnnualPlanIdOrderBySortOrder(Long annualPlanId);
}
