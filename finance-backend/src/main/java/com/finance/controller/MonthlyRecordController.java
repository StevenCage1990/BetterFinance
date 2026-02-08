package com.finance.controller;

import com.finance.common.Result;
import com.finance.dto.request.MonthlyRecordRequest;
import com.finance.dto.response.MonthlyRecordResponse;
import com.finance.service.MonthlyRecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monthly-record")
public class MonthlyRecordController {

    private final MonthlyRecordService monthlyRecordService;

    public MonthlyRecordController(MonthlyRecordService monthlyRecordService) {
        this.monthlyRecordService = monthlyRecordService;
    }

    @GetMapping("/{year}/{month}")
    public Result<MonthlyRecordResponse> getByYearAndMonth(@PathVariable Integer year, 
                                                           @PathVariable Integer month) {
        return Result.success(monthlyRecordService.getByYearAndMonth(year, month));
    }

    @GetMapping("/list")
    public Result<List<MonthlyRecordResponse>> getByYear(@RequestParam Integer year) {
        return Result.success(monthlyRecordService.getByYear(year));
    }

    @GetMapping("/{year}/{month}/previous")
    public Result<MonthlyRecordResponse> getPreviousTemplate(@PathVariable Integer year, 
                                                              @PathVariable Integer month) {
        return Result.success(monthlyRecordService.getPreviousTemplate(year, month));
    }

    @PostMapping
    public Result<MonthlyRecordResponse> create(@Valid @RequestBody MonthlyRecordRequest request) {
        return Result.success(monthlyRecordService.create(request));
    }

    @PutMapping("/{id}")
    public Result<MonthlyRecordResponse> update(@PathVariable Long id, 
                                                 @Valid @RequestBody MonthlyRecordRequest request) {
        return Result.success(monthlyRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        monthlyRecordService.delete(id);
        return Result.success();
    }
}
