package com.finance.controller;

import com.finance.common.Result;
import com.finance.dto.request.AnnualPlanRequest;
import com.finance.dto.response.AnnualPlanResponse;
import com.finance.service.AnnualPlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annual-plan")
public class AnnualPlanController {

    private final AnnualPlanService annualPlanService;

    public AnnualPlanController(AnnualPlanService annualPlanService) {
        this.annualPlanService = annualPlanService;
    }

    @GetMapping("/{year}")
    public Result<AnnualPlanResponse> getByYear(@PathVariable Integer year) {
        return Result.success(annualPlanService.getByYear(year));
    }

    @PostMapping
    public Result<AnnualPlanResponse> create(@Valid @RequestBody AnnualPlanRequest request) {
        return Result.success(annualPlanService.createOrUpdate(request));
    }

    @PutMapping("/{year}")
    public Result<AnnualPlanResponse> update(@PathVariable Integer year, 
                                              @Valid @RequestBody AnnualPlanRequest request) {
        request.setYear(year);
        return Result.success(annualPlanService.createOrUpdate(request));
    }

    @GetMapping("/{year}/summary")
    public Result<AnnualPlanResponse> getSummary(@PathVariable Integer year) {
        return Result.success(annualPlanService.getSummary(year));
    }

    @GetMapping("/years")
    public Result<List<Integer>> getAvailableYears() {
        return Result.success(annualPlanService.getAvailableYears());
    }
}
