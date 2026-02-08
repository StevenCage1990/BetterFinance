package com.finance.dto.response;

import com.finance.entity.*;
import com.finance.enums.AssetGroup;
import com.finance.enums.ExpenseCategory;
import com.finance.enums.IncomeType;
import com.finance.enums.LiabilityGroup;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AnnualPlanResponse {
    private Long id;
    private Integer year;
    private BigDecimal monthlySurplus;
    private BigDecimal annualSurplus;
    private List<AssetTargetVo> assetTargets;
    private List<LiabilityTargetVo> liabilityTargets;
    private List<AnnualIncomeVo> annualIncomes;
    private List<AnnualExpenseVo> annualExpenses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Static factory method
    public static AnnualPlanResponse fromEntity(AnnualBalancePlan entity) {
        AnnualPlanResponse response = new AnnualPlanResponse();
        response.setId(entity.getId());
        response.setYear(entity.getYear());
        response.setMonthlySurplus(entity.getMonthlySurplus());
        response.setAnnualSurplus(entity.getAnnualSurplus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        
        response.setAssetTargets(entity.getAssetTargets().stream()
                .map(AssetTargetVo::fromEntity)
                .collect(Collectors.toList()));
        
        response.setLiabilityTargets(entity.getLiabilityTargets().stream()
                .map(LiabilityTargetVo::fromEntity)
                .collect(Collectors.toList()));
        
        response.setAnnualIncomes(entity.getAnnualIncomes().stream()
                .map(AnnualIncomeVo::fromEntity)
                .collect(Collectors.toList()));
        
        response.setAnnualExpenses(entity.getAnnualExpenses().stream()
                .map(AnnualExpenseVo::fromEntity)
                .collect(Collectors.toList()));
        
        return response;
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

    public List<AssetTargetVo> getAssetTargets() { return assetTargets; }
    public void setAssetTargets(List<AssetTargetVo> assetTargets) { this.assetTargets = assetTargets; }

    public List<LiabilityTargetVo> getLiabilityTargets() { return liabilityTargets; }
    public void setLiabilityTargets(List<LiabilityTargetVo> liabilityTargets) { this.liabilityTargets = liabilityTargets; }

    public List<AnnualIncomeVo> getAnnualIncomes() { return annualIncomes; }
    public void setAnnualIncomes(List<AnnualIncomeVo> annualIncomes) { this.annualIncomes = annualIncomes; }

    public List<AnnualExpenseVo> getAnnualExpenses() { return annualExpenses; }
    public void setAnnualExpenses(List<AnnualExpenseVo> annualExpenses) { this.annualExpenses = annualExpenses; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Inner VOs
    public static class AssetTargetVo {
        private Long id;
        private AssetGroup assetGroup;
        private String assetGroupLabel;
        private String name;
        private BigDecimal targetAmount;
        private BigDecimal allocationPercentage;
        private BigDecimal expectedReturnRate;
        private Integer sortOrder;

        public static AssetTargetVo fromEntity(AssetTarget entity) {
            AssetTargetVo vo = new AssetTargetVo();
            vo.setId(entity.getId());
            vo.setAssetGroup(entity.getAssetGroup());
            vo.setAssetGroupLabel(entity.getAssetGroup().getLabel());
            vo.setName(entity.getName());
            vo.setTargetAmount(entity.getTargetAmount());
            vo.setAllocationPercentage(entity.getAllocationPercentage());
            vo.setExpectedReturnRate(entity.getExpectedReturnRate());
            vo.setSortOrder(entity.getSortOrder());
            return vo;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public AssetGroup getAssetGroup() { return assetGroup; }
        public void setAssetGroup(AssetGroup assetGroup) { this.assetGroup = assetGroup; }

        public String getAssetGroupLabel() { return assetGroupLabel; }
        public void setAssetGroupLabel(String assetGroupLabel) { this.assetGroupLabel = assetGroupLabel; }

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

    public static class LiabilityTargetVo {
        private Long id;
        private LiabilityGroup liabilityGroup;
        private String liabilityGroupLabel;
        private String name;
        private BigDecimal targetBalance;
        private BigDecimal interestRate;
        private Integer sortOrder;

        public static LiabilityTargetVo fromEntity(LiabilityTarget entity) {
            LiabilityTargetVo vo = new LiabilityTargetVo();
            vo.setId(entity.getId());
            vo.setLiabilityGroup(entity.getLiabilityGroup());
            vo.setLiabilityGroupLabel(entity.getLiabilityGroup() != null ? entity.getLiabilityGroup().getLabel() : null);
            vo.setName(entity.getName());
            vo.setTargetBalance(entity.getTargetBalance());
            vo.setInterestRate(entity.getInterestRate());
            vo.setSortOrder(entity.getSortOrder());
            return vo;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public LiabilityGroup getLiabilityGroup() { return liabilityGroup; }
        public void setLiabilityGroup(LiabilityGroup liabilityGroup) { this.liabilityGroup = liabilityGroup; }

        public String getLiabilityGroupLabel() { return liabilityGroupLabel; }
        public void setLiabilityGroupLabel(String liabilityGroupLabel) { this.liabilityGroupLabel = liabilityGroupLabel; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getTargetBalance() { return targetBalance; }
        public void setTargetBalance(BigDecimal targetBalance) { this.targetBalance = targetBalance; }

        public BigDecimal getInterestRate() { return interestRate; }
        public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }

    public static class AnnualIncomeVo {
        private Long id;
        private IncomeType incomeType;
        private String incomeTypeLabel;
        private String name;
        private BigDecimal amount;
        private Boolean isMonthly;
        private String remark;
        private Integer sortOrder;

        public static AnnualIncomeVo fromEntity(AnnualIncome entity) {
            AnnualIncomeVo vo = new AnnualIncomeVo();
            vo.setId(entity.getId());
            vo.setIncomeType(entity.getIncomeType());
            vo.setIncomeTypeLabel(entity.getIncomeType().getLabel());
            vo.setName(entity.getName());
            vo.setAmount(entity.getAmount());
            vo.setIsMonthly(entity.getIsMonthly());
            vo.setRemark(entity.getRemark());
            vo.setSortOrder(entity.getSortOrder());
            return vo;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public IncomeType getIncomeType() { return incomeType; }
        public void setIncomeType(IncomeType incomeType) { this.incomeType = incomeType; }

        public String getIncomeTypeLabel() { return incomeTypeLabel; }
        public void setIncomeTypeLabel(String incomeTypeLabel) { this.incomeTypeLabel = incomeTypeLabel; }

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

    public static class AnnualExpenseVo {
        private Long id;
        private ExpenseCategory parentCategory;
        private String parentCategoryLabel;
        private String category;
        private BigDecimal budgetAmount;
        private Boolean isMonthly;
        private BigDecimal spentAmount;
        private BigDecimal remainingAmount;
        private Double executionRate;
        private Integer sortOrder;

        public static AnnualExpenseVo fromEntity(AnnualExpense entity) {
            AnnualExpenseVo vo = new AnnualExpenseVo();
            vo.setId(entity.getId());
            vo.setParentCategory(entity.getParentCategory());
            vo.setParentCategoryLabel(entity.getParentCategory() != null ? entity.getParentCategory().getLabel() : null);
            vo.setCategory(entity.getCategory());
            vo.setBudgetAmount(entity.getBudgetAmount());
            vo.setIsMonthly(entity.getIsMonthly());
            vo.setSpentAmount(entity.getSpentAmount());
            vo.setRemainingAmount(entity.getRemainingAmount());
            vo.setExecutionRate(entity.getExecutionRate());
            vo.setSortOrder(entity.getSortOrder());
            return vo;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public ExpenseCategory getParentCategory() { return parentCategory; }
        public void setParentCategory(ExpenseCategory parentCategory) { this.parentCategory = parentCategory; }

        public String getParentCategoryLabel() { return parentCategoryLabel; }
        public void setParentCategoryLabel(String parentCategoryLabel) { this.parentCategoryLabel = parentCategoryLabel; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public BigDecimal getBudgetAmount() { return budgetAmount; }
        public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }

        public Boolean getIsMonthly() { return isMonthly; }
        public void setIsMonthly(Boolean isMonthly) { this.isMonthly = isMonthly; }

        public BigDecimal getSpentAmount() { return spentAmount; }
        public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

        public BigDecimal getRemainingAmount() { return remainingAmount; }
        public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

        public Double getExecutionRate() { return executionRate; }
        public void setExecutionRate(Double executionRate) { this.executionRate = executionRate; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}
