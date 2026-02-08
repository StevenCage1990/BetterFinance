package com.finance.service;

import com.finance.dto.request.MonthlyRecordRequest;
import com.finance.dto.response.MonthlyRecordResponse;
import com.finance.entity.*;
import com.finance.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MonthlyRecordService {

    private final MonthlyRecordRepository monthlyRecordRepository;
    private final AnnualExpenseRepository annualExpenseRepository;

    public MonthlyRecordService(MonthlyRecordRepository monthlyRecordRepository,
                                AnnualExpenseRepository annualExpenseRepository) {
        this.monthlyRecordRepository = monthlyRecordRepository;
        this.annualExpenseRepository = annualExpenseRepository;
    }

    public MonthlyRecordResponse getByYearAndMonth(Integer year, Integer month) {
        MonthlyRecord record = monthlyRecordRepository.findByYearAndMonth(year, month)
                .orElseThrow(() -> new EntityNotFoundException("未找到" + year + "年" + month + "月的月度记录"));
        return MonthlyRecordResponse.fromEntity(record);
    }

    public List<MonthlyRecordResponse> getByYear(Integer year) {
        return monthlyRecordRepository.findByYearOrderByMonthAsc(year).stream()
                .map(MonthlyRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public MonthlyRecordResponse create(MonthlyRecordRequest request) {
        if (monthlyRecordRepository.existsByYearAndMonth(request.getYear(), request.getMonth())) {
            throw new IllegalArgumentException(request.getYear() + "年" + request.getMonth() + "月的记录已存在");
        }
        
        MonthlyRecord record = new MonthlyRecord();
        record.setYear(request.getYear());
        record.setMonth(request.getMonth());
        record.setSummary(request.getSummary());
        
        // Copy from previous month if no details provided
        if (isEmpty(request)) {
            copyFromPreviousMonth(record, request.getYear(), request.getMonth());
        } else {
            populateDetails(record, request);
        }
        
        record.recalculateTotals();
        record = monthlyRecordRepository.save(record);
        
        return MonthlyRecordResponse.fromEntity(record);
    }

    @Transactional
    public MonthlyRecordResponse update(Long id, MonthlyRecordRequest request) {
        MonthlyRecord record = monthlyRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到ID为" + id + "的月度记录"));
        
        record.setSummary(request.getSummary());
        
        // Clear and repopulate details
        record.getAssetDetails().clear();
        record.getLiabilityDetails().clear();
        record.getIncomeDetails().clear();
        record.getExpenseDetails().clear();
        
        populateDetails(record, request);
        
        record.recalculateTotals();
        record = monthlyRecordRepository.save(record);
        
        return MonthlyRecordResponse.fromEntity(record);
    }

    public MonthlyRecordResponse getPreviousTemplate(Integer year, Integer month) {
        Optional<MonthlyRecord> previous = findPreviousRecord(year, month);
        if (previous.isEmpty()) {
            // Return empty template
            MonthlyRecord empty = new MonthlyRecord();
            empty.setYear(year);
            empty.setMonth(month);
            return MonthlyRecordResponse.fromEntity(empty);
        }
        
        MonthlyRecord template = previous.get();
        // 复制结构和数据
        MonthlyRecordResponse response = MonthlyRecordResponse.fromEntity(template);
        response.setId(null);
        response.setYear(year);
        response.setMonth(month);
        response.setSummary(null);
        
        // 清除明细ID但保留金额
        response.getAssetDetails().forEach(d -> d.setId(null));
        response.getLiabilityDetails().forEach(d -> d.setId(null));
        response.getIncomeDetails().forEach(d -> d.setId(null));
        response.getExpenseDetails().forEach(d -> d.setId(null));
        
        return response;
    }

    private boolean isEmpty(MonthlyRecordRequest request) {
        return (request.getAssetDetails() == null || request.getAssetDetails().isEmpty())
                && (request.getLiabilityDetails() == null || request.getLiabilityDetails().isEmpty())
                && (request.getIncomeDetails() == null || request.getIncomeDetails().isEmpty())
                && (request.getExpenseDetails() == null || request.getExpenseDetails().isEmpty());
    }

    private void copyFromPreviousMonth(MonthlyRecord record, Integer year, Integer month) {
        Optional<MonthlyRecord> previous = findPreviousRecord(year, month);
        if (previous.isEmpty()) return;
        
        MonthlyRecord prev = previous.get();
        
        // Copy asset details
        for (MonthlyAssetDetail detail : prev.getAssetDetails()) {
            MonthlyAssetDetail newDetail = new MonthlyAssetDetail();
            newDetail.setAssetGroup(detail.getAssetGroup());
            newDetail.setName(detail.getName());
            newDetail.setAmount(BigDecimal.ZERO);
            newDetail.setReturnRate(detail.getReturnRate());
            newDetail.setSortOrder(detail.getSortOrder());
            record.addAssetDetail(newDetail);
        }
        
        // Copy liability details
        for (MonthlyLiabilityDetail detail : prev.getLiabilityDetails()) {
            MonthlyLiabilityDetail newDetail = new MonthlyLiabilityDetail();
            newDetail.setName(detail.getName());
            newDetail.setAmount(BigDecimal.ZERO);
            newDetail.setInterestRate(detail.getInterestRate());
            newDetail.setSortOrder(detail.getSortOrder());
            record.addLiabilityDetail(newDetail);
        }
        
        // Copy income details
        for (MonthlyIncomeDetail detail : prev.getIncomeDetails()) {
            MonthlyIncomeDetail newDetail = new MonthlyIncomeDetail();
            newDetail.setName(detail.getName());
            newDetail.setAmount(BigDecimal.ZERO);
            newDetail.setSortOrder(detail.getSortOrder());
            record.addIncomeDetail(newDetail);
        }
        
        // Copy expense details
        for (MonthlyExpenseDetail detail : prev.getExpenseDetails()) {
            MonthlyExpenseDetail newDetail = new MonthlyExpenseDetail();
            newDetail.setName(detail.getName());
            newDetail.setAmount(BigDecimal.ZERO);
            newDetail.setDetail(null);
            newDetail.setAnnualExpense(detail.getAnnualExpense());
            newDetail.setSortOrder(detail.getSortOrder());
            record.addExpenseDetail(newDetail);
        }
    }

    private Optional<MonthlyRecord> findPreviousRecord(Integer year, Integer month) {
        int prevYear = month == 1 ? year - 1 : year;
        int prevMonth = month == 1 ? 12 : month - 1;
        return monthlyRecordRepository.findByYearAndMonth(prevYear, prevMonth);
    }

    private void populateDetails(MonthlyRecord record, MonthlyRecordRequest request) {
        if (request.getAssetDetails() != null) {
            for (MonthlyRecordRequest.AssetDetailDto dto : request.getAssetDetails()) {
                MonthlyAssetDetail detail = new MonthlyAssetDetail();
                detail.setAssetGroup(dto.getAssetGroup());
                detail.setName(dto.getName());
                detail.setAmount(dto.getAmount());
                detail.setReturnRate(dto.getReturnRate());
                detail.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
                record.addAssetDetail(detail);
            }
        }
        
        if (request.getLiabilityDetails() != null) {
            for (MonthlyRecordRequest.LiabilityDetailDto dto : request.getLiabilityDetails()) {
                MonthlyLiabilityDetail detail = new MonthlyLiabilityDetail();
                detail.setName(dto.getName());
                detail.setAmount(dto.getAmount());
                detail.setInterestRate(dto.getInterestRate());
                detail.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
                record.addLiabilityDetail(detail);
            }
        }
        
        if (request.getIncomeDetails() != null) {
            for (MonthlyRecordRequest.IncomeDetailDto dto : request.getIncomeDetails()) {
                MonthlyIncomeDetail detail = new MonthlyIncomeDetail();
                detail.setName(dto.getName());
                detail.setAmount(dto.getAmount());
                detail.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
                record.addIncomeDetail(detail);
            }
        }
        
        if (request.getExpenseDetails() != null) {
            for (MonthlyRecordRequest.ExpenseDetailDto dto : request.getExpenseDetails()) {
                MonthlyExpenseDetail detail = new MonthlyExpenseDetail();
                detail.setName(dto.getName());
                detail.setAmount(dto.getAmount());
                detail.setDetail(dto.getDetail());
                detail.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
                
                if (dto.getAnnualExpenseId() != null) {
                    AnnualExpense expense = annualExpenseRepository.findById(dto.getAnnualExpenseId())
                            .orElse(null);
                    detail.setAnnualExpense(expense);
                }
                
                record.addExpenseDetail(detail);
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!monthlyRecordRepository.existsById(id)) {
            throw new EntityNotFoundException("未找到ID为" + id + "的月度记录");
        }
        monthlyRecordRepository.deleteById(id);
    }
}
