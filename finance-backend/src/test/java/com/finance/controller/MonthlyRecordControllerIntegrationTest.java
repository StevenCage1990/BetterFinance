package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dto.request.AnnualPlanRequest;
import com.finance.dto.request.MonthlyRecordRequest;
import com.finance.enums.AssetGroup;
import com.finance.enums.IncomeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class MonthlyRecordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long annualExpenseId;

    @BeforeAll
    void setup() throws Exception {
        // Create annual plan first to get annualExpenseId
        AnnualPlanRequest planRequest = new AnnualPlanRequest();
        planRequest.setYear(2026);

        AnnualPlanRequest.AnnualIncomeDto income = new AnnualPlanRequest.AnnualIncomeDto();
        income.setIncomeType(IncomeType.SALARY);
        income.setName("工资");
        income.setAmount(BigDecimal.valueOf(2.5));
        income.setIsMonthly(true);
        income.setSortOrder(0);
        planRequest.setAnnualIncomes(List.of(income));

        planRequest.setAssetTargets(new ArrayList<>());
        planRequest.setLiabilityTargets(new ArrayList<>());

        AnnualPlanRequest.AnnualExpenseDto expense = new AnnualPlanRequest.AnnualExpenseDto();
        expense.setCategory("日常消费");
        expense.setBudgetAmount(BigDecimal.valueOf(5));
        expense.setIsMonthly(true);
        expense.setSortOrder(0);
        planRequest.setAnnualExpenses(List.of(expense));

        String response = mockMvc.perform(post("/api/annual-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planRequest)))
                .andReturn().getResponse().getContentAsString();

        // Extract annualExpenseId from response
        var dataNode = objectMapper.readTree(response).path("data");
        var expensesNode = dataNode.path("annualExpenses");
        if (expensesNode.isArray() && expensesNode.size() > 0) {
            annualExpenseId = expensesNode.get(0).path("id").asLong();
        } else {
            annualExpenseId = 1L; // fallback
        }
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/monthly-record - 创建月度记录")
    void createMonthlyRecord() throws Exception {
        MonthlyRecordRequest request = buildTestRequest(2026, 1);

        mockMvc.perform(post("/api/monthly-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.month").value(1))
                .andExpect(jsonPath("$.data.totalAsset").value(50))
                .andExpect(jsonPath("$.data.totalLiability").value(100))
                .andExpect(jsonPath("$.data.totalIncome").value(2.5))
                .andExpect(jsonPath("$.data.totalExpense").value(1.2))
                .andExpect(jsonPath("$.data.netWorth").value(-50))
                .andExpect(jsonPath("$.data.surplus").value(1.3))
                .andExpect(jsonPath("$.data.assetDetails", hasSize(1)))
                .andExpect(jsonPath("$.data.expenseDetails", hasSize(1)));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/monthly-record/{year}/{month} - 获取月度记录")
    void getMonthlyRecord() throws Exception {
        mockMvc.perform(get("/api/monthly-record/2026/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.month").value(1))
                .andExpect(jsonPath("$.data.assetDetails[0].assetGroupLabel").value("活钱"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/monthly-record/{year}/{month} - 记录不存在返回404")
    void getMonthlyRecord_NotFound() throws Exception {
        mockMvc.perform(get("/api/monthly-record/2026/12"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/monthly-record/list - 获取年度所有月度记录")
    void getMonthlyRecordsByYear() throws Exception {
        mockMvc.perform(get("/api/monthly-record/list").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].month").value(1));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/monthly-record/{year}/{month}/previous - 获取上月模板")
    void getPreviousTemplate() throws Exception {
        mockMvc.perform(get("/api/monthly-record/2026/2/previous"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.month").value(2))
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(jsonPath("$.data.totalAsset").value(0))
                .andExpect(jsonPath("$.data.assetDetails", hasSize(1)))
                .andExpect(jsonPath("$.data.assetDetails[0].name").value("银行活期"))
                .andExpect(jsonPath("$.data.assetDetails[0].amount").value(0));
    }

    @Test
    @Order(6)
    @DisplayName("PUT /api/monthly-record/{id} - 更新月度记录")
    void updateMonthlyRecord() throws Exception {
        MonthlyRecordRequest request = buildTestRequest(2026, 1);
        // Update asset amount
        request.getAssetDetails().get(0).setAmount(BigDecimal.valueOf(55));

        mockMvc.perform(put("/api/monthly-record/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalAsset").value(55))
                .andExpect(jsonPath("$.data.netWorth").value(-45));
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/monthly-record - 重复创建返回错误")
    void createMonthlyRecord_Duplicate() throws Exception {
        MonthlyRecordRequest request = buildTestRequest(2026, 1);

        mockMvc.perform(post("/api/monthly-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/monthly-record - 空请求自动复制上月")
    void createMonthlyRecord_CopyFromPrevious() throws Exception {
        MonthlyRecordRequest request = new MonthlyRecordRequest();
        request.setYear(2026);
        request.setMonth(2);
        request.setAssetDetails(new ArrayList<>());
        request.setLiabilityDetails(new ArrayList<>());
        request.setIncomeDetails(new ArrayList<>());
        request.setExpenseDetails(new ArrayList<>());

        mockMvc.perform(post("/api/monthly-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.month").value(2))
                .andExpect(jsonPath("$.data.assetDetails", hasSize(1)))
                .andExpect(jsonPath("$.data.assetDetails[0].name").value("银行活期"))
                .andExpect(jsonPath("$.data.totalAsset").value(0));
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /api/monthly-record/{id} - 删除月度记录")
    void deleteMonthlyRecord() throws Exception {
        // Delete the record created in order 8
        mockMvc.perform(delete("/api/monthly-record/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify deletion
        mockMvc.perform(get("/api/monthly-record/2026/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    @DisplayName("DELETE /api/monthly-record/{id} - 删除不存在的记录返回404")
    void deleteMonthlyRecord_NotFound() throws Exception {
        mockMvc.perform(delete("/api/monthly-record/999"))
                .andExpect(status().isNotFound());
    }

    private MonthlyRecordRequest buildTestRequest(int year, int month) {
        MonthlyRecordRequest request = new MonthlyRecordRequest();
        request.setYear(year);
        request.setMonth(month);

        // Asset: 50万
        MonthlyRecordRequest.AssetDetailDto asset = new MonthlyRecordRequest.AssetDetailDto();
        asset.setAssetGroup(AssetGroup.LIQUID);
        asset.setName("银行活期");
        asset.setAmount(BigDecimal.valueOf(50));
        asset.setSortOrder(0);
        request.setAssetDetails(List.of(asset));

        // Liability: 100万
        MonthlyRecordRequest.LiabilityDetailDto liability = new MonthlyRecordRequest.LiabilityDetailDto();
        liability.setName("房贷");
        liability.setAmount(BigDecimal.valueOf(100));
        liability.setSortOrder(0);
        request.setLiabilityDetails(List.of(liability));

        // Income: 2.5万
        MonthlyRecordRequest.IncomeDetailDto income = new MonthlyRecordRequest.IncomeDetailDto();
        income.setName("工资");
        income.setAmount(BigDecimal.valueOf(2.5));
        income.setSortOrder(0);
        request.setIncomeDetails(List.of(income));

        // Expense: 1.2万, linked to annual expense
        MonthlyRecordRequest.ExpenseDetailDto expense = new MonthlyRecordRequest.ExpenseDetailDto();
        expense.setName("餐饮");
        expense.setAmount(BigDecimal.valueOf(1.2));
        expense.setAnnualExpenseId(annualExpenseId);
        expense.setSortOrder(0);
        request.setExpenseDetails(List.of(expense));

        return request;
    }
}
