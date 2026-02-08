package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dto.request.AnnualPlanRequest;
import com.finance.enums.AssetGroup;
import com.finance.enums.IncomeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AnnualPlanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @DisplayName("POST /api/annual-plan - 创建年度规划")
    void createAnnualPlan() throws Exception {
        AnnualPlanRequest request = buildTestRequest(2026);

        mockMvc.perform(post("/api/annual-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.monthlySurplus").value(1.5))
                .andExpect(jsonPath("$.data.annualSurplus").value(18.0))
                .andExpect(jsonPath("$.data.annualIncomes", hasSize(1)))
                .andExpect(jsonPath("$.data.annualIncomes[0].name").value("工资"))
                .andExpect(jsonPath("$.data.assetTargets", hasSize(1)))
                .andExpect(jsonPath("$.data.liabilityTargets", hasSize(1)))
                .andExpect(jsonPath("$.data.annualExpenses", hasSize(1)));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/annual-plan/{year} - 获取年度规划")
    void getAnnualPlan() throws Exception {
        mockMvc.perform(get("/api/annual-plan/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.annualIncomes[0].incomeTypeLabel").value("工资"))
                .andExpect(jsonPath("$.data.assetTargets[0].assetGroupLabel").value("活钱"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/annual-plan/{year} - 年度规划不存在返回404")
    void getAnnualPlan_NotFound() throws Exception {
        mockMvc.perform(get("/api/annual-plan/2020"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /api/annual-plan/{year} - 更新年度规划")
    void updateAnnualPlan() throws Exception {
        AnnualPlanRequest request = buildTestRequest(2026);
        // Add bonus income
        AnnualPlanRequest.AnnualIncomeDto bonus = new AnnualPlanRequest.AnnualIncomeDto();
        bonus.setIncomeType(IncomeType.BONUS);
        bonus.setName("年终奖");
        bonus.setAmount(BigDecimal.valueOf(6));
        bonus.setIsMonthly(false);
        bonus.setSortOrder(1);
        request.setAnnualIncomes(List.of(request.getAnnualIncomes().get(0), bonus));

        mockMvc.perform(put("/api/annual-plan/2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.annualIncomes", hasSize(2)))
                .andExpect(jsonPath("$.data.annualSurplus").value(24.0)); // 1.5 * 12 + 6
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/annual-plan/{year}/summary - 获取年度摘要")
    void getAnnualSummary() throws Exception {
        mockMvc.perform(get("/api/annual-plan/2026/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.monthlySurplus").isNumber())
                .andExpect(jsonPath("$.data.annualSurplus").isNumber());
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/annual-plan - 验证请求参数")
    void createAnnualPlan_ValidationError() throws Exception {
        AnnualPlanRequest request = new AnnualPlanRequest();
        // year is null - should fail validation

        mockMvc.perform(post("/api/annual-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private AnnualPlanRequest buildTestRequest(int year) {
        AnnualPlanRequest request = new AnnualPlanRequest();
        request.setYear(year);

        // Income: 2.5万/月
        AnnualPlanRequest.AnnualIncomeDto income = new AnnualPlanRequest.AnnualIncomeDto();
        income.setIncomeType(IncomeType.SALARY);
        income.setName("工资");
        income.setAmount(BigDecimal.valueOf(2.5));
        income.setIsMonthly(true);
        income.setSortOrder(0);
        request.setAnnualIncomes(List.of(income));

        // Asset target
        AnnualPlanRequest.AssetTargetDto asset = new AnnualPlanRequest.AssetTargetDto();
        asset.setAssetGroup(AssetGroup.LIQUID);
        asset.setName("活钱账户");
        asset.setTargetAmount(BigDecimal.valueOf(10));
        asset.setSortOrder(0);
        request.setAssetTargets(List.of(asset));

        // Liability target: 1万/月
        AnnualPlanRequest.LiabilityTargetDto liability = new AnnualPlanRequest.LiabilityTargetDto();
        liability.setName("房贷");
        liability.setTargetBalance(BigDecimal.valueOf(100));
        liability.setSortOrder(0);
        request.setLiabilityTargets(List.of(liability));

        // Expense budget
        AnnualPlanRequest.AnnualExpenseDto expense = new AnnualPlanRequest.AnnualExpenseDto();
        expense.setCategory("日常消费");
        expense.setBudgetAmount(BigDecimal.valueOf(5));
        expense.setIsMonthly(true);
        expense.setSortOrder(0);
        request.setAnnualExpenses(List.of(expense));

        return request;
    }
}
