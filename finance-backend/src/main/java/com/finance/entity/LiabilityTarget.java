package com.finance.entity;

import com.finance.enums.LiabilityGroup;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "liability_target")
public class LiabilityTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annual_plan_id", nullable = false)
    private AnnualBalancePlan annualPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "liability_group")
    private LiabilityGroup liabilityGroup = LiabilityGroup.LOAN;

    @Column(nullable = false)
    private String name;

    @Column(name = "target_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal targetBalance;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AnnualBalancePlan getAnnualPlan() { return annualPlan; }
    public void setAnnualPlan(AnnualBalancePlan annualPlan) { this.annualPlan = annualPlan; }

    public LiabilityGroup getLiabilityGroup() { return liabilityGroup; }
    public void setLiabilityGroup(LiabilityGroup liabilityGroup) { this.liabilityGroup = liabilityGroup; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getTargetBalance() { return targetBalance; }
    public void setTargetBalance(BigDecimal targetBalance) { this.targetBalance = targetBalance; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
