import api from './api';
import type { MonthlyRecord } from '../types';

export interface MonthlyRecordResponse extends MonthlyRecord {
  netWorth?: number;
  surplus?: number;
  createdAt?: string;
  updatedAt?: string;
}

export const monthlyRecordService = {
  getByYearAndMonth: (year: number, month: number) => 
    api.get<MonthlyRecordResponse>(`/monthly-record/${year}/${month}`),

  getByYear: (year: number) => 
    api.get<MonthlyRecordResponse[]>(`/monthly-record/list?year=${year}`),

  getPreviousTemplate: (year: number, month: number) => 
    api.get<MonthlyRecordResponse>(`/monthly-record/${year}/${month}/previous`),

  create: (data: MonthlyRecord) => 
    api.post<MonthlyRecordResponse>('/monthly-record', data),

  update: (id: number, data: MonthlyRecord) => 
    api.put<MonthlyRecordResponse>(`/monthly-record/${id}`, data),

  delete: (id: number) => 
    api.delete<void>(`/monthly-record/${id}`),
};
