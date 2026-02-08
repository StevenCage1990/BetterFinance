package com.finance.repository;

import com.finance.entity.AnnualBalancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnualBalancePlanRepository extends JpaRepository<AnnualBalancePlan, Long> {
    Optional<AnnualBalancePlan> findByYear(Integer year);
    boolean existsByYear(Integer year);
}
