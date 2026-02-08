import api from './api';
import type { TrendData, AssetDistribution, BudgetProgress } from '../types';

export interface DashboardOverview {
  totalAsset: number;
  totalLiability: number;
  netWorth: number;
  totalIncome: number;
  totalExpense: number;
  surplus: number;
}

export interface AnnualProgressData {
  hasData: boolean;
  monthlySurplus?: number;
  annualSurplus?: number;
  budgetProgress?: BudgetProgress[];
}

export interface AnnualTargetTrendData {
  hasTarget: boolean;
  assetTargetTotal?: number;
  liabilityTargetTotal?: number;
  monthlyData?: Array<{
    month: number;
    assetTarget: number;
    assetActual: number | null;
    liabilityTarget: number;
    liabilityActual: number | null;
  }>;
}

export interface BudgetPieData {
  hasData: boolean;
  totalBudget?: number;
  totalSpent?: number;
  categories?: Array<{
    category: string;
    budgetAmount: number;
    spentAmount: number;
    percentage: number;
  }>;
}

export const dashboardService = {
  getOverview: (year: number, month: number) => 
    api.get<DashboardOverview>(`/dashboard/overview?year=${year}&month=${month}`),

  getAssetTrend: (year: number) => 
    api.get<TrendData[]>(`/dashboard/asset-trend?year=${year}`),

  getIncomeExpenseTrend: (year: number) => 
    api.get<TrendData[]>(`/dashboard/income-expense-trend?year=${year}`),

  getAssetDistribution: (year: number, month: number) => 
    api.get<AssetDistribution[]>(`/dashboard/asset-distribution/${year}/${month}`),

  getAnnualProgress: (year: number) => 
    api.get<AnnualProgressData>(`/dashboard/annual-progress/${year}`),

  getAnnualTargetTrend: (year: number) => 
    api.get<AnnualTargetTrendData>(`/dashboard/annual-target-trend/${year}`),

  getBudgetPie: (year: number) => 
    api.get<BudgetPieData>(`/dashboard/budget-pie/${year}`),
};
