import api from './api';

export interface ImportResult {
  success: boolean;
  messages: string[];
}

export const dataService = {
  // Export APIs
  exportAnnualPlan: (year: number) =>
    api.get(`/data/export/annual-plan/${year}`, { responseType: 'blob' }),

  exportMonthlyRecords: (year: number) =>
    api.get(`/data/export/monthly-records/${year}`, { responseType: 'blob' }),

  exportMonthlyRecordsCsv: (year: number) =>
    api.get(`/data/export/monthly-records/${year}/csv`, { responseType: 'blob' }),

  exportFullData: (year: number) =>
    api.get(`/data/export/full/${year}`, { responseType: 'blob' }),

  // Import APIs
  importAnnualPlan: (year: number, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post<ImportResult>(`/data/import/annual-plan/${year}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  importMonthlyRecord: (year: number, month: number, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post<ImportResult>(`/data/import/monthly-record/${year}/${month}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // Helper to trigger download
  downloadBlob: (blob: Blob, filename: string) => {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },
};
