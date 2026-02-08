package com.finance.service;

import com.finance.entity.*;
import com.finance.enums.AssetGroup;
import com.finance.enums.ExpenseCategory;
import com.finance.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final MonthlyRecordRepository monthlyRecordRepository;
    private final MonthlyAssetDetailRepository assetDetailRepository;
    private final MonthlyExpenseDetailRepository expenseDetailRepository;
    private final AnnualBalancePlanRepository annualPlanRepository;

    public DashboardService(MonthlyRecordRepository monthlyRecordRepository,
                            MonthlyAssetDetailRepository assetDetailRepository,
                            MonthlyExpenseDetailRepository expenseDetailRepository,
                            AnnualBalancePlanRepository annualPlanRepository) {
        this.monthlyRecordRepository = monthlyRecordRepository;
        this.assetDetailRepository = assetDetailRepository;
        this.expenseDetailRepository = expenseDetailRepository;
        this.annualPlanRepository = annualPlanRepository;
    }

    public Map<String, Object> getOverview(Integer year, Integer month) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<MonthlyRecord> current = monthlyRecordRepository.findByYearAndMonth(year, month);
        
        if (current.isPresent()) {
            MonthlyRecord record = current.get();
            result.put("totalAsset", record.getTotalAsset());
            result.put("totalLiability", record.getTotalLiability());
            result.put("netWorth", record.getNetWorth());
            result.put("totalIncome", record.getTotalIncome());
            result.put("totalExpense", record.getTotalExpense());
            result.put("surplus", record.getSurplus());
        } else {
            result.put("totalAsset", BigDecimal.ZERO);
            result.put("totalLiability", BigDecimal.ZERO);
            result.put("netWorth", BigDecimal.ZERO);
            result.put("totalIncome", BigDecimal.ZERO);
            result.put("totalExpense", BigDecimal.ZERO);
            result.put("surplus", BigDecimal.ZERO);
        }
        
        return result;
    }

    public List<Map<String, Object>> getAssetTrend(Integer year) {
        List<MonthlyRecord> records = monthlyRecordRepository.findByYearOrderByMonthAsc(year);
        
        return records.stream().map(record -> {
            Map<String, Object> point = new HashMap<>();
            point.put("period", record.getMonth() + "月");
            point.put("month", record.getMonth());
            point.put("asset", record.getTotalAsset());
            point.put("liability", record.getTotalLiability());
            point.put("netWorth", record.getNetWorth());
            return point;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getIncomeExpenseTrend(Integer year) {
        List<MonthlyRecord> records = monthlyRecordRepository.findByYearOrderByMonthAsc(year);
        
        return records.stream().map(record -> {
            Map<String, Object> point = new HashMap<>();
            point.put("period", record.getMonth() + "月");
            point.put("month", record.getMonth());
            point.put("income", record.getTotalIncome());
            point.put("expense", record.getTotalExpense());
            point.put("surplus", record.getSurplus());
            return point;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAssetDistribution(Integer year, Integer month) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        Optional<MonthlyRecord> recordOpt = monthlyRecordRepository.findByYearAndMonth(year, month);
        Optional<AnnualBalancePlan> planOpt = annualPlanRepository.findByYear(year);
        
        for (AssetGroup group : AssetGroup.values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("group", group.name());
            item.put("label", group.getLabel());
            
            BigDecimal currentAmount = BigDecimal.ZERO;
            if (recordOpt.isPresent()) {
                BigDecimal sum = assetDetailRepository.sumAmountByRecordIdAndGroup(
                        recordOpt.get().getId(), group);
                if (sum != null) currentAmount = sum;
            }
            item.put("currentAmount", currentAmount);
            
            BigDecimal targetAmount = BigDecimal.ZERO;
            if (planOpt.isPresent()) {
                targetAmount = planOpt.get().getAssetTargets().stream()
                        .filter(t -> t.getAssetGroup() == group)
                        .map(AssetTarget::getTargetAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            item.put("targetAmount", targetAmount);
            
            double percentage = 0;
            if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
                percentage = currentAmount.divide(targetAmount, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
            }
            item.put("percentage", percentage);
            
            result.add(item);
        }
        
        return result;
    }

    public Map<String, Object> getAnnualProgress(Integer year) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<AnnualBalancePlan> planOpt = annualPlanRepository.findByYear(year);
        if (planOpt.isEmpty()) {
            result.put("hasData", false);
            return result;
        }
        
        AnnualBalancePlan plan = planOpt.get();
        result.put("hasData", true);
        result.put("monthlySurplus", plan.getMonthlySurplus());
        result.put("annualSurplus", plan.getAnnualSurplus());
        
        // Budget execution summary - 过滤掉日常开销类别
        List<Map<String, Object>> budgetProgress = plan.getAnnualExpenses().stream()
            .filter(expense -> expense.getParentCategory() != ExpenseCategory.DAILY)
            .map(expense -> {
            Map<String, Object> item = new HashMap<>();
            item.put("category", expense.getCategory());
            item.put("budgetAmount", expense.getBudgetAmount());
            
            // 动态计算已支出金额
            BigDecimal spentAmount = expenseDetailRepository.sumAmountByAnnualExpenseIdAndYear(
                    expense.getId(), year);
            if (spentAmount == null) spentAmount = BigDecimal.ZERO;
            item.put("spentAmount", spentAmount);
            
            // 重新计算剩余金额和执行率
            BigDecimal budgetAmount = expense.getBudgetAmount();
            BigDecimal remainingAmount = budgetAmount.subtract(spentAmount);
            item.put("remainingAmount", remainingAmount);
            
            double executionRate = 0.0;
            if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
                executionRate = spentAmount.divide(budgetAmount, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
            }
            item.put("executionRate", executionRate);
            
            return item;
        }).collect(Collectors.toList());
        
        result.put("budgetProgress", budgetProgress);
        
        return result;
    }

    public Map<String, Object> getAnnualTargetTrend(Integer year) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<AnnualBalancePlan> planOpt = annualPlanRepository.findByYear(year);
        if (planOpt.isEmpty()) {
            result.put("hasTarget", false);
            return result;
        }
        
        AnnualBalancePlan plan = planOpt.get();
        result.put("hasTarget", true);
        
        // 汇总资产目标
        BigDecimal assetTargetTotal = plan.getAssetTargets().stream()
                .map(AssetTarget::getTargetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("assetTargetTotal", assetTargetTotal);
        
        // 汇总负债目标
        BigDecimal liabilityTargetTotal = plan.getLiabilityTargets().stream()
                .map(LiabilityTarget::getTargetBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("liabilityTargetTotal", liabilityTargetTotal);
        
        // 构建月度数据（1-12月）
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            Map<String, Object> monthPoint = new HashMap<>();
            monthPoint.put("month", month);
            monthPoint.put("assetTarget", assetTargetTotal);
            monthPoint.put("liabilityTarget", liabilityTargetTotal);
            
            Optional<MonthlyRecord> recordOpt = monthlyRecordRepository.findByYearAndMonth(year, month);
            if (recordOpt.isPresent()) {
                MonthlyRecord record = recordOpt.get();
                monthPoint.put("assetActual", record.getTotalAsset());
                monthPoint.put("liabilityActual", record.getTotalLiability());
            } else {
                monthPoint.put("assetActual", null);
                monthPoint.put("liabilityActual", null);
            }
            
            monthlyData.add(monthPoint);
        }
        result.put("monthlyData", monthlyData);
        
        return result;
    }

    public Map<String, Object> getBudgetPie(Integer year) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<AnnualBalancePlan> planOpt = annualPlanRepository.findByYear(year);
        if (planOpt.isEmpty()) {
            result.put("hasData", false);
            return result;
        }
        
        AnnualBalancePlan plan = planOpt.get();
        result.put("hasData", true);
        
        List<Map<String, Object>> categories = new ArrayList<>();
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;
        
        for (AnnualExpense expense : plan.getAnnualExpenses()) {
            // 过滤掉日常开销类别
            if (expense.getParentCategory() == ExpenseCategory.DAILY) {
                continue;
            }
            
            // 计算预算金额（年度化）
            BigDecimal budgetAmount = Boolean.TRUE.equals(expense.getIsMonthly())
                    ? expense.getBudgetAmount().multiply(BigDecimal.valueOf(12))
                    : expense.getBudgetAmount();
            totalBudget = totalBudget.add(budgetAmount);
            
            // 动态计算已支出金额（从月度记录中汇总）
            BigDecimal spentAmount = expenseDetailRepository.sumAmountByAnnualExpenseIdAndYear(
                    expense.getId(), year);
            if (spentAmount == null) spentAmount = BigDecimal.ZERO;
            
            totalSpent = totalSpent.add(spentAmount);
            
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("category", expense.getCategory());
            categoryData.put("budgetAmount", budgetAmount);
            categoryData.put("spentAmount", spentAmount);
            categories.add(categoryData);
        }
        
        // 计算占比
        for (Map<String, Object> categoryData : categories) {
            BigDecimal spentAmount = (BigDecimal) categoryData.get("spentAmount");
            double percentage = totalSpent.compareTo(BigDecimal.ZERO) > 0
                    ? spentAmount.divide(totalSpent, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0;
            categoryData.put("percentage", percentage);
        }
        
        result.put("totalBudget", totalBudget);
        result.put("totalSpent", totalSpent);
        result.put("categories", categories);
        
        return result;
    }
}
