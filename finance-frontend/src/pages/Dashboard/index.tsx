import { useEffect, useState } from 'react';
import { Card, Row, Col, Table, message, DatePicker, Empty } from 'antd';
import ReactECharts from 'echarts-for-react';
import dayjs from 'dayjs';
import { dashboardService, type AnnualTargetTrendData, type BudgetPieData, type AnnualProgressData } from '../../services/dashboardService';
import type { TrendData, BudgetProgress } from '../../types';

function Dashboard() {
  const [year, setYear] = useState(dayjs().year());
  const [loading, setLoading] = useState(false);
  const [targetTrendData, setTargetTrendData] = useState<AnnualTargetTrendData | null>(null);
  const [incomeExpenseTrend, setIncomeExpenseTrend] = useState<TrendData[]>([]);
  const [budgetPieData, setBudgetPieData] = useState<BudgetPieData | null>(null);
  const [budgetProgress, setBudgetProgress] = useState<BudgetProgress[]>([]);

  useEffect(() => {
    loadData();
  }, [year]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [targetRes, incomeRes, budgetPieRes, progressRes] = await Promise.all([
        dashboardService.getAnnualTargetTrend(year),
        dashboardService.getIncomeExpenseTrend(year),
        dashboardService.getBudgetPie(year),
        dashboardService.getAnnualProgress(year),
      ]);
      setTargetTrendData(targetRes.data);
      setIncomeExpenseTrend(incomeRes.data || []);
      setBudgetPieData(budgetPieRes.data);
      setBudgetProgress((progressRes.data as AnnualProgressData)?.budgetProgress || []);
    } catch (error: unknown) {
      const err = error as Error;
      message.error(err.message || '加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  // 模块一：年度目标追踪（两个横向条形图）
  const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
  
  // 资产增值图 - 横向条形图
  const assetGrowthOption = {
    title: { text: '资产增值进度', left: 'center' },
    tooltip: { 
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: Array<{name: string; value: number | null; marker: string}>) => {
        const p = params[0];
        return p.value !== null ? `${p.name}: ${p.value.toFixed(2)}万` : `${p.name}: 暂无数据`;
      }
    },
    grid: { left: '8%', right: '15%', top: '15%', bottom: '5%' },
    xAxis: { 
      type: 'value', 
      name: '万元',
      min: 0,
      max: 100
    },
    yAxis: { 
      type: 'category', 
      data: months,
      inverse: true
    },
    series: [
      { 
        name: '资产', 
        type: 'bar', 
        data: targetTrendData?.monthlyData?.map(d => d.assetActual ?? '-') || [],
        itemStyle: { color: '#1890ff' },
        label: {
          show: true,
          position: 'right',
          formatter: (params: {value: number | null}) => params.value !== null ? `${params.value.toFixed(2)}万` : ''
        },
        markLine: {
          silent: true,
          symbol: 'none',
          lineStyle: { type: 'dashed', color: '#52c41a', width: 2 },
          label: { 
            formatter: '目标: {c}万',
            position: 'insideEndTop'
          },
          data: targetTrendData?.assetTargetTotal ? [
            { xAxis: targetTrendData.assetTargetTotal }
          ] : []
        }
      },
    ],
  };

  // 负债降低图 - 横向条形图
  const liabilityReductionOption = {
    title: { text: '负债降低进度', left: 'center' },
    tooltip: { 
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: Array<{name: string; value: number | null; marker: string}>) => {
        const p = params[0];
        return p.value !== null ? `${p.name}: ${p.value.toFixed(2)}万` : `${p.name}: 暂无数据`;
      }
    },
    grid: { left: '8%', right: '15%', top: '15%', bottom: '5%' },
    xAxis: { 
      type: 'value', 
      name: '万元',
      inverse: true,
      min: 300,
      max: 450
    },
    yAxis: { 
      type: 'category', 
      data: months,
      inverse: true,
      position: 'right'
    },
    series: [
      { 
        name: '负债', 
        type: 'bar', 
        data: targetTrendData?.monthlyData?.map(d => d.liabilityActual ?? '-') || [],
        itemStyle: { color: '#ff4d4f' },
        label: {
          show: true,
          position: 'left',
          formatter: (params: {value: number | null}) => params.value !== null ? `${params.value.toFixed(2)}万` : ''
        },
        markLine: {
          silent: true,
          symbol: 'none',
          lineStyle: { type: 'dashed', color: '#52c41a', width: 2 },
          label: { 
            formatter: '目标: {c}万',
            position: 'insideEndTop'
          },
          data: targetTrendData?.liabilityTargetTotal ? [
            { xAxis: targetTrendData.liabilityTargetTotal }
          ] : []
        }
      },
    ],
  };

  // 模块二：月度收支趋势
  const incomeExpenseOption = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出', '盈余'] },
    xAxis: { type: 'category', data: incomeExpenseTrend.map(d => d.period) },
    yAxis: { type: 'value', name: '万元' },
    series: [
      { name: '收入', type: 'bar', data: incomeExpenseTrend.map(d => d.income), itemStyle: { color: '#52c41a' } },
      { name: '支出', type: 'bar', data: incomeExpenseTrend.map(d => d.expense), itemStyle: { color: '#faad14' } },
      { name: '盈余', type: 'line', data: incomeExpenseTrend.map(d => d.surplus), itemStyle: { color: '#1890ff' } },
    ],
  };

  // 模块三：预算消耗饼图
  const budgetPieOption = {
    title: {
      text: '预算消耗分布',
      subtext: `总预算: ${budgetPieData?.totalBudget?.toFixed(2) || 0}万 | 已消耗: ${budgetPieData?.totalSpent?.toFixed(2) || 0}万`,
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}万 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    series: [
      {
        name: '预算消耗',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '55%'],
        data: budgetPieData?.categories?.map(c => ({
          name: c.category,
          value: c.spentAmount
        })) || [],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          formatter: '{b}: {d}%'
        }
      }
    ]
  };

  // 预算明细表格列定义
  const budgetColumns = [
    { title: '分类', dataIndex: 'category', key: 'category' },
    { 
      title: '预算', 
      dataIndex: 'budgetAmount', 
      key: 'budgetAmount',
      render: (v: number) => `${v?.toFixed(2) || 0}万`
    },
    { 
      title: '已消耗', 
      dataIndex: 'spentAmount', 
      key: 'spentAmount',
      render: (v: number) => `${v?.toFixed(2) || 0}万`
    },
    { 
      title: '剩余', 
      dataIndex: 'remainingAmount', 
      key: 'remainingAmount',
      render: (v: number) => `${v?.toFixed(2) || 0}万`
    },
    { 
      title: '消耗率', 
      dataIndex: 'executionRate', 
      key: 'executionRate',
      render: (rate: number) => {
        const color = rate > 100 ? '#ff4d4f' : rate > 80 ? '#faad14' : '#52c41a';
        return <span style={{ color }}>{rate?.toFixed(1) || 0}%</span>;
      }
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>数据看板</h2>
        <DatePicker 
          picker="year" 
          value={dayjs().year(year)}
          onChange={(date) => {
            if (date) {
              setYear(date.year());
            }
          }}
        />
      </div>
      
      {/* 模块一：年度目标追踪 */}
      <Card title="年度目标追踪" loading={loading} style={{ marginBottom: 16 }}>
        {targetTrendData?.hasTarget ? (
          <Row gutter={16}>
            <Col span={12}>
              <ReactECharts option={assetGrowthOption} style={{ height: 400 }} />
            </Col>
            <Col span={12}>
              <ReactECharts option={liabilityReductionOption} style={{ height: 400 }} />
            </Col>
          </Row>
        ) : (
          <Empty description="暂无年度规划，请先配置年度规划" />
        )}
      </Card>

      {/* 模块二：月度收支趋势 */}
      <Card title="月度收支趋势" loading={loading} style={{ marginBottom: 16 }}>
        {incomeExpenseTrend.length > 0 ? (
          <ReactECharts option={incomeExpenseOption} style={{ height: 300 }} />
        ) : (
          <Empty description="暂无月度记录数据" />
        )}
      </Card>

      {/* 模块三：预算管理 */}
      <Card title="预算管理" loading={loading}>
        {budgetPieData?.hasData ? (
          <Row gutter={16}>
            <Col span={12}>
              <ReactECharts option={budgetPieOption} style={{ height: 350 }} />
            </Col>
            <Col span={12}>
              <Table 
                dataSource={budgetProgress}
                columns={budgetColumns}
                rowKey="category"
                pagination={false}
                size="small"
              />
            </Col>
          </Row>
        ) : (
          <Empty description="暂无预算数据" />
        )}
      </Card>
    </div>
  );
}

export default Dashboard;
