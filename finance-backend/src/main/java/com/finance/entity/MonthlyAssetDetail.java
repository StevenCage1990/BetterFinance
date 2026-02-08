package com.finance.entity;

import com.finance.enums.AssetGroup;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "monthly_asset_detail")
public class MonthlyAssetDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_record_id", nullable = false)
    private MonthlyRecord monthlyRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_group", nullable = false)
    private AssetGroup assetGroup;

    @Column(nullable = false)
    private String name;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "return_rate", precision = 5, scale = 2)
    private BigDecimal returnRate;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MonthlyRecord getMonthlyRecord() { return monthlyRecord; }
    public void setMonthlyRecord(MonthlyRecord monthlyRecord) { this.monthlyRecord = monthlyRecord; }

    public AssetGroup getAssetGroup() { return assetGroup; }
    public void setAssetGroup(AssetGroup assetGroup) { this.assetGroup = assetGroup; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getReturnRate() { return returnRate; }
    public void setReturnRate(BigDecimal returnRate) { this.returnRate = returnRate; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
