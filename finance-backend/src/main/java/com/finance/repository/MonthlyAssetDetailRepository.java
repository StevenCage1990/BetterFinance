package com.finance.repository;

import com.finance.entity.MonthlyAssetDetail;
import com.finance.enums.AssetGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MonthlyAssetDetailRepository extends JpaRepository<MonthlyAssetDetail, Long> {
    List<MonthlyAssetDetail> findByMonthlyRecordIdOrderBySortOrder(Long monthlyRecordId);
    List<MonthlyAssetDetail> findByMonthlyRecordIdAndAssetGroupOrderBySortOrder(Long monthlyRecordId, AssetGroup assetGroup);
    
    @Query("SELECT SUM(d.amount) FROM MonthlyAssetDetail d WHERE d.monthlyRecord.id = :recordId AND d.assetGroup = :group")
    BigDecimal sumAmountByRecordIdAndGroup(@Param("recordId") Long recordId, @Param("group") AssetGroup group);
}
