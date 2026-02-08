package com.finance.controller;

import com.finance.common.Result;
import com.finance.service.DataExportService;
import com.finance.service.DataImportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final DataExportService dataExportService;
    private final DataImportService dataImportService;

    public DataController(DataExportService dataExportService, DataImportService dataImportService) {
        this.dataExportService = dataExportService;
        this.dataImportService = dataImportService;
    }

    // ==================== Export APIs ====================

    @GetMapping("/export/annual-plan/{year}")
    public ResponseEntity<byte[]> exportAnnualPlan(@PathVariable Integer year) {
        byte[] data = dataExportService.exportAnnualPlanToExcel(year);
        String filename = year + "年度规划.xlsx";
        return createExcelResponse(data, filename);
    }

    @GetMapping("/export/monthly-records/{year}")
    public ResponseEntity<byte[]> exportMonthlyRecords(@PathVariable Integer year) {
        byte[] data = dataExportService.exportMonthlyRecordsToExcel(year);
        String filename = year + "年月度记录.xlsx";
        return createExcelResponse(data, filename);
    }

    @GetMapping("/export/monthly-records/{year}/csv")
    public ResponseEntity<byte[]> exportMonthlyRecordsCsv(@PathVariable Integer year) {
        String csvData = dataExportService.exportMonthlyRecordsToCsv(year);
        String filename = year + "年月度汇总.csv";
        return createCsvResponse(csvData.getBytes(StandardCharsets.UTF_8), filename);
    }

    @GetMapping("/export/full/{year}")
    public ResponseEntity<byte[]> exportFullData(@PathVariable Integer year) {
        byte[] data = dataExportService.exportFullDataToExcel(year);
        String filename = year + "年财务数据.xlsx";
        return createExcelResponse(data, filename);
    }

    // ==================== Import APIs ====================

    @PostMapping("/import/annual-plan/{year}")
    public Result<DataImportService.ImportResult> importAnnualPlan(
            @PathVariable Integer year,
            @RequestParam("file") MultipartFile file) {
        DataImportService.ImportResult result = dataImportService.importAnnualPlanFromExcel(year, file);
        return result.isSuccess() ? Result.success(result) : Result.error(400, String.join("; ", result.getMessages()));
    }

    @PostMapping("/import/monthly-record/{year}/{month}")
    public Result<DataImportService.ImportResult> importMonthlyRecord(
            @PathVariable Integer year,
            @PathVariable Integer month,
            @RequestParam("file") MultipartFile file) {
        DataImportService.ImportResult result = dataImportService.importMonthlyRecordFromCsv(year, month, file);
        return result.isSuccess() ? Result.success(result) : Result.error(400, String.join("; ", result.getMessages()));
    }

    // ==================== Helper Methods ====================

    private ResponseEntity<byte[]> createExcelResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", encodeFilename(filename));
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private ResponseEntity<byte[]> createCsvResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", encodeFilename(filename));
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private String encodeFilename(String filename) {
        try {
            return URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        } catch (Exception e) {
            return filename;
        }
    }
}
