package com.finance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monthly_record", uniqueConstraints = @UniqueConstraint(columnNames = {"record_year", "record_month"}))
public class MonthlyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_year", nullable = false)
    private Integer year;

    @Column(name = "record_month", nullable = false)
    private Integer month;

    @Column(name = "total_asset", precision = 15, scale = 2)
    private BigDecimal totalAsset = BigDecimal.ZERO;

    @Column(name = "total_liability", precision = 15, scale = 2)
    private BigDecimal totalLiability = BigDecimal.ZERO;

    @Column(name = "total_income", precision = 15, scale = 2)
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Column(name = "total_expense", precision = 15, scale = 2)
    private BigDecimal totalExpense = BigDecimal.ZERO;

    @Column(length = 1000)
    private String summary;

    @OneToMany(mappedBy = "monthlyRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<MonthlyAssetDetail> assetDetails = new ArrayList<>();

    @OneToMany(mappedBy = "monthlyRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<MonthlyLiabilityDetail> liabilityDetails = new ArrayList<>();

    @OneToMany(mappedBy = "monthlyRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<MonthlyIncomeDetail> incomeDetails = new ArrayList<>();

    @OneToMany(mappedBy = "monthlyRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<MonthlyExpenseDetail> expenseDetails = new ArrayList<>();

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

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public BigDecimal getTotalAsset() { return totalAsset; }
    public void setTotalAsset(BigDecimal totalAsset) { this.totalAsset = totalAsset; }

    public BigDecimal getTotalLiability() { return totalLiability; }
    public void setTotalLiability(BigDecimal totalLiability) { this.totalLiability = totalLiability; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<MonthlyAssetDetail> getAssetDetails() { return assetDetails; }
    public void setAssetDetails(List<MonthlyAssetDetail> assetDetails) { this.assetDetails = assetDetails; }

    public List<MonthlyLiabilityDetail> getLiabilityDetails() { return liabilityDetails; }
    public void setLiabilityDetails(List<MonthlyLiabilityDetail> liabilityDetails) { this.liabilityDetails = liabilityDetails; }

    public List<MonthlyIncomeDetail> getIncomeDetails() { return incomeDetails; }
    public void setIncomeDetails(List<MonthlyIncomeDetail> incomeDetails) { this.incomeDetails = incomeDetails; }

    public List<MonthlyExpenseDetail> getExpenseDetails() { return expenseDetails; }
    public void setExpenseDetails(List<MonthlyExpenseDetail> expenseDetails) { this.expenseDetails = expenseDetails; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public void addAssetDetail(MonthlyAssetDetail detail) {
        assetDetails.add(detail);
        detail.setMonthlyRecord(this);
    }

    public void addLiabilityDetail(MonthlyLiabilityDetail detail) {
        liabilityDetails.add(detail);
        detail.setMonthlyRecord(this);
    }

    public void addIncomeDetail(MonthlyIncomeDetail detail) {
        incomeDetails.add(detail);
        detail.setMonthlyRecord(this);
    }

    public void addExpenseDetail(MonthlyExpenseDetail detail) {
        expenseDetails.add(detail);
        detail.setMonthlyRecord(this);
    }

    public BigDecimal getNetWorth() {
        return totalAsset.subtract(totalLiability);
    }

    public BigDecimal getSurplus() {
        return totalIncome.subtract(totalExpense);
    }

    public void recalculateTotals() {
        this.totalAsset = assetDetails.stream()
                .map(MonthlyAssetDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalLiability = liabilityDetails.stream()
                .map(MonthlyLiabilityDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalIncome = incomeDetails.stream()
                .map(MonthlyIncomeDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalExpense = expenseDetails.stream()
                .map(MonthlyExpenseDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
