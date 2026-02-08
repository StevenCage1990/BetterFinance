package com.finance.repository;

import com.finance.entity.MonthlyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyRecordRepository extends JpaRepository<MonthlyRecord, Long> {
    Optional<MonthlyRecord> findByYearAndMonth(Integer year, Integer month);
    List<MonthlyRecord> findByYearOrderByMonthAsc(Integer year);
    boolean existsByYearAndMonth(Integer year, Integer month);
    
    @Query("SELECT mr FROM MonthlyRecord mr WHERE mr.year = :year OR (mr.year = :year - 1 AND mr.month >= :month) ORDER BY mr.year DESC, mr.month DESC")
    List<MonthlyRecord> findRecentRecords(@Param("year") Integer year, @Param("month") Integer month);
    
    @Query("SELECT mr FROM MonthlyRecord mr WHERE (mr.year = :year AND mr.month < :month) OR (mr.year = :year - 1) ORDER BY mr.year DESC, mr.month DESC")
    Optional<MonthlyRecord> findPreviousRecord(@Param("year") Integer year, @Param("month") Integer month);
}
