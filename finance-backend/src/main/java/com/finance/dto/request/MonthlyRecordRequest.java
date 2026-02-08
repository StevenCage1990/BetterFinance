package com.finance.dto.request;

import com.finance.enums.AssetGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class MonthlyRecordRequest {
    
    @NotNull(message = "年份不能为空")
    private Integer year;
    
    @NotNull(message = "月份不能为空")
    @Min(value = 1, message = "月份必须在1-12之间")
    @Max(value = 12, message = "月份必须在1-12之间")
    private Integer month;
    
    private String summary;
    
    @Valid
    private List<AssetDetailDto> assetDetails;
    
    @Valid
    private List<LiabilityDetailDto> liabilityDetails;
    
    @Valid
    private List<IncomeDetailDto> incomeDetails;
    
    @Valid
    private List<ExpenseDetailDto> expenseDetails;

    // Getters and Setters
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<AssetDetailDto> getAssetDetails() { return assetDetails; }
    public void setAssetDetails(List<AssetDetailDto> assetDetails) { this.assetDetails = assetDetails; }

    public List<LiabilityDetailDto> getLiabilityDetails() { return liabilityDetails; }
    public void setLiabilityDetails(List<LiabilityDetailDto> liabilityDetails) { this.liabilityDetails = liabilityDetails; }

    public List<IncomeDetailDto> getIncomeDetails() { return incomeDetails; }
    public void setIncomeDetails(List<IncomeDetailDto> incomeDetails) { this.incomeDetails = incomeDetails; }

    public List<ExpenseDetailDto> getExpenseDetails() { return expenseDetails; }
    public void setExpenseDetails(List<ExpenseDetailDto> expenseDetails) { this.expenseDetails = expenseDetails; }

    // Inner DTOs
    public static class AssetDetailDto {
        private Long id;
        
        @NotNull(message = "资产分组不能为空")
        private AssetGroup assetGroup;
        
        @NotBlank(message = "资产名称不能为空")
        private String name;
        
        @NotNull(message = "金额不能为空")
        private BigDecimal amount;
        
        private BigDecimal returnRate;
        private Integer sortOrder = 0;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

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

    public static class LiabilityDetailDto {
        private Long id;
        
        @NotBlank(message = "负债名称不能为空")
        private String name;
        
        @NotNull(message = "金额不能为空")
        private BigDecimal amount;
        
        private BigDecimal interestRate;
        private Integer sortOrder = 0;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public BigDecimal getInterestRate() { return interestRate; }
        public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }

    public static class IncomeDetailDto {
        private Long id;
        
        @NotBlank(message = "收入名称不能为空")
        private String name;
        
        @NotNull(message = "金额不能为空")
        private BigDecimal amount;
        
        private Integer sortOrder = 0;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }

    public static class ExpenseDetailDto {
        private Long id;
        private Long annualExpenseId;
        
        @NotBlank(message = "支出名称不能为空")
        private String name;
        
        @NotNull(message = "金额不能为空")
        private BigDecimal amount;
        
        private String detail;
        private Integer sortOrder = 0;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getAnnualExpenseId() { return annualExpenseId; }
        public void setAnnualExpenseId(Long annualExpenseId) { this.annualExpenseId = annualExpenseId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}
