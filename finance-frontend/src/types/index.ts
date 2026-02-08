// Asset group constants
export const AssetGroup = {
  LIQUID: 'LIQUID',
  PROTECTION: 'PROTECTION',
  INVESTMENT: 'INVESTMENT',
} as const;

export type AssetGroupType = typeof AssetGroup[keyof typeof AssetGroup];

export const AssetGroupLabels: Record<AssetGroupType, string> = {
  [AssetGroup.LIQUID]: '活钱',
  [AssetGroup.PROTECTION]: '保障',
  [AssetGroup.INVESTMENT]: '投资',
};

// Income type constants
export const IncomeType = {
  SALARY: 'SALARY',
  FUND: 'FUND',
  BONUS: 'BONUS',
  DIVIDEND: 'DIVIDEND',
  OTHER: 'OTHER',
} as const;

export type IncomeTypeType = typeof IncomeType[keyof typeof IncomeType];

export const IncomeTypeLabels: Record<IncomeTypeType, string> = {
  [IncomeType.SALARY]: '工资',
  [IncomeType.FUND]: '公积金',
  [IncomeType.BONUS]: '奖金',
  [IncomeType.DIVIDEND]: '股权',
  [IncomeType.OTHER]: '其他',
};

// Expense category constants (一级分类)
export const ExpenseCategory = {
  PROTECTION: 'PROTECTION',
  LEISURE: 'LEISURE',
  DAILY: 'DAILY',
  OTHER: 'OTHER',
} as const;

export type ExpenseCategoryType = typeof ExpenseCategory[keyof typeof ExpenseCategory];

export const ExpenseCategoryLabels: Record<ExpenseCategoryType, string> = {
  [ExpenseCategory.PROTECTION]: '人生保障',
  [ExpenseCategory.LEISURE]: '休闲玩乐',
  [ExpenseCategory.DAILY]: '日常开销',
  [ExpenseCategory.OTHER]: '其他',
};

// Liability group constants
export const LiabilityGroup = {
  LOAN: 'LOAN',
} as const;

export type LiabilityGroupType = typeof LiabilityGroup[keyof typeof LiabilityGroup];

export const LiabilityGroupLabels: Record<LiabilityGroupType, string> = {
  [LiabilityGroup.LOAN]: '贷款',
};

// Annual Plan types
export interface AnnualBalancePlan {
  id?: number;
  year: number;
  monthlySurplus?: number;
  annualSurplus?: number;
  assetTargets: AssetTarget[];
  liabilityTargets: LiabilityTarget[];
  annualIncomes: AnnualIncome[];
  annualExpenses: AnnualExpense[];
}

export interface AssetTarget {
  id?: number;
  assetGroup: AssetGroupType;
  name: string;
  targetAmount: number;
  allocationPercentage?: number;
  expectedReturnRate?: number;
  sortOrder: number;
}

export interface LiabilityTarget {
  id?: number;
  liabilityGroup: LiabilityGroupType;
  name: string;
  targetBalance: number;
  interestRate?: number;
  sortOrder: number;
}

export interface AnnualIncome {
  id?: number;
  incomeType: IncomeTypeType;
  name: string;
  amount: number;
  isMonthly: boolean;
  remark?: string;
  sortOrder: number;
}

export interface AnnualExpense {
  id?: number;
  parentCategory: ExpenseCategoryType;
  category: string;
  budgetAmount: number;
  isMonthly: boolean;
  spentAmount?: number;
  sortOrder: number;
}

// Monthly Record types
export interface MonthlyRecord {
  id?: number;
  year: number;
  month: number;
  totalAsset: number;
  totalLiability: number;
  totalIncome: number;
  totalExpense: number;
  summary?: string;
  assetDetails: MonthlyAssetDetail[];
  liabilityDetails: MonthlyLiabilityDetail[];
  incomeDetails: MonthlyIncomeDetail[];
  expenseDetails: MonthlyExpenseDetail[];
}

export interface MonthlyAssetDetail {
  id?: number;
  assetGroup: AssetGroupType;
  name: string;
  amount: number;
  returnRate?: number;
  sortOrder: number;
}

export interface MonthlyLiabilityDetail {
  id?: number;
  name: string;
  amount: number;
  interestRate?: number;
  sortOrder: number;
}

export interface MonthlyIncomeDetail {
  id?: number;
  name: string;
  amount: number;
  sortOrder: number;
}

export interface MonthlyExpenseDetail {
  id?: number;
  annualExpenseId?: number;
  name: string;
  amount: number;
  detail?: string;
  sortOrder: number;
}

// Dashboard types
export interface TrendData {
  period: string;
  asset: number;
  liability: number;
  netWorth: number;
  income?: number;
  expense?: number;
  surplus?: number;
}

export interface AssetDistribution {
  group: AssetGroupType;
  label: string;
  currentAmount: number;
  targetAmount: number;
  percentage: number;
}

export interface BudgetProgress {
  category: string;
  budgetAmount: number;
  spentAmount: number;
  remainingAmount: number;
  executionRate: number;
  isAlert: boolean;
}

// API Response type
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
}
