import api from './api';
import type { AnnualBalancePlan } from '../types';

export interface AnnualPlanResponse extends AnnualBalancePlan {
  createdAt?: string;
  updatedAt?: string;
}

export const annualPlanService = {
  getByYear: (year: number) => 
    api.get<AnnualPlanResponse>(`/annual-plan/${year}`),

  create: (data: AnnualBalancePlan) => 
    api.post<AnnualPlanResponse>('/annual-plan', data),

  update: (year: number, data: AnnualBalancePlan) => 
    api.put<AnnualPlanResponse>(`/annual-plan/${year}`, data),

  getSummary: (year: number) => 
    api.get<AnnualPlanResponse>(`/annual-plan/${year}/summary`),

  getAvailableYears: () => 
    api.get<number[]>('/annual-plan/years'),
};
