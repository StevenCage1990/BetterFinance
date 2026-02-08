package com.finance.entity;

import com.finance.enums.IncomeType;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "annual_income")
public class AnnualIncome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annual_plan_id", nullable = false)
    private AnnualBalancePlan annualPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_type", nullable = false)
    private IncomeType incomeType;

    @Column(nullable = false)
    private String name;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "is_monthly", nullable = false)
    private Boolean isMonthly = true;

    @Column(length = 200)
    private String remark;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AnnualBalancePlan getAnnualPlan() { return annualPlan; }
    public void setAnnualPlan(AnnualBalancePlan annualPlan) { this.annualPlan = annualPlan; }

    public IncomeType getIncomeType() { return incomeType; }
    public void setIncomeType(IncomeType incomeType) { this.incomeType = incomeType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Boolean getIsMonthly() { return isMonthly; }
    public void setIsMonthly(Boolean isMonthly) { this.isMonthly = isMonthly; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
