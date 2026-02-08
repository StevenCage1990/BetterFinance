import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Tabs, Card, Table, Button, Input, InputNumber, Select, message, Popconfirm, Statistic, Row, Col } from 'antd';
import { PlusOutlined, DeleteOutlined, SaveOutlined } from '@ant-design/icons';
import { annualPlanService } from '../../services/annualPlanService';
import { AssetGroup, AssetGroupLabels, IncomeType, IncomeTypeLabels, ExpenseCategory, ExpenseCategoryLabels, LiabilityGroup, LiabilityGroupLabels } from '../../types';
import type { AnnualBalancePlan, AssetTarget, LiabilityTarget, AnnualIncome, AnnualExpense } from '../../types';

function AnnualPlan() {
  const { year } = useParams<{ year: string }>();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [plan, setPlan] = useState<AnnualBalancePlan>({
    year: parseInt(year || new Date().getFullYear().toString()),
    assetTargets: [],
    liabilityTargets: [],
    annualIncomes: [],
    annualExpenses: [],
  });

  useEffect(() => {
    if (year) {
      loadPlan(parseInt(year));
    }
  }, [year]);

  const loadPlan = async (y: number) => {
    setLoading(true);
    try {
      const res = await annualPlanService.getByYear(y);
      setPlan(res.data);
    } catch {
      setPlan({
        year: y,
        assetTargets: [],
        liabilityTargets: [],
        annualIncomes: [],
        annualExpenses: [],
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const res = await annualPlanService.update(plan.year, plan);
      setPlan(res.data);
      message.success('保存成功');
    } catch (error: unknown) {
      const err = error as Error;
      message.error(err.message || '保存失败');
    } finally {
      setSaving(false);
    }
  };

  // Calculate income summaries
  const monthlyIncomeTotal = plan.annualIncomes
    .filter(i => i.isMonthly)
    .reduce((sum, i) => sum + (i.amount || 0), 0);
  const annualIncomeTotal = plan.annualIncomes
    .filter(i => !i.isMonthly)
    .reduce((sum, i) => sum + (i.amount || 0), 0);
  const totalYearIncome = monthlyIncomeTotal * 12 + annualIncomeTotal;

  // Calculate expense summary
  const monthlyExpenseTotal = plan.annualExpenses
    .filter(e => e.isMonthly)
    .reduce((sum, e) => sum + (e.budgetAmount || 0), 0);
  const annualExpenseTotal = plan.annualExpenses
    .filter(e => !e.isMonthly)
    .reduce((sum, e) => sum + (e.budgetAmount || 0), 0);
  const totalYearExpense = monthlyExpenseTotal * 12 + annualExpenseTotal;

  // Calculate asset/liability totals
  const totalAssetTarget = plan.assetTargets
    .reduce((sum, a) => sum + (a.targetAmount || 0), 0);
  const totalLiabilityTarget = plan.liabilityTargets
    .reduce((sum, l) => sum + (l.targetBalance || 0), 0);

  // Income table columns
  const incomeColumns = [
    {
      title: '分组',
      dataIndex: 'incomeType',
      width: 100,
      render: (_: unknown, record: AnnualIncome, index: number) => (
        <Select
          value={record.incomeType}
          onChange={(v) => updateIncome(index, 'incomeType', v)}
          style={{ width: '100%' }}
          size="small"
        >
          {Object.entries(IncomeTypeLabels).map(([k, v]) => (
            <Select.Option key={k} value={k}>{v}</Select.Option>
          ))}
        </Select>
      ),
    },
    {
      title: '名称',
      dataIndex: 'name',
      render: (_: unknown, record: AnnualIncome, index: number) => (
        <Input value={record.name} onChange={(e) => updateIncome(index, 'name', e.target.value)} size="small" />
      ),
    },
    {
      title: '金额(万)',
      dataIndex: 'amount',
      width: 90,
      render: (_: unknown, record: AnnualIncome, index: number) => (
        <InputNumber value={record.amount} onChange={(v) => updateIncome(index, 'amount', v || 0)} style={{ width: '100%' }} size="small" />
      ),
    },
    {
      title: '月度/年度',
      dataIndex: 'isMonthly',
      width: 100,
      render: (_: unknown, record: AnnualIncome, index: number) => (
        <Select value={record.isMonthly} onChange={(v) => updateIncome(index, 'isMonthly', v)} style={{ width: '100%' }} size="small">
          <Select.Option value={true}>月度</Select.Option>
          <Select.Option value={false}>年度</Select.Option>
        </Select>
      ),
    },
    {
      title: '',
      width: 40,
      render: (_: unknown, __: AnnualIncome, index: number) => (
        <Popconfirm title="确定删除?" onConfirm={() => removeIncome(index)}>
          <Button type="link" danger icon={<DeleteOutlined />} size="small" />
        </Popconfirm>
      ),
    },
  ];

  const addIncome = () => {
    setPlan({
      ...plan,
      annualIncomes: [...plan.annualIncomes, {
        incomeType: IncomeType.SALARY,
        name: '',
        amount: 0,
        isMonthly: true,
        sortOrder: plan.annualIncomes.length,
      }],
    });
  };

  const updateIncome = (index: number, field: keyof AnnualIncome, value: unknown) => {
    const newIncomes = [...plan.annualIncomes];
    newIncomes[index] = { ...newIncomes[index], [field]: value };
    setPlan({ ...plan, annualIncomes: newIncomes });
  };

  const removeIncome = (index: number) => {
    setPlan({ ...plan, annualIncomes: plan.annualIncomes.filter((_, i) => i !== index) });
  };

  // Expense budget table columns
  const expenseColumns = [
    {
      title: '分组',
      dataIndex: 'parentCategory',
      width: 110,
      render: (_: unknown, record: AnnualExpense, index: number) => (
        <Select 
          value={record.parentCategory} 
          onChange={(v) => updateExpense(index, 'parentCategory', v)} 
          style={{ width: '100%' }}
          size="small"
        >
          {Object.entries(ExpenseCategoryLabels).map(([k, v]) => (
            <Select.Option key={k} value={k}>{v}</Select.Option>
          ))}
        </Select>
      ),
    },
    {
      title: '名称',
      dataIndex: 'category',
      render: (_: unknown, record: AnnualExpense, index: number) => (
        <Input value={record.category} onChange={(e) => updateExpense(index, 'category', e.target.value)} size="small" />
      ),
    },
    {
      title: '金额(万)',
      dataIndex: 'budgetAmount',
      width: 90,
      render: (_: unknown, record: AnnualExpense, index: number) => (
        <InputNumber value={record.budgetAmount} onChange={(v) => updateExpense(index, 'budgetAmount', v || 0)} style={{ width: '100%' }} size="small" />
      ),
    },
    {
      title: '月度/年度',
      dataIndex: 'isMonthly',
      width: 100,
      render: (_: unknown, record: AnnualExpense, index: number) => (
        <Select value={record.isMonthly} onChange={(v) => updateExpense(index, 'isMonthly', v)} style={{ width: '100%' }} size="small">
          <Select.Option value={true}>月度</Select.Option>
          <Select.Option value={false}>年度</Select.Option>
        </Select>
      ),
    },
    {
      title: '',
      width: 40,
      render: (_: unknown, __: AnnualExpense, index: number) => (
        <Popconfirm title="确定删除?" onConfirm={() => removeExpense(index)}>
          <Button type="link" danger icon={<DeleteOutlined />} size="small" />
        </Popconfirm>
      ),
    },
  ];

  const addExpense = () => {
    setPlan({
      ...plan,
      annualExpenses: [...plan.annualExpenses, {
        parentCategory: ExpenseCategory.DAILY,
        category: '',
        budgetAmount: 0,
        isMonthly: true,
        sortOrder: plan.annualExpenses.length,
      }],
    });
  };

  const updateExpense = (index: number, field: keyof AnnualExpense, value: unknown) => {
    const newExpenses = [...plan.annualExpenses];
    newExpenses[index] = { ...newExpenses[index], [field]: value };
    setPlan({ ...plan, annualExpenses: newExpenses });
  };

  const removeExpense = (index: number) => {
    setPlan({ ...plan, annualExpenses: plan.annualExpenses.filter((_, i) => i !== index) });
  };

  // Asset target table columns
  const assetColumns = [
    {
      title: '分组',
      dataIndex: 'assetGroup',
      width: 80,
      render: (_: unknown, record: AssetTarget, index: number) => (
        <Select value={record.assetGroup} onChange={(v) => updateAsset(index, 'assetGroup', v)} style={{ width: '100%' }} size="small">
          {Object.entries(AssetGroupLabels).map(([k, v]) => (
            <Select.Option key={k} value={k}>{v}</Select.Option>
          ))}
        </Select>
      ),
    },
    {
      title: '名称',
      dataIndex: 'name',
      render: (_: unknown, record: AssetTarget, index: number) => (
        <Input value={record.name} onChange={(e) => updateAsset(index, 'name', e.target.value)} size="small" />
      ),
    },
    {
      title: '目标(万)',
      dataIndex: 'targetAmount',
      width: 90,
      render: (_: unknown, record: AssetTarget, index: number) => (
        <InputNumber value={record.targetAmount} onChange={(v) => updateAsset(index, 'targetAmount', v || 0)} style={{ width: '100%' }} size="small" />
      ),
    },
    {
      title: '收益率(%)',
      dataIndex: 'expectedReturnRate',
      width: 90,
      render: (_: unknown, record: AssetTarget, index: number) => (
        <InputNumber value={record.expectedReturnRate} onChange={(v) => updateAsset(index, 'expectedReturnRate', v)} style={{ width: '100%' }} size="small" />
      ),
    },
    {
      title: '',
      width: 40,
      render: (_: unknown, __: AssetTarget, index: number) => (
        <Popconfirm title="确定删除?" onConfirm={() => removeAsset(index)}>
          <Button type="link" danger icon={<DeleteOutlined />} size="small" />
        </Popconfirm>
      ),
    },
  ];

  const addAsset = () => {
    setPlan({
      ...plan,
      assetTargets: [...plan.assetTargets, {
        assetGroup: AssetGroup.LIQUID,
        name: '',
        targetAmount: 0,
        sortOrder: plan.assetTargets.length,
      }],
    });
  };

  const updateAsset = (index: number, field: keyof AssetTarget, value: unknown) => {
    const newAssets = [...plan.assetTargets];
    newAssets[index] = { ...newAssets[index], [field]: value };
    setPlan({ ...plan, assetTargets: newAssets });
  };

  const removeAsset = (index: number) => {
    setPlan({ ...plan, assetTargets: plan.assetTargets.filter((_, i) => i !== index) });
  };

  // Liability target table columns
  const liabilityColumns = [
    {
      title: '分组',
      dataIndex: 'liabilityGroup',
      width: 80,
      render: (_: unknown, record: LiabilityTarget, index: number) => (
        <Select value={record.liabilityGroup} onChange={(v) => updateLiability(index, 'liabilityGroup', v)} style={{ width: '100%' }} size="small">
          {Object.entries(LiabilityGroupLabels).map(([k, v]) => (
            <Select.Option key={k} value={k}>{v}</Select.Option>
          ))}
        </Select>
      ),
    },
    {
      title: '名称',
      dataIndex: 'name',
      render: (_: unknown, record: LiabilityTarget, index: number) => (
        <Input value={record.name} onChange={(e) => updateLiability(index, 'name', e.target.value)} size="small" />
      ),
    },
    {
      title: '余额(万)',
      dataIndex: 'targetBalance',
      width: 90,
      render: (_: unknown, record: LiabilityTarget, index: number) => (
        <InputNumber value={record.targetBalance} onChange={(v) => updateLiability(index, 'targetBalance', v || 0)} style={{ width: '100%' }} size="small" />
      ),
    },
    {
      title: '利率(%)',
      dataIndex: 'interestRate',
      width: 80,
      render: (_: unknown, record: LiabilityTarget, index: number) => (
        <InputNumber value={record.interestRate} onChange={(v) => updateLiability(index, 'interestRate', v)} style={{ width: '100%' }} size="small" />
      ),
    },
    {
      title: '',
      width: 40,
      render: (_: unknown, __: LiabilityTarget, index: number) => (
        <Popconfirm title="确定删除?" onConfirm={() => removeLiability(index)}>
          <Button type="link" danger icon={<DeleteOutlined />} size="small" />
        </Popconfirm>
      ),
    },
  ];

  const addLiability = () => {
    setPlan({
      ...plan,
      liabilityTargets: [...plan.liabilityTargets, {
        liabilityGroup: LiabilityGroup.LOAN,
        name: '',
        targetBalance: 0,
        sortOrder: plan.liabilityTargets.length,
      }],
    });
  };

  const updateLiability = (index: number, field: keyof LiabilityTarget, value: unknown) => {
    const newLiabilities = [...plan.liabilityTargets];
    newLiabilities[index] = { ...newLiabilities[index], [field]: value };
    setPlan({ ...plan, liabilityTargets: newLiabilities });
  };

  const removeLiability = (index: number) => {
    setPlan({ ...plan, liabilityTargets: plan.liabilityTargets.filter((_, i) => i !== index) });
  };

  const items = [
    {
      key: 'income-expense',
      label: '收入支出',
      children: (
        <div>
          {/* Summary Cards - Row 1: Income */}
          <Row gutter={16} style={{ marginBottom: 8 }}>
            <Col span={8}>
              <Card size="small">
                <Statistic title="月度收入" value={monthlyIncomeTotal} precision={2} suffix="万/月" valueStyle={{ color: '#3f8600' }} />
              </Card>
            </Col>
            <Col span={8}>
              <Card size="small">
                <Statistic title="年度收入" value={annualIncomeTotal} precision={2} suffix="万/年" valueStyle={{ color: '#3f8600' }} />
              </Card>
            </Col>
            <Col span={8}>
              <Card size="small">
                <Statistic title="年度总收入" value={totalYearIncome} precision={2} suffix="万" valueStyle={{ color: '#3f8600', fontWeight: 'bold' }} />
              </Card>
            </Col>
          </Row>
          {/* Summary Cards - Row 2: Expense */}
          <Row gutter={16} style={{ marginBottom: 8 }}>
            <Col span={8}>
              <Card size="small">
                <Statistic title="月度支出" value={monthlyExpenseTotal} precision={2} suffix="万/月" valueStyle={{ color: '#cf1322' }} />
              </Card>
            </Col>
            <Col span={8}>
              <Card size="small">
                <Statistic title="年度支出" value={annualExpenseTotal} precision={2} suffix="万/年" valueStyle={{ color: '#cf1322' }} />
              </Card>
            </Col>
            <Col span={8}>
              <Card size="small">
                <Statistic title="年度总支出" value={totalYearExpense} precision={2} suffix="万" valueStyle={{ color: '#cf1322', fontWeight: 'bold' }} />
              </Card>
            </Col>
          </Row>
          {/* Summary Cards - Row 3: Surplus */}
          <Row gutter={16} style={{ marginBottom: 16 }}>
            <Col span={8}>
              <Card size="small">
                <Statistic title="年度结余" value={plan.annualSurplus || 0} precision={2} suffix="万" valueStyle={{ fontWeight: 'bold' }} />
              </Card>
            </Col>
          </Row>

          {/* Left-Right Layout */}
          <Row gutter={24}>
            {/* Left: Income */}
            <Col span={12}>
              <Card 
                title="收入项" 
                size="small"
                extra={
                  <Button type="dashed" onClick={addIncome} icon={<PlusOutlined />} size="small">
                    添加
                  </Button>
                }
              >
                <Table
                  columns={incomeColumns}
                  dataSource={plan.annualIncomes}
                  rowKey={(_, i) => `income-${i}`}
                  pagination={false}
                  size="small"
                  scroll={{ y: 400 }}
                />
              </Card>
            </Col>

            {/* Right: Expense */}
            <Col span={12}>
              <Card 
                title="支出项" 
                size="small"
                extra={
                  <Button type="dashed" onClick={addExpense} icon={<PlusOutlined />} size="small">
                    添加
                  </Button>
                }
              >
                <Table
                  columns={expenseColumns}
                  dataSource={plan.annualExpenses}
                  rowKey={(_, i) => `expense-${i}`}
                  pagination={false}
                  size="small"
                  scroll={{ y: 400 }}
                />
              </Card>
            </Col>
          </Row>
        </div>
      ),
    },
    {
      key: 'asset-liability',
      label: '资产负债',
      children: (
        <div>
          {/* Summary Cards */}
          <Row gutter={16} style={{ marginBottom: 16 }}>
            <Col span={12}>
              <Card size="small">
                <Statistic title="资产目标" value={totalAssetTarget} precision={2} suffix="万" valueStyle={{ color: '#3f8600' }} />
              </Card>
            </Col>
            <Col span={12}>
              <Card size="small">
                <Statistic title="负债目标" value={totalLiabilityTarget} precision={2} suffix="万" valueStyle={{ color: '#cf1322' }} />
              </Card>
            </Col>
          </Row>

          {/* Left-Right Layout */}
          <Row gutter={24}>
            {/* Left: Asset */}
            <Col span={12}>
              <Card 
                title="资产目标" 
                size="small"
                extra={
                  <Button type="dashed" onClick={addAsset} icon={<PlusOutlined />} size="small">
                    添加
                  </Button>
                }
              >
                <Table
                  columns={assetColumns}
                  dataSource={plan.assetTargets}
                  rowKey={(_, i) => `asset-${i}`}
                  pagination={false}
                  size="small"
                  scroll={{ y: 400 }}
                />
              </Card>
            </Col>

            {/* Right: Liability */}
            <Col span={12}>
              <Card 
                title="负债目标" 
                size="small"
                extra={
                  <Button type="dashed" onClick={addLiability} icon={<PlusOutlined />} size="small">
                    添加
                  </Button>
                }
              >
                <Table
                  columns={liabilityColumns}
                  dataSource={plan.liabilityTargets}
                  rowKey={(_, i) => `liability-${i}`}
                  pagination={false}
                  size="small"
                  scroll={{ y: 400 }}
                />
              </Card>
            </Col>
          </Row>
        </div>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>{year}年度规划</h2>
        <Button type="primary" icon={<SaveOutlined />} onClick={handleSave} loading={saving}>
          保存
        </Button>
      </div>
      <Card loading={loading}>
        <Tabs items={items} />
      </Card>
    </div>
  );
}

export default AnnualPlan;
