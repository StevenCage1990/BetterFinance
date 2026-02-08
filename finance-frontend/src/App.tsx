import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import Dashboard from './pages/Dashboard';
import AnnualPlan from './pages/AnnualPlan';
import MonthlyRecordList from './pages/MonthlyRecord/List';
import MonthlyRecordDetail from './pages/MonthlyRecord/Detail';
import Budget from './pages/Budget';
import Layout from './components/Layout';
import './App.css';

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<Dashboard />} />
            <Route path="annual-plan/:year" element={<AnnualPlan />} />
            <Route path="monthly-record">
              <Route index element={<MonthlyRecordList />} />
              <Route path=":year/:month" element={<MonthlyRecordDetail />} />
            </Route>
            <Route path="budget/:year" element={<Budget />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
