import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Tabs, Card, Table, Button, Input, InputNumber, Select, message, Popconfirm, Statistic, Row, Col, Spin } from 'antd';
import { PlusOutlined, DeleteOutlined, SaveOutlined } from '@ant-design/icons';
import { monthlyRecordService } from '../../../services/monthlyRecordService';
import { annualPlanService } from '../../../services/annualPlanService';
import { AssetGroup, AssetGroupLabels } from '../../../types';
import type { MonthlyRecord, MonthlyAssetDetail, MonthlyLiabilityDetail, MonthlyIncomeDetail, MonthlyExpenseDetail, AnnualExpense } from '../../../types';

function MonthlyRecordDetail() {
  const { year, month } = useParams<{ year: string; month: string }>();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [isNew, setIsNew] = useState(false);
  const [annualExpenses, setAnnualExpenses] = useState<AnnualExpense[]>([]);
  const [record, setRecord] = useState<MonthlyRecord>({
    year: parseInt(year || new Date().getFullYear().toString()),
    month: parseInt(month || (new Date().getMonth() + 1).toString()),
    totalAsset: 0,
    totalLiability: 0,
    totalIncome: 0,
    totalExpense: 0,
    assetDetails: [],
    liabilityDetails: [],
    incomeDetails: [],
    expenseDetails: [],
  });

  useEffect(() => {
    if (year && month) {
      loadRecord(parseInt(year), parseInt(month));
      loadAnnualExpenses(parseInt(year));
    }
  }, [year, month]);

  const loadRecord = async (y: number, m: number) => {
    setLoading(true);
    try {
      const res = await monthlyRecordService.getByYearAndMonth(y, m);
      if (res.data) {
        setRecord(res.data);
        setIsNew(false);
      }
    } catch {
      // Record not found, try to load previous template with data
      setIsNew(true);
      try {
        const templateRes = await monthlyRecordService.getPreviousTemplate(y, m);
        if (templateRes.data) {
          // 复制上月结构和数据
          setRecord({
            ...templateRes.data,
            id: undefined,
            year: y,
            month: m,
          });
        }
      } catch {
        // No previous template, use empty record
        setRecord({
          year: y,
          month: m,
          totalAsset: 0,
          totalLiability: 0,
          totalIncome: 0,
          totalExpense: 0,
          assetDetails: [],
          liabilityDetails: [],
          incomeDetails: [],
          expenseDetails: [],
        });
      }
    } finally {
      setLoading(false);
    }
  };

  const loadAnnualExpenses = async (y: number) => {
    try {
      const res = await annualPlanService.getByYear(y);
      if (res.data?.annualExpenses) {
        setAnnualExpenses(res.data.annualExpenses);
      }
    } catch {
      setAnnualExpenses([]);
    }
  };

  const recalculateTotals = (r: MonthlyRecord): MonthlyRecord => {
    const totalAsset = r.assetDetails.reduce((sum, d) => sum + (d.amount || 0), 0);
    const totalLiability = r.liabilityDetails.reduce((sum, d) => sum + (d.amount || 0), 0);
    const totalIncome = r.incomeDetails.reduce((sum, d) => sum + (d.amount || 0), 0);
    const totalExpense = r.expenseDetails.reduce((sum, d) => sum + (d.amount || 0), 0);
    return { ...r, totalAsset, totalLiability, totalIncome, totalExpense };
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const dataToSave = recalculateTotals(record);
      if (isNew || !record.id) {
        const res = await monthlyRecordService.create(dataToSave);
        setRecord(res.data);
        setIsNew(false);
        message.success('创建成功');
      } else {
        const res = await monthlyRecordService.update(record.id, dataToSave);
        setRecord(res.data);
        message.success('保存成功');
      }
    } catch (error: unknown) {
      const err = error as Error;
      message.error(err.message || '保存失败');
    } finally {
      setSaving(false);
    }
  };

  // Asset detail table
  const assetColumns = [
    {
      title: '分组',
      dataIndex: 'assetGroup',
      width: 100,
      render: (_: unknown, r: MonthlyAssetDetail, index: number) => (
        <Select value={r.assetGroup} onChange={(v) => updateAsset(index, 'assetGroup', v)} style={{ width: '100%' }}>
          {Object.entries(AssetGroupLabels).map(([k, v]) => (
            <Select.Option key={k} value={k}>{v}</Select.Option>
          ))}
        </Select>
      ),
    },
    {
      title: '名称',
      dataIndex: 'name',
      render: (_: unknown, r: MonthlyAssetDetail, index: number) => (
        <Input value={r.name} onChange={(e) => updateAsset(index, 'name', e.target.value)} />
      ),
    },
    {
      title: '金额(万)',
      dataIndex: 'amount',
      width: 120,
      render: (_: unknown, r: MonthlyAssetDetail, index: number) => (
        <InputNumber value={r.amount} onChange={(v) => updateAsset(index, 'amount', v || 0)} style={{ width: '100%' }} />
      ),
    },
    {
      title: '收益率(%)',
      dataIndex: 'returnRate',
      width: 100,
      render: (_: unknown, r: MonthlyAssetDetail, index: number) => (
        <InputNumber value={r.returnRate} onChange={(v) => updateAsset(index, 'returnRate', v)} style={{ width: '100%' }} />
      ),
    },
    {
      title: '操作',
      width: 60,
      render: (_: unknown, __: MonthlyAssetDetail, index: number) => (
        <Popconfirm title="确定删除?" onConfirm={() => removeAsset(index)}>
          <Button type="link" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  const addAsset = () => {
    setRecord({
      ...record,
      assetDetails: [...record.assetDetails, {
        assetGroup: AssetGroup.LIQUID,
        name: '',
        amount: 0,
        sortOrder: record.assetDetails.length,
      }],
    });
  };

  const updateAsset = (index: number, field: keyof MonthlyAssetDetail, value: unknown) => {
    const newDetails = [...record.assetDetails];
    newDetails[index] = { ...newDetails[index], [field]: value };
    setRecord({ ...record, assetDetails: newDetails });
  };

  const removeAsset = (index: number) => {
    setRecord({ ...record, assetDetails: record.assetDetails.filter((_, i) => i !== index) });
  };

  // Liability detail table
  const liabilityColumns = [
    {
      title: '名称',
      dataIndex: 'name',
      render: (_: unknown, r: MonthlyLiabilityDetail, index: number) => (
        <Input value={r.name} onChange={(e) => updateLiability(index, 'name', e.target.value)} />
      ),
    },
    {
      title: '金额(万)',
      dataIndex: 'amount',
      width: 120,
      render: (_: unknown, r: MonthlyLiabilityDetail, index: number) => (
        <InputNumber value={r.amount} onChange={(v) => updateLiability(index, 'amount', v || 0)} style={{ width: '100%' }} />
      ),
    },
    {
      title: '利率(%)',
      dataIndex: 'interestRate',
      width: 100,
      render: (_: unknown, r: MonthlyLiabilityDetail, index: number) => (
        <InputNumber value={r.interestRate} onChange={(v) => updateLiability(index, 'interestRate', v)} style={{ width: '100%' }} />
      ),
    },
    {
      title: '操作',
      width: 60,
      render: (_: unknown, __: MonthlyLiabilityDetail, index: number) => (
        <Popconfirm title="确定删除?" onConfirm={() => removeLiability(index)}>
          <Button type="link" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  const addLiability = () => {
    setRecord({
      ...record,
      liabilityDetails: [...record.liabilityDetails, {
        name: '',
        amount: 0,
        sortOrder: record.liabilityDetails.length,
      }],
    });
  };

  const updateLiability = (index: number, field: keyof MonthlyLiabilityDetail, value: unknown) => {
    const newDetails = [...record.liabilityDetails];
    newDetails[index] = { ...newDetails[index], [field]: value };
    setRecord({ ...record, liabilityDetails: newDetails });
  };

  const removeLiability = (index: number) => {
    setRecord({ ...record, liabilityDetails: record.liabilityDetails.filter((_, i) => i !== index) });
  };

  // Income detail table
  const incomeColumns = [
    {
      title: '名称',
      dataIndex: 'name',
      render: (_: unknown, r: MonthlyIncomeDetail, index: number) => (
        <Input value={r.name} onChange={(e) => updateIncome(index, 'name', e.target.value)} />
      ),
    },
    {
      title: '金额(万)',
      dataIndex: 'amount',
      width: 120,
      render: (_: unknown, r: MonthlyIncomeDetail, index: number) => (
        <InputNumber value={r.amount} onChange={(v) => updateIncome(index, 'amount', v || 0)} style={{ width: '100%' }} />
      ),
    },
    {
      title: '操作',
      width: 60,
      render: (_: unknown, __: MonthlyIncomeDetail, index: number) => (
        <Popconfirm title="确定删除?" onConfirm={() => removeIncome(index)}>
          <Button type="link" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  const addIncome = () => {
    setRecord({
      ...record,
      incomeDetails: [...record.incomeDetails, {
        name: '',
        amount: 0,
        sortOrder: record.incomeDetails.length,
      }],
    });
  };

  const updateIncome = (index: number, field: keyof MonthlyIncomeDetail, value: unknown) => {
    const newDetails = [...record.incomeDetails];
    newDetails[index] = { ...newDetails[index], [field]: value };
    setRecord({ ...record, incomeDetails: newDetails });
  };

  const removeIncome = (index: number) => {
    setRecord({ ...record, incomeDetails: record.incomeDetails.filter((_, i) => i !== index) });
  };

  // Expense detail table
  const expenseColumns = [
    {
      title: '预算分类',
      dataIndex: 'annualExpenseId',
      width: 140,
      render: (_: unknown, r: MonthlyExpenseDetail, index: number) => (
        <Select 
          value={r.annualExpenseId} 
          onChange={(v) => updateExpense(index, 'annualExpenseId', v)}
          style={{ width: '100%' }}
          allowClear
          placeholder="选择分类"
        >
          {annualExpenses.map((e) => (
            <Select.Option key={e.id} value={e.id}>{e.category}</Select.Option>
          ))}
        </Select>
      ),
    },
    {
      title: '名称',
      dataIndex: 'name',
      render: (_: unknown, r: MonthlyExpenseDetail, index: number) => (
        <Input value={r.name} onChange={(e) => updateExpense(index, 'name', e.target.value)} />
      ),
    },
    {
      title: '金额(万)',
      dataIndex: 'amount',
      width: 120,
      render: (_: unknown, r: MonthlyExpenseDetail, index: number) => (
        <InputNumber value={r.amount} onChange={(v) => updateExpense(index, 'amount', v || 0)} style={{ width: '100%' }} />
      ),
    },
    {
      title: '明细',
      dataIndex: 'detail',
      render: (_: unknown, r: MonthlyExpenseDetail, index: number) => (
        <Input value={r.detail} onChange={(e) => updateExpense(index, 'detail', e.target.value)} placeholder="可选" />
      ),
    },
    {
      title: '操作',
      width: 60,
      render: (_: unknown, __: MonthlyExpenseDetail, index: number) => (
        <Popconfirm title="确定删除?" onConfirm={() => removeExpense(index)}>
          <Button type="link" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  const addExpense = () => {
    setRecord({
      ...record,
      expenseDetails: [...record.expenseDetails, {
        name: '',
        amount: 0,
        sortOrder: record.expenseDetails.length,
      }],
    });
  };

  const updateExpense = (index: number, field: keyof MonthlyExpenseDetail, value: unknown) => {
    const newDetails = [...record.expenseDetails];
    newDetails[index] = { ...newDetails[index], [field]: value };
    setRecord({ ...record, expenseDetails: newDetails });
  };

  const removeExpense = (index: number) => {
    setRecord({ ...record, expenseDetails: record.expenseDetails.filter((_, i) => i !== index) });
  };

  // Calculate current totals for display
  const currentTotals = recalculateTotals(record);
  const surplus = currentTotals.totalIncome - currentTotals.totalExpense;

  const items = [
    {
      key: 'asset',
      label: `资产明细 (${record.assetDetails.length})`,
      children: (
        <div>
          <Button type="dashed" onClick={addAsset} icon={<PlusOutlined />} style={{ marginBottom: 16 }}>
            添加资产项
          </Button>
          <Table
            columns={assetColumns}
            dataSource={record.assetDetails}
            rowKey={(_, i) => `asset-${i}`}
            pagination={false}
            size="small"
          />
        </div>
      ),
    },
    {
      key: 'liability',
      label: `负债明细 (${record.liabilityDetails.length})`,
      children: (
        <div>
          <Button type="dashed" onClick={addLiability} icon={<PlusOutlined />} style={{ marginBottom: 16 }}>
            添加负债项
          </Button>
          <Table
            columns={liabilityColumns}
            dataSource={record.liabilityDetails}
            rowKey={(_, i) => `liability-${i}`}
            pagination={false}
            size="small"
          />
        </div>
      ),
    },
    {
      key: 'income',
      label: `收入明细 (${record.incomeDetails.length})`,
      children: (
        <div>
          <Button type="dashed" onClick={addIncome} icon={<PlusOutlined />} style={{ marginBottom: 16 }}>
            添加收入项
          </Button>
          <Table
            columns={incomeColumns}
            dataSource={record.incomeDetails}
            rowKey={(_, i) => `income-${i}`}
            pagination={false}
            size="small"
          />
        </div>
      ),
    },
    {
      key: 'expense',
      label: `支出明细 (${record.expenseDetails.length})`,
      children: (
        <div>
          <Button type="dashed" onClick={addExpense} icon={<PlusOutlined />} style={{ marginBottom: 16 }}>
            添加支出项
          </Button>
          <Table
            columns={expenseColumns}
            dataSource={record.expenseDetails}
            rowKey={(_, i) => `expense-${i}`}
            pagination={false}
            size="small"
          />
        </div>
      ),
    },
  ];

  return (
    <Spin spinning={loading}>
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <h2 style={{ margin: 0 }}>{year}年{month}月财务记录 {isNew && <span style={{ color: '#1890ff', fontSize: 14 }}>(新建)</span>}</h2>
          <Button type="primary" icon={<SaveOutlined />} onClick={handleSave} loading={saving}>
            保存
          </Button>
        </div>
        
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={8}>
            <Card>
              <Statistic title="当月资产" value={currentTotals.totalAsset} precision={2} prefix="¥" suffix="万" valueStyle={{ color: '#3f8600' }} />
            </Card>
          </Col>
          <Col span={8}>
            <Card>
              <Statistic title="当月负债" value={currentTotals.totalLiability} precision={2} prefix="¥" suffix="万" valueStyle={{ color: '#cf1322' }} />
            </Card>
          </Col>
          <Col span={8}>
            <Card>
              <Statistic title="当月结余" value={surplus} precision={2} prefix="¥" suffix="万" valueStyle={{ color: surplus >= 0 ? '#3f8600' : '#cf1322' }} />
            </Card>
          </Col>
        </Row>

        <Card>
          <Tabs items={items} />
        </Card>
      </div>
    </Spin>
  );
}

export default MonthlyRecordDetail;
