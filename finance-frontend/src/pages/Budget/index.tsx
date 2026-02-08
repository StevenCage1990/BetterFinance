import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Table, Progress, Select, Row, Col, Statistic, Tag, Empty, Spin, Button, Collapse } from 'antd';
import { WarningOutlined, CheckCircleOutlined, SettingOutlined } from '@ant-design/icons';
import { dashboardService } from '../../services/dashboardService';
import type { AnnualProgressData } from '../../services/dashboardService';
import { annualPlanService } from '../../services/annualPlanService';
import { monthlyRecordService } from '../../services/monthlyRecordService';
import type { BudgetProgress, MonthlyExpenseDetail, AnnualExpense } from '../../types';
import { ExpenseCategory } from '../../types';

interface ExpenseDetailByCategory {
  category: string;
  annualExpenseId?: number;
  details: Array<{
    month: number;
    name: string;
    amount: number;
    detail?: string;
  }>;
  totalSpent: number;
}

function Budget() {
  const { year: yearParam } = useParams<{ year: string }>();
  const navigate = useNavigate();
  const [year, setYear] = useState(parseInt(yearParam || new Date().getFullYear().toString()));
  const [loading, setLoading] = useState(false);
  const [progressData, setProgressData] = useState<AnnualProgressData | null>(null);
  const [expenseDetails, setExpenseDetails] = useState<ExpenseDetailByCategory[]>([]);

  useEffect(() => {
    loadData(year);
  }, [year]);

  const loadData = async (y: number) => {
    setLoading(true);
    try {
      // Load annual progress
      const progressRes = await dashboardService.getAnnualProgress(y);
      setProgressData(progressRes.data);

      // Load monthly records to get expense details
      const recordsRes = await monthlyRecordService.getByYear(y);
      const records = recordsRes.data || [];

      // Load annual plan to get budget categories
      const planRes = await annualPlanService.getByYear(y);
      const annualExpenses = planRes.data?.annualExpenses || [];

      // Group expense details by category
      const categoryMap = new Map<string, ExpenseDetailByCategory>();
      
      // Initialize categories from annual plan - 过滤掉日常开销
      annualExpenses
        .filter((exp: AnnualExpense) => exp.parentCategory !== ExpenseCategory.DAILY)
        .forEach((exp: AnnualExpense) => {
        categoryMap.set(exp.category, {
          category: exp.category,
          annualExpenseId: exp.id,
          details: [],
          totalSpent: 0,
        });
      });

      // Add "未分类" for expenses without category
      categoryMap.set('未分类', {
        category: '未分类',
        details: [],
        totalSpent: 0,
      });

      // Aggregate expense details from monthly records
      records.forEach(record => {
        record.expenseDetails?.forEach((detail: MonthlyExpenseDetail) => {
          // Find category by annualExpenseId
          let categoryName = '未分类';
          if (detail.annualExpenseId) {
            const expense = annualExpenses.find(e => e.id === detail.annualExpenseId);
            if (expense) {
              categoryName = expense.category;
            }
          }

          const category = categoryMap.get(categoryName);
          if (category) {
            category.details.push({
              month: record.month,
              name: detail.name,
              amount: detail.amount || 0,
              detail: detail.detail,
            });
            category.totalSpent += detail.amount || 0;
          }
        });
      });

      // Remove empty "未分类"
      const uncategorized = categoryMap.get('未分类');
      if (uncategorized && uncategorized.details.length === 0) {
        categoryMap.delete('未分类');
      }

      setExpenseDetails(Array.from(categoryMap.values()));
    } catch {
      setProgressData(null);
      setExpenseDetails([]);
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    {
      title: '预算分类',
      dataIndex: 'category',
      key: 'category',
      width: 150,
    },
    {
      title: '预算金额',
      dataIndex: 'budgetAmount',
      key: 'budgetAmount',
      width: 120,
      align: 'right' as const,
      render: (v: number) => `${v?.toFixed(2) || '0.00'}万`,
    },
    {
      title: '已支出',
      dataIndex: 'spentAmount',
      key: 'spentAmount',
      width: 120,
      align: 'right' as const,
      render: (v: number) => `${v?.toFixed(2) || '0.00'}万`,
    },
    {
      title: '剩余',
      dataIndex: 'remainingAmount',
      key: 'remainingAmount',
      width: 120,
      align: 'right' as const,
      render: (v: number) => (
        <span style={{ color: v >= 0 ? '#3f8600' : '#cf1322' }}>
          {v?.toFixed(2) || '0.00'}万
        </span>
      ),
    },
    {
      title: '消耗率',
      dataIndex: 'executionRate',
      key: 'executionRate',
      width: 200,
      render: (rate: number, record: BudgetProgress) => (
        <Progress 
          percent={Math.min(rate, 100)} 
          size="small"
          status={record.isAlert ? 'exception' : 'normal'}
          format={() => `${rate.toFixed(1)}%`}
        />
      ),
    },
    {
      title: '状态',
      key: 'status',
      width: 100,
      render: (_: unknown, record: BudgetProgress) => (
        record.isAlert ? (
          <Tag icon={<WarningOutlined />} color="error">超预警</Tag>
        ) : (
          <Tag icon={<CheckCircleOutlined />} color="success">正常</Tag>
        )
      ),
    },
  ];

  // Calculate summary stats
  const totalBudget = progressData?.budgetProgress?.reduce((sum, b) => sum + (b.budgetAmount || 0), 0) || 0;
  const totalSpent = progressData?.budgetProgress?.reduce((sum, b) => sum + (b.spentAmount || 0), 0) || 0;
  const totalRemaining = totalBudget - totalSpent;
  const overallRate = totalBudget > 0 ? (totalSpent / totalBudget) : 0;

  return (
    <Spin spinning={loading}>
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <h2 style={{ margin: 0 }}>预算执行</h2>
            <Select value={year} onChange={(v) => { setYear(v); navigate(`/budget/${v}`); }} style={{ width: 120 }}>
              {[2024, 2025, 2026, 2027].map(y => (
                <Select.Option key={y} value={y}>{y}年</Select.Option>
              ))}
            </Select>
          </div>
          <Button icon={<SettingOutlined />} onClick={() => navigate(`/annual-plan/${year}`)}>
            管理预算分类
          </Button>
        </div>

        {!progressData?.hasData ? (
          <Card>
            <Empty 
              description={
                <span>
                  暂无预算数据，请先 <a onClick={() => navigate(`/annual-plan/${year}`)}>设置年度预算</a>
                </span>
              } 
            />
          </Card>
        ) : (
          <>
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={6}>
                <Card>
                  <Statistic 
                    title="年度总预算" 
                    value={totalBudget} 
                    precision={2} 
                    suffix="万"
                  />
                </Card>
              </Col>
              <Col span={6}>
                <Card>
                  <Statistic 
                    title="已支出" 
                    value={totalSpent} 
                    precision={2} 
                    suffix="万"
                    valueStyle={{ color: '#cf1322' }}
                  />
                </Card>
              </Col>
              <Col span={6}>
                <Card>
                  <Statistic 
                    title="剩余预算" 
                    value={totalRemaining} 
                    precision={2} 
                    suffix="万"
                    valueStyle={{ color: totalRemaining >= 0 ? '#3f8600' : '#cf1322' }}
                  />
                </Card>
              </Col>
              <Col span={6}>
                <Card>
                  <div style={{ marginBottom: 8 }}>整体消耗率</div>
                  <Progress 
                    percent={overallRate * 100} 
                    status={overallRate > 0.9 ? 'exception' : 'normal'}
                    format={() => `${(overallRate * 100).toFixed(1)}%`}
                  />
                </Card>
              </Col>
            </Row>

            <Card title="预算分类执行情况" style={{ marginBottom: 16 }}>
              <Table 
                columns={columns} 
                dataSource={progressData?.budgetProgress || []}
                rowKey="category"
                pagination={false}
              />
            </Card>

            <Card title="支出明细追溯">
              {expenseDetails.length === 0 ? (
                <Empty description="暂无支出记录" />
              ) : (
                <Collapse>
                  {expenseDetails.map(category => (
                    <Collapse.Panel 
                      key={category.category}
                      header={
                        <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', paddingRight: 24 }}>
                          <span>{category.category}</span>
                          <span>合计：{category.totalSpent.toFixed(2)}万 ({category.details.length}笔)</span>
                        </div>
                      }
                    >
                      <Table
                        size="small"
                        dataSource={category.details}
                        rowKey={(_, i) => `${category.category}-${i}`}
                        pagination={false}
                        columns={[
                          { title: '月份', dataIndex: 'month', width: 80, render: (m: number) => `${m}月` },
                          { title: '名称', dataIndex: 'name' },
                          { title: '金额', dataIndex: 'amount', width: 100, align: 'right', render: (v: number) => `${v.toFixed(2)}万` },
                          { title: '明细', dataIndex: 'detail', ellipsis: true },
                        ]}
                      />
                    </Collapse.Panel>
                  ))}
                </Collapse>
              )}
            </Card>
          </>
        )}
      </div>
    </Spin>
  );
}

export default Budget;
