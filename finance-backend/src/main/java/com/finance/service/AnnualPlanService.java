package com.finance.service;

import com.finance.dto.request.AnnualPlanRequest;
import com.finance.dto.response.AnnualPlanResponse;
import com.finance.entity.*;
import com.finance.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AnnualPlanService {

    private final AnnualBalancePlanRepository annualPlanRepository;
    private final MonthlyExpenseDetailRepository expenseDetailRepository;

    public AnnualPlanService(AnnualBalancePlanRepository annualPlanRepository,
                             MonthlyExpenseDetailRepository expenseDetailRepository) {
        this.annualPlanRepository = annualPlanRepository;
        this.expenseDetailRepository = expenseDetailRepository;
    }

    public AnnualPlanResponse getByYear(Integer year) {
        AnnualBalancePlan plan = annualPlanRepository.findByYear(year)
                .orElseThrow(() -> new EntityNotFoundException("未找到" + year + "年的年度规划"));
        
        // Update spent amounts for expenses
        updateSpentAmounts(plan);
        
        return AnnualPlanResponse.fromEntity(plan);
    }

    @Transactional
    public AnnualPlanResponse createOrUpdate(AnnualPlanRequest request) {
        AnnualBalancePlan plan = annualPlanRepository.findByYear(request.getYear())
                .orElse(new AnnualBalancePlan());
        
        plan.setYear(request.getYear());
        
        // Update asset targets
        if (request.getAssetTargets() != null) {
            plan.getAssetTargets().clear();
            for (AnnualPlanRequest.AssetTargetDto dto : request.getAssetTargets()) {
                AssetTarget target = new AssetTarget();
                target.setAssetGroup(dto.getAssetGroup());
                target.setName(dto.getName());
                target.setTargetAmount(dto.getTargetAmount());
                target.setAllocationPercentage(dto.getAllocationPercentage());
                target.setExpectedReturnRate(dto.getExpectedReturnRate());
                target.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
                plan.addAssetTarget(target);
            }
        }
        
        // Update liability targets
        if (request.getLiabilityTargets() != null) {
            plan.getLiabilityTargets().clear();
            for (AnnualPlanRequest.LiabilityTargetDto dto : request.getLiabilityTargets()) {
                LiabilityTarget target = new LiabilityTarget();
                target.setLiabilityGroup(dto.getLiabilityGroup());
                target.setName(dto.getName());
                target.setTargetBalance(dto.getTargetBalance());
                target.setInterestRate(dto.getInterestRate());
                target.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
                plan.addLiabilityTarget(target);
            }
        }
        
        // Update annual incomes
        if (request.getAnnualIncomes() != null) {
            plan.getAnnualIncomes().clear();
            for (AnnualPlanRequest.AnnualIncomeDto dto : request.getAnnualIncomes()) {
                AnnualIncome income = new AnnualIncome();
                income.setIncomeType(dto.getIncomeType());
                income.setName(dto.getName());
                income.setAmount(dto.getAmount());
                income.setIsMonthly(dto.getIsMonthly() != null ? dto.getIsMonthly() : true);
                income.setRemark(dto.getRemark());
                income.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
                plan.addAnnualIncome(income);
            }
        }
        
        // Update annual expenses
        if (request.getAnnualExpenses() != null) {
            // Clear references in monthly expense details before deleting
            List<Long> existingExpenseIds = plan.getAnnualExpenses().stream()
                    .map(AnnualExpense::getId)
                    .filter(id -> id != null)
                    .toList();
            if (!existingExpenseIds.isEmpty()) {
                expenseDetailRepository.clearAnnualExpenseReferences(existingExpenseIds);
            }
            
            plan.getAnnualExpenses().clear();
            for (AnnualPlanRequest.AnnualExpenseDto dto : request.getAnnualExpenses()) {
                AnnualExpense expense = new AnnualExpense();
                expense.setParentCategory(dto.getParentCategory());
                expense.setCategory(dto.getCategory());
                expense.setBudgetAmount(dto.getBudgetAmount());
                expense.setIsMonthly(dto.getIsMonthly() != null ? dto.getIsMonthly() : true);
                expense.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
                plan.addAnnualExpense(expense);
            }
        }
        
        // Calculate surplus
        calculateSurplus(plan);
        
        plan = annualPlanRepository.save(plan);
        
        return AnnualPlanResponse.fromEntity(plan);
    }

    public AnnualPlanResponse getSummary(Integer year) {
        AnnualBalancePlan plan = annualPlanRepository.findByYear(year)
                .orElseThrow(() -> new EntityNotFoundException("未找到" + year + "年的年度规划"));
        
        // Recalculate
        calculateSurplus(plan);
        updateSpentAmounts(plan);
        annualPlanRepository.save(plan);
        
        return AnnualPlanResponse.fromEntity(plan);
    }

    private void calculateSurplus(AnnualBalancePlan plan) {
        // Monthly fixed income
        BigDecimal monthlyIncome = plan.getAnnualIncomes().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsMonthly()))
                .map(AnnualIncome::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Monthly fixed expense (from monthly expense items)
        BigDecimal monthlyExpense = plan.getAnnualExpenses().stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsMonthly()))
                .map(AnnualExpense::getBudgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Monthly surplus
        BigDecimal monthlySurplus = monthlyIncome.subtract(monthlyExpense);
        plan.setMonthlySurplus(monthlySurplus);
        
        // Non-monthly income
        BigDecimal nonMonthlyIncome = plan.getAnnualIncomes().stream()
                .filter(i -> !Boolean.TRUE.equals(i.getIsMonthly()))
                .map(AnnualIncome::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Non-monthly expense
        BigDecimal nonMonthlyExpense = plan.getAnnualExpenses().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsMonthly()))
                .map(AnnualExpense::getBudgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Annual surplus = monthly surplus * 12 + non-monthly income - non-monthly expense
        BigDecimal annualSurplus = monthlySurplus.multiply(BigDecimal.valueOf(12))
                .add(nonMonthlyIncome)
                .subtract(nonMonthlyExpense);
        plan.setAnnualSurplus(annualSurplus);
    }

    private void updateSpentAmounts(AnnualBalancePlan plan) {
        for (AnnualExpense expense : plan.getAnnualExpenses()) {
            BigDecimal spent = expenseDetailRepository.sumAmountByAnnualExpenseIdAndYear(
                    expense.getId(), plan.getYear());
            expense.setSpentAmount(spent != null ? spent : BigDecimal.ZERO);
        }
    }

    public List<Integer> getAvailableYears() {
        return annualPlanRepository.findAll().stream()
                .map(AnnualBalancePlan::getYear)
                .sorted()
                .toList();
    }
}
