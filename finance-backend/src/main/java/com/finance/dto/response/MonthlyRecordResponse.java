package com.finance.dto.response;

import com.finance.entity.*;
import com.finance.enums.AssetGroup;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MonthlyRecordResponse {
    private Long id;
    private Integer year;
    private Integer month;
    private BigDecimal totalAsset;
    private BigDecimal totalLiability;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netWorth;
    private BigDecimal surplus;
    private String summary;
    private List<AssetDetailVo> assetDetails;
    private List<LiabilityDetailVo> liabilityDetails;
    private List<IncomeDetailVo> incomeDetails;
    private List<ExpenseDetailVo> expenseDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MonthlyRecordResponse fromEntity(MonthlyRecord entity) {
        MonthlyRecordResponse response = new MonthlyRecordResponse();
        response.setId(entity.getId());
        response.setYear(entity.getYear());
        response.setMonth(entity.getMonth());
        response.setTotalAsset(entity.getTotalAsset());
        response.setTotalLiability(entity.getTotalLiability());
        response.setTotalIncome(entity.getTotalIncome());
        response.setTotalExpense(entity.getTotalExpense());
        response.setNetWorth(entity.getNetWorth());
        response.setSurplus(entity.getSurplus());
        response.setSummary(entity.getSummary());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        
        response.setAssetDetails(entity.getAssetDetails().stream()
                .map(AssetDetailVo::fromEntity)
                .collect(Collectors.toList()));
        
        response.setLiabilityDetails(entity.getLiabilityDetails().stream()
                .map(LiabilityDetailVo::fromEntity)
                .collect(Collectors.toList()));
        
        response.setIncomeDetails(entity.getIncomeDetails().stream()
                .map(IncomeDetailVo::fromEntity)
                .collect(Collectors.toList()));
        
        response.setExpenseDetails(entity.getExpenseDetails().stream()
                .map(ExpenseDetailVo::fromEntity)
                .collect(Collectors.toList()));
        
        return response;
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

    public BigDecimal getNetWorth() { return netWorth; }
    public void setNetWorth(BigDecimal netWorth) { this.netWorth = netWorth; }

    public BigDecimal getSurplus() { return surplus; }
    public void setSurplus(BigDecimal surplus) { this.surplus = surplus; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<AssetDetailVo> getAssetDetails() { return assetDetails; }
    public void setAssetDetails(List<AssetDetailVo> assetDetails) { this.assetDetails = assetDetails; }

    public List<LiabilityDetailVo> getLiabilityDetails() { return liabilityDetails; }
    public void setLiabilityDetails(List<LiabilityDetailVo> liabilityDetails) { this.liabilityDetails = liabilityDetails; }

    public List<IncomeDetailVo> getIncomeDetails() { return incomeDetails; }
    public void setIncomeDetails(List<IncomeDetailVo> incomeDetails) { this.incomeDetails = incomeDetails; }

    public List<ExpenseDetailVo> getExpenseDetails() { return expenseDetails; }
    public void setExpenseDetails(List<ExpenseDetailVo> expenseDetails) { this.expenseDetails = expenseDetails; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Inner VOs
    public static class AssetDetailVo {
        private Long id;
        private AssetGroup assetGroup;
        private String assetGroupLabel;
        private String name;
        private BigDecimal amount;
        private BigDecimal returnRate;
        private Integer sortOrder;

        public static AssetDetailVo fromEntity(MonthlyAssetDetail entity) {
            AssetDetailVo vo = new AssetDetailVo();
            vo.setId(entity.getId());
            vo.setAssetGroup(entity.getAssetGroup());
            vo.setAssetGroupLabel(entity.getAssetGroup().getLabel());
            vo.setName(entity.getName());
            vo.setAmount(entity.getAmount());
            vo.setReturnRate(entity.getReturnRate());
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

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public BigDecimal getReturnRate() { return returnRate; }
        public void setReturnRate(BigDecimal returnRate) { this.returnRate = returnRate; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }

    public static class LiabilityDetailVo {
        private Long id;
        private String name;
        private BigDecimal amount;
        private BigDecimal interestRate;
        private Integer sortOrder;

        public static LiabilityDetailVo fromEntity(MonthlyLiabilityDetail entity) {
            LiabilityDetailVo vo = new LiabilityDetailVo();
            vo.setId(entity.getId());
            vo.setName(entity.getName());
            vo.setAmount(entity.getAmount());
            vo.setInterestRate(entity.getInterestRate());
            vo.setSortOrder(entity.getSortOrder());
            return vo;
        }

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

    public static class IncomeDetailVo {
        private Long id;
        private String name;
        private BigDecimal amount;
        private Integer sortOrder;

        public static IncomeDetailVo fromEntity(MonthlyIncomeDetail entity) {
            IncomeDetailVo vo = new IncomeDetailVo();
            vo.setId(entity.getId());
            vo.setName(entity.getName());
            vo.setAmount(entity.getAmount());
            vo.setSortOrder(entity.getSortOrder());
            return vo;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }

    public static class ExpenseDetailVo {
        private Long id;
        private Long annualExpenseId;
        private String budgetCategory;
        private String name;
        private BigDecimal amount;
        private String detail;
        private Integer sortOrder;

        public static ExpenseDetailVo fromEntity(MonthlyExpenseDetail entity) {
            ExpenseDetailVo vo = new ExpenseDetailVo();
            vo.setId(entity.getId());
            vo.setName(entity.getName());
            vo.setAmount(entity.getAmount());
            vo.setDetail(entity.getDetail());
            vo.setSortOrder(entity.getSortOrder());
            if (entity.getAnnualExpense() != null) {
                vo.setAnnualExpenseId(entity.getAnnualExpense().getId());
                vo.setBudgetCategory(entity.getAnnualExpense().getCategory());
            }
            return vo;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getAnnualExpenseId() { return annualExpenseId; }
        public void setAnnualExpenseId(Long annualExpenseId) { this.annualExpenseId = annualExpenseId; }

        public String getBudgetCategory() { return budgetCategory; }
        public void setBudgetCategory(String budgetCategory) { this.budgetCategory = budgetCategory; }

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
