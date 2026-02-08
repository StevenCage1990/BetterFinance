package com.finance.service;

import com.finance.dto.request.AnnualPlanRequest;
import com.finance.dto.response.AnnualPlanResponse;
import com.finance.entity.*;
import com.finance.enums.AssetGroup;
import com.finance.enums.IncomeType;
import com.finance.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnualPlanServiceTest {

    @Mock
    private AnnualBalancePlanRepository annualPlanRepository;

    @Mock
    private MonthlyExpenseDetailRepository monthlyExpenseDetailRepository;

    @InjectMocks
    private AnnualPlanService annualPlanService;

    private AnnualBalancePlan testPlan;
    private AnnualPlanRequest testRequest;

    @BeforeEach
    void setUp() {
        // Setup test plan entity
        testPlan = new AnnualBalancePlan();
        testPlan.setId(1L);
        testPlan.setYear(2026);
        testPlan.setMonthlySurplus(BigDecimal.valueOf(1.5));
        testPlan.setAnnualSurplus(BigDecimal.valueOf(18));

        // Add income
        AnnualIncome income = new AnnualIncome();
        income.setId(1L);
        income.setIncomeType(IncomeType.SALARY);
        income.setName("工资");
        income.setAmount(BigDecimal.valueOf(2.5));
        income.setIsMonthly(true);
        income.setSortOrder(0);
        testPlan.addAnnualIncome(income);

        // Add liability
        LiabilityTarget liability = new LiabilityTarget();
        liability.setId(1L);
        liability.setName("房贷");
        liability.setTargetBalance(BigDecimal.valueOf(100));
        liability.setSortOrder(0);
        testPlan.addLiabilityTarget(liability);

        // Add expense
        AnnualExpense expense = new AnnualExpense();
        expense.setId(1L);
        expense.setCategory("日常消费");
        expense.setBudgetAmount(BigDecimal.valueOf(5));
        expense.setIsMonthly(true);
        expense.setSortOrder(0);
        testPlan.addAnnualExpense(expense);

        // Setup test request
        testRequest = new AnnualPlanRequest();
        testRequest.setYear(2026);
        
        AnnualPlanRequest.AnnualIncomeDto incomeDto = new AnnualPlanRequest.AnnualIncomeDto();
        incomeDto.setIncomeType(IncomeType.SALARY);
        incomeDto.setName("工资");
        incomeDto.setAmount(BigDecimal.valueOf(2.5));
        incomeDto.setIsMonthly(true);
        incomeDto.setSortOrder(0);
        testRequest.setAnnualIncomes(List.of(incomeDto));

        AnnualPlanRequest.LiabilityTargetDto liabilityDto = new AnnualPlanRequest.LiabilityTargetDto();
        liabilityDto.setName("房贷");
        liabilityDto.setTargetBalance(BigDecimal.valueOf(100));
        liabilityDto.setSortOrder(0);
        testRequest.setLiabilityTargets(List.of(liabilityDto));

        AnnualPlanRequest.AnnualExpenseDto expenseDto = new AnnualPlanRequest.AnnualExpenseDto();
        expenseDto.setCategory("日常消费");
        expenseDto.setBudgetAmount(BigDecimal.valueOf(5));
        expenseDto.setIsMonthly(true);
        expenseDto.setSortOrder(0);
        testRequest.setAnnualExpenses(List.of(expenseDto));

        testRequest.setAssetTargets(new ArrayList<>());
    }

    @Test
    @DisplayName("getByYear - 成功获取年度规划")
    void getByYear_Success() {
        when(annualPlanRepository.findByYear(2026)).thenReturn(Optional.of(testPlan));

        AnnualPlanResponse response = annualPlanService.getByYear(2026);

        assertThat(response).isNotNull();
        assertThat(response.getYear()).isEqualTo(2026);
        assertThat(response.getAnnualIncomes()).hasSize(1);
        assertThat(response.getAnnualExpenses()).hasSize(1);
        verify(annualPlanRepository).findByYear(2026);
    }

    @Test
    @DisplayName("getByYear - 年度规划不存在抛出异常")
    void getByYear_NotFound() {
        when(annualPlanRepository.findByYear(2026)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> annualPlanService.getByYear(2026))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("2026");
    }

    @Test
    @DisplayName("createOrUpdate - 创建新年度规划")
    void createOrUpdate_CreateNew() {
        when(annualPlanRepository.findByYear(2026)).thenReturn(Optional.empty());
        when(annualPlanRepository.save(any(AnnualBalancePlan.class))).thenAnswer(invocation -> {
            AnnualBalancePlan plan = invocation.getArgument(0);
            plan.setId(1L);
            return plan;
        });

        AnnualPlanResponse response = annualPlanService.createOrUpdate(testRequest);

        assertThat(response).isNotNull();
        assertThat(response.getYear()).isEqualTo(2026);
        verify(annualPlanRepository).save(any(AnnualBalancePlan.class));
    }

    @Test
    @DisplayName("createOrUpdate - 更新现有年度规划")
    void createOrUpdate_UpdateExisting() {
        when(annualPlanRepository.findByYear(2026)).thenReturn(Optional.of(testPlan));
        when(annualPlanRepository.save(any(AnnualBalancePlan.class))).thenReturn(testPlan);

        AnnualPlanResponse response = annualPlanService.createOrUpdate(testRequest);

        assertThat(response).isNotNull();
        verify(annualPlanRepository).save(any(AnnualBalancePlan.class));
    }

    @Test
    @DisplayName("createOrUpdate - 正确计算月结余")
    void createOrUpdate_CalculateMonthlySurplus() {
        when(annualPlanRepository.findByYear(2026)).thenReturn(Optional.empty());
        when(annualPlanRepository.save(any(AnnualBalancePlan.class))).thenAnswer(invocation -> {
            AnnualBalancePlan plan = invocation.getArgument(0);
            // Monthly surplus = monthly income (2.5) - monthly payment (1) = 1.5
            assertThat(plan.getMonthlySurplus()).isEqualByComparingTo(BigDecimal.valueOf(1.5));
            return plan;
        });

        annualPlanService.createOrUpdate(testRequest);

        verify(annualPlanRepository).save(any(AnnualBalancePlan.class));
    }

    @Test
    @DisplayName("createOrUpdate - 正确计算年结余")
    void createOrUpdate_CalculateAnnualSurplus() {
        // Add non-monthly income
        AnnualPlanRequest.AnnualIncomeDto bonusDto = new AnnualPlanRequest.AnnualIncomeDto();
        bonusDto.setIncomeType(IncomeType.BONUS);
        bonusDto.setName("年终奖");
        bonusDto.setAmount(BigDecimal.valueOf(6));
        bonusDto.setIsMonthly(false);
        bonusDto.setSortOrder(1);
        testRequest.setAnnualIncomes(List.of(testRequest.getAnnualIncomes().get(0), bonusDto));

        when(annualPlanRepository.findByYear(2026)).thenReturn(Optional.empty());
        when(annualPlanRepository.save(any(AnnualBalancePlan.class))).thenAnswer(invocation -> {
            AnnualBalancePlan plan = invocation.getArgument(0);
            // Annual surplus = (monthly income 2.5 - monthly payment 1) * 12 + non-monthly 6 = 1.5 * 12 + 6 = 24
            assertThat(plan.getAnnualSurplus()).isEqualByComparingTo(BigDecimal.valueOf(24));
            return plan;
        });

        annualPlanService.createOrUpdate(testRequest);

        verify(annualPlanRepository).save(any(AnnualBalancePlan.class));
    }

    @Test
    @DisplayName("createOrUpdate - 处理空的收入列表")
    void createOrUpdate_EmptyIncomes() {
        testRequest.setAnnualIncomes(new ArrayList<>());

        when(annualPlanRepository.findByYear(2026)).thenReturn(Optional.empty());
        when(annualPlanRepository.save(any(AnnualBalancePlan.class))).thenAnswer(invocation -> {
            AnnualBalancePlan plan = invocation.getArgument(0);
            // No income, only monthly payment: -1 * 12 = -12
            assertThat(plan.getMonthlySurplus()).isEqualByComparingTo(BigDecimal.valueOf(-1));
            return plan;
        });

        annualPlanService.createOrUpdate(testRequest);
    }

    @Test
    @DisplayName("createOrUpdate - 处理空的负债列表")
    void createOrUpdate_EmptyLiabilities() {
        testRequest.setLiabilityTargets(new ArrayList<>());

        when(annualPlanRepository.findByYear(2026)).thenReturn(Optional.empty());
        when(annualPlanRepository.save(any(AnnualBalancePlan.class))).thenAnswer(invocation -> {
            AnnualBalancePlan plan = invocation.getArgument(0);
            // Only monthly income: 2.5
            assertThat(plan.getMonthlySurplus()).isEqualByComparingTo(BigDecimal.valueOf(2.5));
            return plan;
        });

        annualPlanService.createOrUpdate(testRequest);
    }

    @Test
    @DisplayName("createOrUpdate - 添加资产目标")
    void createOrUpdate_WithAssetTargets() {
        AnnualPlanRequest.AssetTargetDto assetDto = new AnnualPlanRequest.AssetTargetDto();
        assetDto.setAssetGroup(AssetGroup.LIQUID);
        assetDto.setName("活钱账户");
        assetDto.setTargetAmount(BigDecimal.valueOf(10));
        assetDto.setSortOrder(0);
        testRequest.setAssetTargets(List.of(assetDto));

        when(annualPlanRepository.findByYear(2026)).thenReturn(Optional.empty());
        when(annualPlanRepository.save(any(AnnualBalancePlan.class))).thenAnswer(invocation -> {
            AnnualBalancePlan plan = invocation.getArgument(0);
            assertThat(plan.getAssetTargets()).hasSize(1);
            assertThat(plan.getAssetTargets().get(0).getAssetGroup()).isEqualTo(AssetGroup.LIQUID);
            return plan;
        });

        annualPlanService.createOrUpdate(testRequest);
    }
}
