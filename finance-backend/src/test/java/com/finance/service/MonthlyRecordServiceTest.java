package com.finance.service;

import com.finance.dto.request.MonthlyRecordRequest;
import com.finance.dto.response.MonthlyRecordResponse;
import com.finance.entity.*;
import com.finance.enums.AssetGroup;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthlyRecordServiceTest {

    @Mock
    private MonthlyRecordRepository monthlyRecordRepository;

    @Mock
    private AnnualExpenseRepository annualExpenseRepository;

    @InjectMocks
    private MonthlyRecordService monthlyRecordService;

    private MonthlyRecord testRecord;
    private MonthlyRecordRequest testRequest;
    private AnnualExpense testAnnualExpense;

    @BeforeEach
    void setUp() {
        // Setup test record entity
        testRecord = new MonthlyRecord();
        testRecord.setId(1L);
        testRecord.setYear(2026);
        testRecord.setMonth(1);
        testRecord.setTotalAsset(BigDecimal.valueOf(50));
        testRecord.setTotalLiability(BigDecimal.valueOf(100));
        testRecord.setTotalIncome(BigDecimal.valueOf(2.5));
        testRecord.setTotalExpense(BigDecimal.valueOf(1.2));

        // Add asset detail
        MonthlyAssetDetail assetDetail = new MonthlyAssetDetail();
        assetDetail.setId(1L);
        assetDetail.setAssetGroup(AssetGroup.LIQUID);
        assetDetail.setName("银行活期");
        assetDetail.setAmount(BigDecimal.valueOf(50));
        assetDetail.setSortOrder(0);
        testRecord.addAssetDetail(assetDetail);

        // Add liability detail
        MonthlyLiabilityDetail liabilityDetail = new MonthlyLiabilityDetail();
        liabilityDetail.setId(1L);
        liabilityDetail.setName("房贷");
        liabilityDetail.setAmount(BigDecimal.valueOf(100));
        liabilityDetail.setSortOrder(0);
        testRecord.addLiabilityDetail(liabilityDetail);

        // Add income detail
        MonthlyIncomeDetail incomeDetail = new MonthlyIncomeDetail();
        incomeDetail.setId(1L);
        incomeDetail.setName("工资");
        incomeDetail.setAmount(BigDecimal.valueOf(2.5));
        incomeDetail.setSortOrder(0);
        testRecord.addIncomeDetail(incomeDetail);

        // Add expense detail
        MonthlyExpenseDetail expenseDetail = new MonthlyExpenseDetail();
        expenseDetail.setId(1L);
        expenseDetail.setName("餐饮");
        expenseDetail.setAmount(BigDecimal.valueOf(1.2));
        expenseDetail.setSortOrder(0);
        testRecord.addExpenseDetail(expenseDetail);

        // Setup annual expense for linking
        testAnnualExpense = new AnnualExpense();
        testAnnualExpense.setId(1L);
        testAnnualExpense.setCategory("日常消费");
        testAnnualExpense.setBudgetAmount(BigDecimal.valueOf(5));

        // Setup test request
        testRequest = new MonthlyRecordRequest();
        testRequest.setYear(2026);
        testRequest.setMonth(1);

        MonthlyRecordRequest.AssetDetailDto assetDto = new MonthlyRecordRequest.AssetDetailDto();
        assetDto.setAssetGroup(AssetGroup.LIQUID);
        assetDto.setName("银行活期");
        assetDto.setAmount(BigDecimal.valueOf(50));
        assetDto.setSortOrder(0);
        testRequest.setAssetDetails(List.of(assetDto));

        MonthlyRecordRequest.LiabilityDetailDto liabilityDto = new MonthlyRecordRequest.LiabilityDetailDto();
        liabilityDto.setName("房贷");
        liabilityDto.setAmount(BigDecimal.valueOf(100));
        liabilityDto.setSortOrder(0);
        testRequest.setLiabilityDetails(List.of(liabilityDto));

        MonthlyRecordRequest.IncomeDetailDto incomeDto = new MonthlyRecordRequest.IncomeDetailDto();
        incomeDto.setName("工资");
        incomeDto.setAmount(BigDecimal.valueOf(2.5));
        incomeDto.setSortOrder(0);
        testRequest.setIncomeDetails(List.of(incomeDto));

        MonthlyRecordRequest.ExpenseDetailDto expenseDto = new MonthlyRecordRequest.ExpenseDetailDto();
        expenseDto.setName("餐饮");
        expenseDto.setAmount(BigDecimal.valueOf(1.2));
        expenseDto.setAnnualExpenseId(1L);
        expenseDto.setSortOrder(0);
        testRequest.setExpenseDetails(List.of(expenseDto));
    }

    @Test
    @DisplayName("getByYearAndMonth - 成功获取月度记录")
    void getByYearAndMonth_Success() {
        when(monthlyRecordRepository.findByYearAndMonth(2026, 1)).thenReturn(Optional.of(testRecord));

        MonthlyRecordResponse response = monthlyRecordService.getByYearAndMonth(2026, 1);

        assertThat(response).isNotNull();
        assertThat(response.getYear()).isEqualTo(2026);
        assertThat(response.getMonth()).isEqualTo(1);
        assertThat(response.getTotalAsset()).isEqualByComparingTo(BigDecimal.valueOf(50));
        verify(monthlyRecordRepository).findByYearAndMonth(2026, 1);
    }

    @Test
    @DisplayName("getByYearAndMonth - 记录不存在抛出异常")
    void getByYearAndMonth_NotFound() {
        when(monthlyRecordRepository.findByYearAndMonth(2026, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> monthlyRecordService.getByYearAndMonth(2026, 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("2026")
                .hasMessageContaining("1");
    }

    @Test
    @DisplayName("getByYear - 成功获取年度所有月度记录")
    void getByYear_Success() {
        MonthlyRecord record2 = new MonthlyRecord();
        record2.setId(2L);
        record2.setYear(2026);
        record2.setMonth(2);
        
        when(monthlyRecordRepository.findByYearOrderByMonthAsc(2026))
                .thenReturn(List.of(testRecord, record2));

        List<MonthlyRecordResponse> responses = monthlyRecordService.getByYear(2026);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getMonth()).isEqualTo(1);
        assertThat(responses.get(1).getMonth()).isEqualTo(2);
    }

    @Test
    @DisplayName("create - 成功创建月度记录")
    void create_Success() {
        when(monthlyRecordRepository.existsByYearAndMonth(2026, 1)).thenReturn(false);
        when(annualExpenseRepository.findById(1L)).thenReturn(Optional.of(testAnnualExpense));
        when(monthlyRecordRepository.save(any(MonthlyRecord.class))).thenAnswer(invocation -> {
            MonthlyRecord record = invocation.getArgument(0);
            record.setId(1L);
            return record;
        });

        MonthlyRecordResponse response = monthlyRecordService.create(testRequest);

        assertThat(response).isNotNull();
        assertThat(response.getYear()).isEqualTo(2026);
        assertThat(response.getMonth()).isEqualTo(1);
        verify(monthlyRecordRepository).save(any(MonthlyRecord.class));
    }

    @Test
    @DisplayName("create - 记录已存在抛出异常")
    void create_AlreadyExists() {
        when(monthlyRecordRepository.existsByYearAndMonth(2026, 1)).thenReturn(true);

        assertThatThrownBy(() -> monthlyRecordService.create(testRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    @DisplayName("create - 正确计算总资产")
    void create_CalculateTotalAsset() {
        when(monthlyRecordRepository.existsByYearAndMonth(2026, 1)).thenReturn(false);
        when(annualExpenseRepository.findById(1L)).thenReturn(Optional.of(testAnnualExpense));
        when(monthlyRecordRepository.save(any(MonthlyRecord.class))).thenAnswer(invocation -> {
            MonthlyRecord record = invocation.getArgument(0);
            assertThat(record.getTotalAsset()).isEqualByComparingTo(BigDecimal.valueOf(50));
            return record;
        });

        monthlyRecordService.create(testRequest);

        verify(monthlyRecordRepository).save(any(MonthlyRecord.class));
    }

    @Test
    @DisplayName("create - 正确计算总负债")
    void create_CalculateTotalLiability() {
        when(monthlyRecordRepository.existsByYearAndMonth(2026, 1)).thenReturn(false);
        when(annualExpenseRepository.findById(1L)).thenReturn(Optional.of(testAnnualExpense));
        when(monthlyRecordRepository.save(any(MonthlyRecord.class))).thenAnswer(invocation -> {
            MonthlyRecord record = invocation.getArgument(0);
            assertThat(record.getTotalLiability()).isEqualByComparingTo(BigDecimal.valueOf(100));
            return record;
        });

        monthlyRecordService.create(testRequest);
    }

    @Test
    @DisplayName("create - 正确计算总收入和总支出")
    void create_CalculateTotals() {
        when(monthlyRecordRepository.existsByYearAndMonth(2026, 1)).thenReturn(false);
        when(annualExpenseRepository.findById(1L)).thenReturn(Optional.of(testAnnualExpense));
        when(monthlyRecordRepository.save(any(MonthlyRecord.class))).thenAnswer(invocation -> {
            MonthlyRecord record = invocation.getArgument(0);
            assertThat(record.getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(2.5));
            assertThat(record.getTotalExpense()).isEqualByComparingTo(BigDecimal.valueOf(1.2));
            return record;
        });

        monthlyRecordService.create(testRequest);
    }

    @Test
    @DisplayName("create - 空请求时从上月复制")
    void create_CopyFromPreviousMonth() {
        MonthlyRecordRequest emptyRequest = new MonthlyRecordRequest();
        emptyRequest.setYear(2026);
        emptyRequest.setMonth(2);
        emptyRequest.setAssetDetails(new ArrayList<>());
        emptyRequest.setLiabilityDetails(new ArrayList<>());
        emptyRequest.setIncomeDetails(new ArrayList<>());
        emptyRequest.setExpenseDetails(new ArrayList<>());

        when(monthlyRecordRepository.existsByYearAndMonth(2026, 2)).thenReturn(false);
        when(monthlyRecordRepository.findByYearAndMonth(2026, 1)).thenReturn(Optional.of(testRecord));
        when(monthlyRecordRepository.save(any(MonthlyRecord.class))).thenAnswer(invocation -> {
            MonthlyRecord record = invocation.getArgument(0);
            // Should copy structure from previous month
            assertThat(record.getAssetDetails()).hasSize(1);
            assertThat(record.getAssetDetails().get(0).getName()).isEqualTo("银行活期");
            // Amount should be zero after copy
            assertThat(record.getAssetDetails().get(0).getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            return record;
        });

        monthlyRecordService.create(emptyRequest);
    }

    @Test
    @DisplayName("create - 1月份空请求从上年12月复制")
    void create_CopyFromDecemberOfPreviousYear() {
        MonthlyRecordRequest emptyRequest = new MonthlyRecordRequest();
        emptyRequest.setYear(2026);
        emptyRequest.setMonth(1);
        emptyRequest.setAssetDetails(new ArrayList<>());
        emptyRequest.setLiabilityDetails(new ArrayList<>());
        emptyRequest.setIncomeDetails(new ArrayList<>());
        emptyRequest.setExpenseDetails(new ArrayList<>());

        MonthlyRecord dec2025 = new MonthlyRecord();
        dec2025.setYear(2025);
        dec2025.setMonth(12);
        MonthlyAssetDetail decAsset = new MonthlyAssetDetail();
        decAsset.setAssetGroup(AssetGroup.LIQUID);
        decAsset.setName("去年存款");
        decAsset.setAmount(BigDecimal.valueOf(30));
        dec2025.addAssetDetail(decAsset);

        when(monthlyRecordRepository.existsByYearAndMonth(2026, 1)).thenReturn(false);
        when(monthlyRecordRepository.findByYearAndMonth(2025, 12)).thenReturn(Optional.of(dec2025));
        when(monthlyRecordRepository.save(any(MonthlyRecord.class))).thenAnswer(invocation -> {
            MonthlyRecord record = invocation.getArgument(0);
            assertThat(record.getAssetDetails()).hasSize(1);
            assertThat(record.getAssetDetails().get(0).getName()).isEqualTo("去年存款");
            return record;
        });

        monthlyRecordService.create(emptyRequest);
    }

    @Test
    @DisplayName("update - 成功更新月度记录")
    void update_Success() {
        when(monthlyRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
        when(annualExpenseRepository.findById(1L)).thenReturn(Optional.of(testAnnualExpense));
        when(monthlyRecordRepository.save(any(MonthlyRecord.class))).thenReturn(testRecord);

        MonthlyRecordResponse response = monthlyRecordService.update(1L, testRequest);

        assertThat(response).isNotNull();
        verify(monthlyRecordRepository).save(any(MonthlyRecord.class));
    }

    @Test
    @DisplayName("update - 记录不存在抛出异常")
    void update_NotFound() {
        when(monthlyRecordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> monthlyRecordService.update(1L, testRequest))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getPreviousTemplate - 成功获取上月模板")
    void getPreviousTemplate_Success() {
        when(monthlyRecordRepository.findByYearAndMonth(2026, 1)).thenReturn(Optional.of(testRecord));

        MonthlyRecordResponse response = monthlyRecordService.getPreviousTemplate(2026, 2);

        assertThat(response).isNotNull();
        assertThat(response.getYear()).isEqualTo(2026);
        assertThat(response.getMonth()).isEqualTo(2);
        assertThat(response.getId()).isNull();
        // Amounts should be zero
        assertThat(response.getTotalAsset()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getAssetDetails().get(0).getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getPreviousTemplate - 无上月记录返回空模板")
    void getPreviousTemplate_NoPrevious() {
        when(monthlyRecordRepository.findByYearAndMonth(anyInt(), anyInt())).thenReturn(Optional.empty());

        MonthlyRecordResponse response = monthlyRecordService.getPreviousTemplate(2026, 1);

        assertThat(response).isNotNull();
        assertThat(response.getYear()).isEqualTo(2026);
        assertThat(response.getMonth()).isEqualTo(1);
        assertThat(response.getAssetDetails()).isEmpty();
    }

    @Test
    @DisplayName("delete - 成功删除月度记录")
    void delete_Success() {
        when(monthlyRecordRepository.existsById(1L)).thenReturn(true);
        doNothing().when(monthlyRecordRepository).deleteById(1L);

        monthlyRecordService.delete(1L);

        verify(monthlyRecordRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete - 记录不存在抛出异常")
    void delete_NotFound() {
        when(monthlyRecordRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> monthlyRecordService.delete(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("create - 支出关联年度预算分类")
    void create_LinkExpenseToAnnualBudget() {
        when(monthlyRecordRepository.existsByYearAndMonth(2026, 1)).thenReturn(false);
        when(annualExpenseRepository.findById(1L)).thenReturn(Optional.of(testAnnualExpense));
        when(monthlyRecordRepository.save(any(MonthlyRecord.class))).thenAnswer(invocation -> {
            MonthlyRecord record = invocation.getArgument(0);
            assertThat(record.getExpenseDetails().get(0).getAnnualExpense()).isNotNull();
            assertThat(record.getExpenseDetails().get(0).getAnnualExpense().getCategory()).isEqualTo("日常消费");
            return record;
        });

        monthlyRecordService.create(testRequest);
    }
}
