package com.finance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "annual_balance_plan", uniqueConstraints = @UniqueConstraint(columnNames = "plan_year"))
public class AnnualBalancePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_year", nullable = false, unique = true)
    private Integer year;

    @Column(precision = 15, scale = 2)
    private BigDecimal monthlySurplus;

    @Column(precision = 15, scale = 2)
    private BigDecimal annualSurplus;

    @OneToMany(mappedBy = "annualPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<AssetTarget> assetTargets = new ArrayList<>();

    @OneToMany(mappedBy = "annualPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<LiabilityTarget> liabilityTargets = new ArrayList<>();

    @OneToMany(mappedBy = "annualPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<AnnualIncome> annualIncomes = new ArrayList<>();

    @OneToMany(mappedBy = "annualPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<AnnualExpense> annualExpenses = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public BigDecimal getMonthlySurplus() { return monthlySurplus; }
    public void setMonthlySurplus(BigDecimal monthlySurplus) { this.monthlySurplus = monthlySurplus; }

    public BigDecimal getAnnualSurplus() { return annualSurplus; }
    public void setAnnualSurplus(BigDecimal annualSurplus) { this.annualSurplus = annualSurplus; }

    public List<AssetTarget> getAssetTargets() { return assetTargets; }
    public void setAssetTargets(List<AssetTarget> assetTargets) { this.assetTargets = assetTargets; }

    public List<LiabilityTarget> getLiabilityTargets() { return liabilityTargets; }
    public void setLiabilityTargets(List<LiabilityTarget> liabilityTargets) { this.liabilityTargets = liabilityTargets; }

    public List<AnnualIncome> getAnnualIncomes() { return annualIncomes; }
    public void setAnnualIncomes(List<AnnualIncome> annualIncomes) { this.annualIncomes = annualIncomes; }

    public List<AnnualExpense> getAnnualExpenses() { return annualExpenses; }
    public void setAnnualExpenses(List<AnnualExpense> annualExpenses) { this.annualExpenses = annualExpenses; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public void addAssetTarget(AssetTarget target) {
        assetTargets.add(target);
        target.setAnnualPlan(this);
    }

    public void addLiabilityTarget(LiabilityTarget target) {
        liabilityTargets.add(target);
        target.setAnnualPlan(this);
    }

    public void addAnnualIncome(AnnualIncome income) {
        annualIncomes.add(income);
        income.setAnnualPlan(this);
    }

    public void addAnnualExpense(AnnualExpense expense) {
        annualExpenses.add(expense);
        expense.setAnnualPlan(this);
    }
}
