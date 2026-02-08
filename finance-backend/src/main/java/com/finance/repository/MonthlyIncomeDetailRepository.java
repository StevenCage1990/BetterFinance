package com.finance.repository;

import com.finance.entity.MonthlyIncomeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyIncomeDetailRepository extends JpaRepository<MonthlyIncomeDetail, Long> {
    List<MonthlyIncomeDetail> findByMonthlyRecordIdOrderBySortOrder(Long monthlyRecordId);
}
