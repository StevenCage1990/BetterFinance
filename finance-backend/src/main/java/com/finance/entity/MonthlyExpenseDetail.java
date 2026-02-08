package com.finance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "monthly_expense_detail")
public class MonthlyExpenseDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_record_id", nullable = false)
    private MonthlyRecord monthlyRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annual_expense_id")
    private AnnualExpense annualExpense;

    @Column(nullable = false)
    private String name;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(length = 500)
    private String detail;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MonthlyRecord getMonthlyRecord() { return monthlyRecord; }
    public void setMonthlyRecord(MonthlyRecord monthlyRecord) { this.monthlyRecord = monthlyRecord; }

    public AnnualExpense getAnnualExpense() { return annualExpense; }
    public void setAnnualExpense(AnnualExpense annualExpense) { this.annualExpense = annualExpense; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
