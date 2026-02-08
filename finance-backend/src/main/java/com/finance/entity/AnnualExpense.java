package com.finance.entity;

import com.finance.enums.ExpenseCategory;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "annual_expense")
public class AnnualExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annual_plan_id", nullable = false)
    private AnnualBalancePlan annualPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent_category")
    private ExpenseCategory parentCategory = ExpenseCategory.DAILY;

    @Column(nullable = false)
    private String category;

    @Column(name = "budget_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal budgetAmount;

    @Column(name = "is_monthly")
    private Boolean isMonthly = true;

    @Column(name = "spent_amount", precision = 15, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AnnualBalancePlan getAnnualPlan() { return annualPlan; }
    public void setAnnualPlan(AnnualBalancePlan annualPlan) { this.annualPlan = annualPlan; }

    public ExpenseCategory getParentCategory() { return parentCategory; }
    public void setParentCategory(ExpenseCategory parentCategory) { this.parentCategory = parentCategory; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }

    public Boolean getIsMonthly() { return isMonthly; }
    public void setIsMonthly(Boolean isMonthly) { this.isMonthly = isMonthly; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    // Helper methods
    public BigDecimal getRemainingAmount() {
        return budgetAmount.subtract(spentAmount != null ? spentAmount : BigDecimal.ZERO);
    }

    public double getExecutionRate() {
        if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return spentAmount.divide(budgetAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }
}
