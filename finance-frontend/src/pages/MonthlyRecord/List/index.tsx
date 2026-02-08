import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Button, Card, Select, Modal, Row, Col, Statistic, Tag, message } from 'antd';
import { PlusOutlined, EditOutlined } from '@ant-design/icons';
import { monthlyRecordService } from '../../../services/monthlyRecordService';
import type { MonthlyRecordResponse } from '../../../services/monthlyRecordService';
import dayjs from 'dayjs';

function MonthlyRecordList() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [year, setYear] = useState(dayjs().year());
  const [records, setRecords] = useState<MonthlyRecordResponse[]>([]);
  const [newMonthModalOpen, setNewMonthModalOpen] = useState(false);
  const [selectedMonth, setSelectedMonth] = useState<number | undefined>();

  useEffect(() => {
    loadRecords(year);
  }, [year]);

  const loadRecords = async (y: number) => {
    setLoading(true);
    try {
      const res = await monthlyRecordService.getByYear(y);
      setRecords(res.data || []);
    } catch {
      setRecords([]);
    } finally {
      setLoading(false);
    }
  };

  const existingMonths = records.map(r => r.month);
  const availableMonths = Array.from({ length: 12 }, (_, i) => i + 1).filter(m => !existingMonths.includes(m));

  const handleCreateNew = () => {
    if (!selectedMonth) {
      message.warning('请选择月份');
      return;
    }
    setNewMonthModalOpen(false);
    navigate(`/monthly-record/${year}/${selectedMonth}`);
  };

  const columns = [
    { 
      title: '月份', 
      dataIndex: 'month', 
      key: 'month', 
      width: 100,
      render: (m: number) => <Tag color="blue">{m}月</Tag>,
    },
    { 
      title: '总资产', 
      dataIndex: 'totalAsset', 
      key: 'totalAsset',
      align: 'right' as const,
      render: (v: number) => <span style={{ color: '#3f8600' }}>{v?.toFixed(2) || '0.00'}万</span>,
    },
    { 
      title: '总负债', 
      dataIndex: 'totalLiability', 
      key: 'totalLiability',
      align: 'right' as const,
      render: (v: number) => <span style={{ color: '#cf1322' }}>{v?.toFixed(2) || '0.00'}万</span>,
    },
    {
      title: '结余',
      key: 'surplus',
      align: 'right' as const,
      render: (_: unknown, record: MonthlyRecordResponse) => {
        const surplus = (record.totalIncome || 0) - (record.totalExpense || 0);
        return <span style={{ color: surplus >= 0 ? '#3f8600' : '#cf1322' }}>{surplus.toFixed(2)}万</span>;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_: unknown, record: MonthlyRecordResponse) => (
        <Button 
          type="link" 
          icon={<EditOutlined />}
          onClick={() => navigate(`/monthly-record/${record.year}/${record.month}`)}
        >
          编辑
        </Button>
      ),
    },
  ];

  // Calculate year totals
  const yearTotals = records.reduce((acc, r) => ({
    income: acc.income + (r.totalIncome || 0),
    expense: acc.expense + (r.totalExpense || 0),
  }), { income: 0, expense: 0 });

  // Get latest record for current status
  const latestRecord = records.length > 0 ? records.reduce((a, b) => a.month > b.month ? a : b) : null;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <h2 style={{ margin: 0 }}>月度记录</h2>
          <Select value={year} onChange={setYear} style={{ width: 120 }}>
            {[2024, 2025, 2026, 2027].map(y => (
              <Select.Option key={y} value={y}>{y}年</Select.Option>
            ))}
          </Select>
        </div>
        <Button 
          type="primary" 
          icon={<PlusOutlined />}
          onClick={() => {
            setSelectedMonth(availableMonths[0]);
            setNewMonthModalOpen(true);
          }}
          disabled={availableMonths.length === 0}
        >
          新建月度记录
        </Button>
      </div>

      {latestRecord && (
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card size="small">
              <Statistic 
                title={`最新资产 (${latestRecord.month}月)`}
                value={latestRecord.totalAsset || 0} 
                precision={2} 
                suffix="万"
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic 
                title={`最新负债 (${latestRecord.month}月)`}
                value={latestRecord.totalLiability || 0} 
                precision={2} 
                suffix="万"
                valueStyle={{ color: '#cf1322' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic 
                title="年度累计收入"
                value={yearTotals.income} 
                precision={2} 
                suffix="万"
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic 
                title="年度累计支出"
                value={yearTotals.expense} 
                precision={2} 
                suffix="万"
              />
            </Card>
          </Col>
        </Row>
      )}

      <Card>
        <Table 
          columns={columns} 
          dataSource={records.sort((a, b) => b.month - a.month)} 
          rowKey={(r) => `${r.year}-${r.month}`}
          loading={loading}
          pagination={false}
        />
      </Card>

      <Modal
        title="新建月度记录"
        open={newMonthModalOpen}
        onOk={handleCreateNew}
        onCancel={() => setNewMonthModalOpen(false)}
        okText="创建"
        cancelText="取消"
      >
        <div style={{ marginBottom: 16 }}>
          <span>选择月份：</span>
          <Select 
            value={selectedMonth} 
            onChange={setSelectedMonth}
            style={{ width: 120, marginLeft: 8 }}
            placeholder="选择月份"
          >
            {availableMonths.map(m => (
              <Select.Option key={m} value={m}>{m}月</Select.Option>
            ))}
          </Select>
        </div>
        <p style={{ color: '#666' }}>
          创建后将自动从上月记录复制结构和数据（如存在）
        </p>
      </Modal>
    </div>
  );
}

export default MonthlyRecordList;
