package com.finance.entity;

import com.finance.enums.AssetGroup;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "asset_target")
public class AssetTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annual_plan_id", nullable = false)
    private AnnualBalancePlan annualPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_group", nullable = false)
    private AssetGroup assetGroup;

    @Column(nullable = false)
    private String name;

    @Column(name = "target_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "allocation_percentage", precision = 5, scale = 2)
    private BigDecimal allocationPercentage;

    @Column(name = "expected_return_rate", precision = 5, scale = 2)
    private BigDecimal expectedReturnRate;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AnnualBalancePlan getAnnualPlan() { return annualPlan; }
    public void setAnnualPlan(AnnualBalancePlan annualPlan) { this.annualPlan = annualPlan; }

    public AssetGroup getAssetGroup() { return assetGroup; }
    public void setAssetGroup(AssetGroup assetGroup) { this.assetGroup = assetGroup; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

    public BigDecimal getAllocationPercentage() { return allocationPercentage; }
    public void setAllocationPercentage(BigDecimal allocationPercentage) { this.allocationPercentage = allocationPercentage; }

    public BigDecimal getExpectedReturnRate() { return expectedReturnRate; }
    public void setExpectedReturnRate(BigDecimal expectedReturnRate) { this.expectedReturnRate = expectedReturnRate; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
