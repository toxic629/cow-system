import { Button, DatePicker, Drawer, Form, Input, Select, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useEffect, useState } from 'react';
import { Alarm, ackAlarm, falsePositiveAlarm, getAlarms, resolveAlarm } from '../api/alarms';

export function AlarmsPage() {
  const [data, setData] = useState<Alarm[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [selected, setSelected] = useState<Alarm | null>(null);
  const [note, setNote] = useState('');
  const [filter] = Form.useForm();

  const load = async (page = 1, size = 10) => {
    setLoading(true);
    try {
      const values = filter.getFieldsValue();
      const [from, to] = values.timeRange || [];
      const params = {
        page,
        size,
        status: values.status,
        severity: values.severity,
        metric: values.metric,
        cowId: values.cowId,
        barnId: values.barnId,
        from: from ? dayjs(from).format('YYYY-MM-DD HH:mm:ss') : undefined,
        to: to ? dayjs(to).format('YYYY-MM-DD HH:mm:ss') : undefined
      };
      const res = await getAlarms(params);
      setData(res.records);
      setTotal(res.total);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const columns: ColumnsType<Alarm> = [
    { title: '告警ID', dataIndex: 'alarmId', render: (_, r) => r.alarmId || r.id },
    { title: '牛只编号', dataIndex: 'cowId' },
    { title: '牛舍', dataIndex: 'barnId' },
    { title: '指标', dataIndex: 'metricName' },
    { title: '等级', dataIndex: 'severity', render: (v) => <Tag color={v === 'P1' ? 'red' : v === 'P2' ? 'orange' : 'blue'}>{v || '-'}</Tag> },
    { title: '状态', dataIndex: 'status' },
    { title: '开始时间', dataIndex: 'startTime' },
    {
      title: '操作',
      render: (_, row) => (
        <Button
          type="link"
          size="small"
          onClick={() => {
            setSelected(row);
            setOpen(true);
          }}
        >
          查看详情
        </Button>
      )
    }
  ];

  const selectedId = Number(selected?.alarmId || selected?.id || 0);

  return (
    <>
      <div className="page-header">
        <Typography.Title level={4} style={{ margin: 0 }}>
          告警中心
        </Typography.Title>
      </div>

      <Form form={filter} layout="inline" onFinish={() => load()} className="panel-card search-bar">
        <Form.Item name="status" label="状态">
          <Select
            allowClear
            style={{ width: 130 }}
            options={[
              { value: 'OPEN', label: '未处理' },
              { value: 'ACKED', label: '已确认' },
              { value: 'RESOLVED', label: '已解决' },
              { value: 'FALSE_POSITIVE', label: '误报' }
            ]}
          />
        </Form.Item>
        <Form.Item name="severity" label="等级">
          <Select allowClear style={{ width: 120 }} options={[{ value: 'P1' }, { value: 'P2' }, { value: 'P3' }]} />
        </Form.Item>
        <Form.Item name="metric" label="指标">
          <Input placeholder="如 resp_rate" />
        </Form.Item>
        <Form.Item name="cowId" label="牛只">
          <Input placeholder="牛只编号" />
        </Form.Item>
        <Form.Item name="barnId" label="牛舍">
          <Input placeholder="牛舍编号" />
        </Form.Item>
        <Form.Item name="timeRange" label="时间范围">
          <DatePicker.RangePicker showTime />
        </Form.Item>
        <Button htmlType="submit" type="primary">
          查询
        </Button>
      </Form>

      <Table
        className="panel-card"
        style={{ marginTop: 12 }}
        rowKey={(r) => String(r.alarmId || r.id)}
        columns={columns}
        dataSource={data}
        loading={loading}
        pagination={{ total, showTotal: (t) => `共 ${t} 条`, onChange: (p, s) => load(p, s) }}
      />

      <Drawer open={open} width={560} title="告警详情" onClose={() => setOpen(false)}>
        <Space direction="vertical" style={{ width: '100%' }} size={14}>
          <div><b>告警ID：</b>{selected?.alarmId || selected?.id}</div>
          <div><b>牛只编号：</b>{selected?.cowId || '-'}</div>
          <div><b>指标：</b>{selected?.metricName || '-'}</div>
          {selected?.thresholdJson ? <div><b>阈值：</b>{selected.thresholdJson}</div> : null}
          {selected?.deviationScore !== undefined ? <div><b>偏离分数：</b>{selected.deviationScore}</div> : null}
          {selected?.evidenceJson ? <div><b>证据：</b>{selected.evidenceJson}</div> : null}
          <Input.TextArea rows={3} value={note} onChange={(e) => setNote(e.target.value)} placeholder="请输入处理备注" />
          <Space>
            <Button
              type="default"
              onClick={async () => {
                await ackAlarm(selectedId, note);
                message.success('已确认');
                load();
              }}
            >
              确认
            </Button>
            <Button
              type="primary"
              onClick={async () => {
                await resolveAlarm(selectedId, note);
                message.success('已解决');
                load();
              }}
            >
              解决
            </Button>
            <Button
              danger
              onClick={async () => {
                await falsePositiveAlarm(selectedId, note);
                message.success('已标记为误报');
                load();
              }}
            >
              标记误报
            </Button>
          </Space>
        </Space>
      </Drawer>
    </>
  );
}
