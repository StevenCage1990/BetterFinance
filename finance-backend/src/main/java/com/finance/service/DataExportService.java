package com.finance.service;

import com.finance.entity.*;
import com.finance.repository.*;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class DataExportService {

    private final AnnualBalancePlanRepository annualPlanRepository;
    private final MonthlyRecordRepository monthlyRecordRepository;

    public DataExportService(AnnualBalancePlanRepository annualPlanRepository,
                            MonthlyRecordRepository monthlyRecordRepository) {
        this.annualPlanRepository = annualPlanRepository;
        this.monthlyRecordRepository = monthlyRecordRepository;
    }

    public byte[] exportAnnualPlanToExcel(Integer year) {
        AnnualBalancePlan plan = annualPlanRepository.findByYear(year)
                .orElseThrow(() -> new IllegalArgumentException("未找到" + year + "年的年度规划"));

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            // Sheet 1: 年度收入
            Sheet incomeSheet = workbook.createSheet("年度收入");
            createIncomeSheet(incomeSheet, plan.getAnnualIncomes(), headerStyle, moneyStyle);

            // Sheet 2: 资产目标
            Sheet assetSheet = workbook.createSheet("资产目标");
            createAssetTargetSheet(assetSheet, plan.getAssetTargets(), headerStyle, moneyStyle);

            // Sheet 3: 负债目标
            Sheet liabilitySheet = workbook.createSheet("负债目标");
            createLiabilityTargetSheet(liabilitySheet, plan.getLiabilityTargets(), headerStyle, moneyStyle);

            // Sheet 4: 年度预算
            Sheet expenseSheet = workbook.createSheet("年度预算");
            createExpenseSheet(expenseSheet, plan.getAnnualExpenses(), headerStyle, moneyStyle);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }

    public byte[] exportMonthlyRecordsToExcel(Integer year) {
        List<MonthlyRecord> records = monthlyRecordRepository.findByYearOrderByMonthAsc(year);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            // Summary sheet
            Sheet summarySheet = workbook.createSheet("月度汇总");
            createMonthlySummarySheet(summarySheet, records, headerStyle, moneyStyle);

            // Create sheet for each month
            for (MonthlyRecord record : records) {
                Sheet monthSheet = workbook.createSheet(record.getMonth() + "月明细");
                createMonthDetailSheet(monthSheet, record, headerStyle, moneyStyle);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }

    public String exportMonthlyRecordsToCsv(Integer year) {
        List<MonthlyRecord> records = monthlyRecordRepository.findByYearOrderByMonthAsc(year);

        StringWriter stringWriter = new StringWriter();
        try (CSVWriter writer = new CSVWriter(stringWriter)) {
            // Header
            writer.writeNext(new String[]{"月份", "总资产", "总负债", "净资产", "总收入", "总支出", "结余"});

            // Data
            for (MonthlyRecord record : records) {
                BigDecimal netWorth = record.getTotalAsset().subtract(record.getTotalLiability());
                BigDecimal surplus = record.getTotalIncome().subtract(record.getTotalExpense());
                writer.writeNext(new String[]{
                        record.getMonth() + "月",
                        format(record.getTotalAsset()),
                        format(record.getTotalLiability()),
                        format(netWorth),
                        format(record.getTotalIncome()),
                        format(record.getTotalExpense()),
                        format(surplus)
                });
            }

            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException("导出CSV失败: " + e.getMessage(), e);
        }
    }

    public byte[] exportFullDataToExcel(Integer year) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            // Annual Plan sheets
            annualPlanRepository.findByYear(year).ifPresent(plan -> {
                Sheet incomeSheet = workbook.createSheet("年度收入");
                createIncomeSheet(incomeSheet, plan.getAnnualIncomes(), headerStyle, moneyStyle);

                Sheet assetSheet = workbook.createSheet("资产目标");
                createAssetTargetSheet(assetSheet, plan.getAssetTargets(), headerStyle, moneyStyle);

                Sheet liabilitySheet = workbook.createSheet("负债目标");
                createLiabilityTargetSheet(liabilitySheet, plan.getLiabilityTargets(), headerStyle, moneyStyle);

                Sheet expenseSheet = workbook.createSheet("年度预算");
                createExpenseSheet(expenseSheet, plan.getAnnualExpenses(), headerStyle, moneyStyle);
            });

            // Monthly records
            List<MonthlyRecord> records = monthlyRecordRepository.findByYearOrderByMonthAsc(year);
            Sheet summarySheet = workbook.createSheet("月度汇总");
            createMonthlySummarySheet(summarySheet, records, headerStyle, moneyStyle);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }

    private void createIncomeSheet(Sheet sheet, List<AnnualIncome> incomes, CellStyle headerStyle, CellStyle moneyStyle) {
        Row header = sheet.createRow(0);
        String[] headers = {"类型", "名称", "金额(万)", "是否月度", "备注"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (AnnualIncome income : incomes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(income.getIncomeType().getLabel());
            row.createCell(1).setCellValue(income.getName());
            Cell amountCell = row.createCell(2);
            amountCell.setCellValue(income.getAmount().doubleValue());
            amountCell.setCellStyle(moneyStyle);
            row.createCell(3).setCellValue(Boolean.TRUE.equals(income.getIsMonthly()) ? "是" : "否");
            row.createCell(4).setCellValue(income.getRemark() != null ? income.getRemark() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createAssetTargetSheet(Sheet sheet, List<AssetTarget> assets, CellStyle headerStyle, CellStyle moneyStyle) {
        Row header = sheet.createRow(0);
        String[] headers = {"分组", "名称", "目标金额(万)", "预期收益率(%)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (AssetTarget asset : assets) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(asset.getAssetGroup().getLabel());
            row.createCell(1).setCellValue(asset.getName());
            Cell amountCell = row.createCell(2);
            amountCell.setCellValue(asset.getTargetAmount().doubleValue());
            amountCell.setCellStyle(moneyStyle);
            if (asset.getExpectedReturnRate() != null) {
                row.createCell(3).setCellValue(asset.getExpectedReturnRate().doubleValue());
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createLiabilityTargetSheet(Sheet sheet, List<LiabilityTarget> liabilities, CellStyle headerStyle, CellStyle moneyStyle) {
        Row header = sheet.createRow(0);
        String[] headers = {"分组", "名称", "目标余额(万)", "利率(%)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (LiabilityTarget liability : liabilities) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(liability.getLiabilityGroup() != null ? liability.getLiabilityGroup().getLabel() : "");
            row.createCell(1).setCellValue(liability.getName());
            Cell balanceCell = row.createCell(2);
            balanceCell.setCellValue(liability.getTargetBalance().doubleValue());
            balanceCell.setCellStyle(moneyStyle);
            if (liability.getInterestRate() != null) {
                row.createCell(3).setCellValue(liability.getInterestRate().doubleValue());
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createExpenseSheet(Sheet sheet, List<AnnualExpense> expenses, CellStyle headerStyle, CellStyle moneyStyle) {
        Row header = sheet.createRow(0);
        String[] headers = {"分组", "名称", "金额(万)", "月度/年度", "已消耗(万)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (AnnualExpense expense : expenses) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(expense.getParentCategory() != null ? expense.getParentCategory().getLabel() : "");
            row.createCell(1).setCellValue(expense.getCategory());
            Cell budgetCell = row.createCell(2);
            budgetCell.setCellValue(expense.getBudgetAmount().doubleValue());
            budgetCell.setCellStyle(moneyStyle);
            row.createCell(3).setCellValue(Boolean.TRUE.equals(expense.getIsMonthly()) ? "月度" : "年度");
            Cell spentCell = row.createCell(4);
            spentCell.setCellValue(expense.getSpentAmount() != null ? expense.getSpentAmount().doubleValue() : 0);
            spentCell.setCellStyle(moneyStyle);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createMonthlySummarySheet(Sheet sheet, List<MonthlyRecord> records, CellStyle headerStyle, CellStyle moneyStyle) {
        Row header = sheet.createRow(0);
        String[] headers = {"月份", "总资产", "总负债", "净资产", "总收入", "总支出", "结余"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (MonthlyRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(record.getMonth() + "月");

            Cell assetCell = row.createCell(1);
            assetCell.setCellValue(record.getTotalAsset().doubleValue());
            assetCell.setCellStyle(moneyStyle);

            Cell liabilityCell = row.createCell(2);
            liabilityCell.setCellValue(record.getTotalLiability().doubleValue());
            liabilityCell.setCellStyle(moneyStyle);

            BigDecimal netWorth = record.getTotalAsset().subtract(record.getTotalLiability());
            Cell netWorthCell = row.createCell(3);
            netWorthCell.setCellValue(netWorth.doubleValue());
            netWorthCell.setCellStyle(moneyStyle);

            Cell incomeCell = row.createCell(4);
            incomeCell.setCellValue(record.getTotalIncome().doubleValue());
            incomeCell.setCellStyle(moneyStyle);

            Cell expenseCell = row.createCell(5);
            expenseCell.setCellValue(record.getTotalExpense().doubleValue());
            expenseCell.setCellStyle(moneyStyle);

            BigDecimal surplus = record.getTotalIncome().subtract(record.getTotalExpense());
            Cell surplusCell = row.createCell(6);
            surplusCell.setCellValue(surplus.doubleValue());
            surplusCell.setCellStyle(moneyStyle);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createMonthDetailSheet(Sheet sheet, MonthlyRecord record, CellStyle headerStyle, CellStyle moneyStyle) {
        int rowNum = 0;

        // Assets section
        Row assetHeader = sheet.createRow(rowNum++);
        assetHeader.createCell(0).setCellValue("资产明细");
        assetHeader.getCell(0).setCellStyle(headerStyle);

        Row assetColHeader = sheet.createRow(rowNum++);
        assetColHeader.createCell(0).setCellValue("分组");
        assetColHeader.createCell(1).setCellValue("名称");
        assetColHeader.createCell(2).setCellValue("金额(万)");

        for (MonthlyAssetDetail detail : record.getAssetDetails()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(detail.getAssetGroup().getLabel());
            row.createCell(1).setCellValue(detail.getName());
            Cell cell = row.createCell(2);
            cell.setCellValue(detail.getAmount().doubleValue());
            cell.setCellStyle(moneyStyle);
        }

        rowNum++; // Empty row

        // Liabilities section
        Row liabilityHeader = sheet.createRow(rowNum++);
        liabilityHeader.createCell(0).setCellValue("负债明细");
        liabilityHeader.getCell(0).setCellStyle(headerStyle);

        Row liabilityColHeader = sheet.createRow(rowNum++);
        liabilityColHeader.createCell(0).setCellValue("名称");
        liabilityColHeader.createCell(1).setCellValue("金额(万)");

        for (MonthlyLiabilityDetail detail : record.getLiabilityDetails()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(detail.getName());
            Cell cell = row.createCell(1);
            cell.setCellValue(detail.getAmount().doubleValue());
            cell.setCellStyle(moneyStyle);
        }

        rowNum++;

        // Income section
        Row incomeHeader = sheet.createRow(rowNum++);
        incomeHeader.createCell(0).setCellValue("收入明细");
        incomeHeader.getCell(0).setCellStyle(headerStyle);

        Row incomeColHeader = sheet.createRow(rowNum++);
        incomeColHeader.createCell(0).setCellValue("名称");
        incomeColHeader.createCell(1).setCellValue("金额(万)");

        for (MonthlyIncomeDetail detail : record.getIncomeDetails()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(detail.getName());
            Cell cell = row.createCell(1);
            cell.setCellValue(detail.getAmount().doubleValue());
            cell.setCellStyle(moneyStyle);
        }

        rowNum++;

        // Expense section
        Row expenseHeader = sheet.createRow(rowNum++);
        expenseHeader.createCell(0).setCellValue("支出明细");
        expenseHeader.getCell(0).setCellStyle(headerStyle);

        Row expenseColHeader = sheet.createRow(rowNum++);
        expenseColHeader.createCell(0).setCellValue("名称");
        expenseColHeader.createCell(1).setCellValue("金额(万)");
        expenseColHeader.createCell(2).setCellValue("备注");

        for (MonthlyExpenseDetail detail : record.getExpenseDetails()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(detail.getName());
            Cell cell = row.createCell(1);
            cell.setCellValue(detail.getAmount().doubleValue());
            cell.setCellStyle(moneyStyle);
            row.createCell(2).setCellValue(detail.getDetail() != null ? detail.getDetail() : "");
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    private String format(BigDecimal value) {
        return value != null ? value.toPlainString() : "0";
    }
}
