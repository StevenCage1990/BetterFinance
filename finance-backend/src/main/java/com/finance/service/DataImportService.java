package com.finance.service;

import com.finance.entity.*;
import com.finance.enums.AssetGroup;
import com.finance.enums.IncomeType;
import com.finance.repository.*;
import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataImportService {

    private final AnnualBalancePlanRepository annualPlanRepository;
    private final MonthlyRecordRepository monthlyRecordRepository;
    private final AnnualPlanService annualPlanService;

    public DataImportService(AnnualBalancePlanRepository annualPlanRepository,
                            MonthlyRecordRepository monthlyRecordRepository,
                            AnnualPlanService annualPlanService) {
        this.annualPlanRepository = annualPlanRepository;
        this.monthlyRecordRepository = monthlyRecordRepository;
        this.annualPlanService = annualPlanService;
    }

    @Transactional
    public ImportResult importAnnualPlanFromExcel(Integer year, MultipartFile file) {
        ImportResult result = new ImportResult();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            AnnualBalancePlan plan = annualPlanRepository.findByYear(year)
                    .orElseGet(() -> {
                        AnnualBalancePlan newPlan = new AnnualBalancePlan();
                        newPlan.setYear(year);
                        return newPlan;
                    });

            // Clear existing data
            plan.getAnnualIncomes().clear();
            plan.getAssetTargets().clear();
            plan.getLiabilityTargets().clear();
            plan.getAnnualExpenses().clear();

            // Import incomes
            Sheet incomeSheet = workbook.getSheet("年度收入");
            if (incomeSheet != null) {
                int count = importIncomes(plan, incomeSheet);
                result.addMessage("导入年度收入: " + count + " 条");
            }

            // Import asset targets
            Sheet assetSheet = workbook.getSheet("资产目标");
            if (assetSheet != null) {
                int count = importAssetTargets(plan, assetSheet);
                result.addMessage("导入资产目标: " + count + " 条");
            }

            // Import liability targets
            Sheet liabilitySheet = workbook.getSheet("负债目标");
            if (liabilitySheet != null) {
                int count = importLiabilityTargets(plan, liabilitySheet);
                result.addMessage("导入负债目标: " + count + " 条");
            }

            // Import expense budgets
            Sheet expenseSheet = workbook.getSheet("年度预算");
            if (expenseSheet != null) {
                int count = importExpenses(plan, expenseSheet);
                result.addMessage("导入年度预算: " + count + " 条");
            }

            // Calculate surplus
            calculateSurplus(plan);
            
            annualPlanRepository.save(plan);
            result.setSuccess(true);
            result.addMessage("年度规划导入完成");

        } catch (Exception e) {
            result.setSuccess(false);
            result.addMessage("导入失败: " + e.getMessage());
        }

        return result;
    }

    @Transactional
    public ImportResult importMonthlyRecordFromCsv(Integer year, Integer month, MultipartFile file) {
        ImportResult result = new ImportResult();

        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            MonthlyRecord record = monthlyRecordRepository.findByYearAndMonth(year, month)
                    .orElseGet(() -> {
                        MonthlyRecord newRecord = new MonthlyRecord();
                        newRecord.setYear(year);
                        newRecord.setMonth(month);
                        return newRecord;
                    });

            // Clear existing details
            record.getAssetDetails().clear();
            record.getLiabilityDetails().clear();
            record.getIncomeDetails().clear();
            record.getExpenseDetails().clear();

            List<String[]> rows = csvReader.readAll();
            String currentSection = null;
            int sortOrder = 0;

            for (String[] row : rows) {
                if (row.length == 0 || row[0].isEmpty()) continue;

                String firstCell = row[0].trim();

                // Detect section headers
                if (firstCell.equals("资产明细") || firstCell.equals("资产")) {
                    currentSection = "ASSET";
                    sortOrder = 0;
                    continue;
                } else if (firstCell.equals("负债明细") || firstCell.equals("负债")) {
                    currentSection = "LIABILITY";
                    sortOrder = 0;
                    continue;
                } else if (firstCell.equals("收入明细") || firstCell.equals("收入")) {
                    currentSection = "INCOME";
                    sortOrder = 0;
                    continue;
                } else if (firstCell.equals("支出明细") || firstCell.equals("支出")) {
                    currentSection = "EXPENSE";
                    sortOrder = 0;
                    continue;
                }

                // Skip header rows
                if (firstCell.equals("分组") || firstCell.equals("名称") || firstCell.equals("金额")) {
                    continue;
                }

                // Parse data rows
                if (currentSection != null) {
                    switch (currentSection) {
                        case "ASSET":
                            if (row.length >= 3) {
                                MonthlyAssetDetail detail = new MonthlyAssetDetail();
                                detail.setAssetGroup(parseAssetGroup(row[0]));
                                detail.setName(row[1]);
                                detail.setAmount(parseBigDecimal(row[2]));
                                detail.setSortOrder(sortOrder++);
                                record.addAssetDetail(detail);
                            }
                            break;
                        case "LIABILITY":
                            if (row.length >= 2) {
                                MonthlyLiabilityDetail detail = new MonthlyLiabilityDetail();
                                detail.setName(row[0]);
                                detail.setAmount(parseBigDecimal(row[1]));
                                detail.setSortOrder(sortOrder++);
                                record.addLiabilityDetail(detail);
                            }
                            break;
                        case "INCOME":
                            if (row.length >= 2) {
                                MonthlyIncomeDetail detail = new MonthlyIncomeDetail();
                                detail.setName(row[0]);
                                detail.setAmount(parseBigDecimal(row[1]));
                                detail.setSortOrder(sortOrder++);
                                record.addIncomeDetail(detail);
                            }
                            break;
                        case "EXPENSE":
                            if (row.length >= 2) {
                                MonthlyExpenseDetail detail = new MonthlyExpenseDetail();
                                detail.setName(row[0]);
                                detail.setAmount(parseBigDecimal(row[1]));
                                if (row.length >= 3) {
                                    detail.setDetail(row[2]);
                                }
                                detail.setSortOrder(sortOrder++);
                                record.addExpenseDetail(detail);
                            }
                            break;
                    }
                }
            }

            record.recalculateTotals();
            monthlyRecordRepository.save(record);

            result.setSuccess(true);
            result.addMessage("导入完成: 资产" + record.getAssetDetails().size() + "条, " +
                    "负债" + record.getLiabilityDetails().size() + "条, " +
                    "收入" + record.getIncomeDetails().size() + "条, " +
                    "支出" + record.getExpenseDetails().size() + "条");

        } catch (Exception e) {
            result.setSuccess(false);
            result.addMessage("导入失败: " + e.getMessage());
        }

        return result;
    }

    private int importIncomes(AnnualBalancePlan plan, Sheet sheet) {
        int count = 0;
        int sortOrder = 0;
        
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String typeLabel = getCellStringValue(row.getCell(0));
            String name = getCellStringValue(row.getCell(1));
            BigDecimal amount = getCellBigDecimalValue(row.getCell(2));
            String isMonthlyStr = getCellStringValue(row.getCell(3));
            String remark = getCellStringValue(row.getCell(4));

            if (name == null || name.isEmpty()) continue;

            AnnualIncome income = new AnnualIncome();
            income.setIncomeType(parseIncomeType(typeLabel));
            income.setName(name);
            income.setAmount(amount != null ? amount : BigDecimal.ZERO);
            income.setIsMonthly("是".equals(isMonthlyStr));
            income.setRemark(remark);
            income.setSortOrder(sortOrder++);
            plan.addAnnualIncome(income);
            count++;
        }
        return count;
    }

    private int importAssetTargets(AnnualBalancePlan plan, Sheet sheet) {
        int count = 0;
        int sortOrder = 0;
        
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String groupLabel = getCellStringValue(row.getCell(0));
            String name = getCellStringValue(row.getCell(1));
            BigDecimal amount = getCellBigDecimalValue(row.getCell(2));
            BigDecimal returnRate = getCellBigDecimalValue(row.getCell(3));

            if (name == null || name.isEmpty()) continue;

            AssetTarget target = new AssetTarget();
            target.setAssetGroup(parseAssetGroup(groupLabel));
            target.setName(name);
            target.setTargetAmount(amount != null ? amount : BigDecimal.ZERO);
            target.setExpectedReturnRate(returnRate);
            target.setSortOrder(sortOrder++);
            plan.addAssetTarget(target);
            count++;
        }
        return count;
    }

    private int importLiabilityTargets(AnnualBalancePlan plan, Sheet sheet) {
        int count = 0;
        int sortOrder = 0;
        
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String name = getCellStringValue(row.getCell(0));
            BigDecimal balance = getCellBigDecimalValue(row.getCell(1));
            BigDecimal rate = getCellBigDecimalValue(row.getCell(2));

            if (name == null || name.isEmpty()) continue;

            LiabilityTarget target = new LiabilityTarget();
            target.setName(name);
            target.setTargetBalance(balance != null ? balance : BigDecimal.ZERO);
            target.setInterestRate(rate);
            target.setSortOrder(sortOrder++);
            plan.addLiabilityTarget(target);
            count++;
        }
        return count;
    }

    private int importExpenses(AnnualBalancePlan plan, Sheet sheet) {
        int count = 0;
        int sortOrder = 0;
        
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String category = getCellStringValue(row.getCell(0));
            BigDecimal budget = getCellBigDecimalValue(row.getCell(1));

            if (category == null || category.isEmpty()) continue;

            AnnualExpense expense = new AnnualExpense();
            expense.setCategory(category);
            expense.setBudgetAmount(budget != null ? budget : BigDecimal.ZERO);
            expense.setIsMonthly(true);
            expense.setSortOrder(sortOrder++);
            plan.addAnnualExpense(expense);
            count++;
        }
        return count;
    }

    private void calculateSurplus(AnnualBalancePlan plan) {
        BigDecimal monthlyIncome = plan.getAnnualIncomes().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsMonthly()))
                .map(AnnualIncome::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyExpense = plan.getAnnualExpenses().stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsMonthly()))
                .map(AnnualExpense::getBudgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlySurplus = monthlyIncome.subtract(monthlyExpense);
        plan.setMonthlySurplus(monthlySurplus);

        BigDecimal nonMonthlyIncome = plan.getAnnualIncomes().stream()
                .filter(i -> !Boolean.TRUE.equals(i.getIsMonthly()))
                .map(AnnualIncome::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal nonMonthlyExpense = plan.getAnnualExpenses().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsMonthly()))
                .map(AnnualExpense::getBudgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal annualSurplus = monthlySurplus.multiply(BigDecimal.valueOf(12))
                .add(nonMonthlyIncome)
                .subtract(nonMonthlyExpense);
        plan.setAnnualSurplus(annualSurplus);
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private BigDecimal getCellBigDecimalValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) return null;
                return new BigDecimal(value.replace(",", ""));
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private AssetGroup parseAssetGroup(String label) {
        if (label == null) return AssetGroup.LIQUID;
        return switch (label.trim()) {
            case "活钱", "LIQUID" -> AssetGroup.LIQUID;
            case "保障", "PROTECTION" -> AssetGroup.PROTECTION;
            case "投资", "INVESTMENT" -> AssetGroup.INVESTMENT;
            default -> AssetGroup.LIQUID;
        };
    }

    private IncomeType parseIncomeType(String label) {
        if (label == null) return IncomeType.OTHER;
        return switch (label.trim()) {
            case "工资", "SALARY" -> IncomeType.SALARY;
            case "公积金", "FUND" -> IncomeType.FUND;
            case "奖金", "BONUS" -> IncomeType.BONUS;
            case "分红", "DIVIDEND" -> IncomeType.DIVIDEND;
            default -> IncomeType.OTHER;
        };
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(value.trim().replace(",", ""));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public static class ImportResult {
        private boolean success;
        private List<String> messages = new ArrayList<>();

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public List<String> getMessages() { return messages; }
        public void addMessage(String message) { this.messages.add(message); }
    }
}
