package com.finance.dto.request;

import com.finance.enums.AssetGroup;
import com.finance.enums.ExpenseCategory;
import com.finance.enums.IncomeType;
import com.finance.enums.LiabilityGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class AnnualPlanRequest {
    
    @NotNull(message = "年份不能为空")
    @Min(value = 2000, message = "年份不能小于2000")
    @Max(value = 2100, message = "年份不能大于2100")
    private Integer year;
    
    @Valid
    private List<AssetTargetDto> assetTargets;
    
    @Valid
    private List<LiabilityTargetDto> liabilityTargets;
    
    @Valid
    private List<AnnualIncomeDto> annualIncomes;
    
    @Valid
    private List<AnnualExpenseDto> annualExpenses;

    // Getters and Setters
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public List<AssetTargetDto> getAssetTargets() { return assetTargets; }
    public void setAssetTargets(List<AssetTargetDto> assetTargets) { this.assetTargets = assetTargets; }

    public List<LiabilityTargetDto> getLiabilityTargets() { return liabilityTargets; }
    public void setLiabilityTargets(List<LiabilityTargetDto> liabilityTargets) { this.liabilityTargets = liabilityTargets; }

    public List<AnnualIncomeDto> getAnnualIncomes() { return annualIncomes; }
    public void setAnnualIncomes(List<AnnualIncomeDto> annualIncomes) { this.annualIncomes = annualIncomes; }

    public List<AnnualExpenseDto> getAnnualExpenses() { return annualExpenses; }
    public void setAnnualExpenses(List<AnnualExpenseDto> annualExpenses) { this.annualExpenses = annualExpenses; }

    // Inner DTOs
    public static class AssetTargetDto {
        private Long id;
        
        @NotNull(message = "资产分组不能为空")
        private AssetGroup assetGroup;
        
        @NotBlank(message = "资产名称不能为空")
        private String name;
        
        @NotNull(message = "目标金额不能为空")
        @DecimalMin(value = "0", message = "目标金额不能为负")
        private BigDecimal targetAmount;
        
        private BigDecimal allocationPercentage;
        private BigDecimal expectedReturnRate;
        private Integer sortOrder = 0;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

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

    public static class LiabilityTargetDto {
        private Long id;
        
        private LiabilityGroup liabilityGroup = LiabilityGroup.LOAN;
        
        @NotBlank(message = "负债名称不能为空")
        private String name;
        
        @NotNull(message = "目标余额不能为空")
        @DecimalMin(value = "0", message = "目标余额不能为负")
        private BigDecimal targetBalance;
        
        private BigDecimal interestRate;
        private Integer sortOrder = 0;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

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

    public static class AnnualIncomeDto {
        private Long id;
        
        @NotNull(message = "收入类型不能为空")
        private IncomeType incomeType;
        
        @NotBlank(message = "收入名称不能为空")
        private String name;
        
        @NotNull(message = "金额不能为空")
        @DecimalMin(value = "0", message = "金额不能为负")
        private BigDecimal amount;
        
        private Boolean isMonthly = true;
        private String remark;
        private Integer sortOrder = 0;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

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

    public static class AnnualExpenseDto {
        private Long id;
        
        private ExpenseCategory parentCategory = ExpenseCategory.DAILY;
        
        @NotBlank(message = "支出分类不能为空")
        private String category;
        
        @NotNull(message = "预算金额不能为空")
        @DecimalMin(value = "0", message = "预算金额不能为负")
        private BigDecimal budgetAmount;
        
        private Boolean isMonthly = true;
        
        private Integer sortOrder = 0;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public ExpenseCategory getParentCategory() { return parentCategory; }
        public void setParentCategory(ExpenseCategory parentCategory) { this.parentCategory = parentCategory; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public BigDecimal getBudgetAmount() { return budgetAmount; }
        public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }

        public Boolean getIsMonthly() { return isMonthly; }
        public void setIsMonthly(Boolean isMonthly) { this.isMonthly = isMonthly; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}
