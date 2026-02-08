package com.finance.controller;

import com.finance.common.Result;
import com.finance.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview(@RequestParam Integer year, 
                                                    @RequestParam Integer month) {
        return Result.success(dashboardService.getOverview(year, month));
    }

    @GetMapping("/asset-trend")
    public Result<List<Map<String, Object>>> getAssetTrend(@RequestParam Integer year) {
        return Result.success(dashboardService.getAssetTrend(year));
    }

    @GetMapping("/income-expense-trend")
    public Result<List<Map<String, Object>>> getIncomeExpenseTrend(@RequestParam Integer year) {
        return Result.success(dashboardService.getIncomeExpenseTrend(year));
    }

    @GetMapping("/asset-distribution/{year}/{month}")
    public Result<List<Map<String, Object>>> getAssetDistribution(@PathVariable Integer year, 
                                                                   @PathVariable Integer month) {
        return Result.success(dashboardService.getAssetDistribution(year, month));
    }

    @GetMapping("/annual-progress/{year}")
    public Result<Map<String, Object>> getAnnualProgress(@PathVariable Integer year) {
        return Result.success(dashboardService.getAnnualProgress(year));
    }

    @GetMapping("/annual-target-trend/{year}")
    public Result<Map<String, Object>> getAnnualTargetTrend(@PathVariable Integer year) {
        return Result.success(dashboardService.getAnnualTargetTrend(year));
    }

    @GetMapping("/budget-pie/{year}")
    public Result<Map<String, Object>> getBudgetPie(@PathVariable Integer year) {
        return Result.success(dashboardService.getBudgetPie(year));
    }
}
