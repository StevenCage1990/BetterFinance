package com.finance.repository;

import com.finance.entity.MonthlyLiabilityDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyLiabilityDetailRepository extends JpaRepository<MonthlyLiabilityDetail, Long> {
    List<MonthlyLiabilityDetail> findByMonthlyRecordIdOrderBySortOrder(Long monthlyRecordId);
}
