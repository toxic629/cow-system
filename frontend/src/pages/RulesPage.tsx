import { Button, Form, Input, InputNumber, Modal, Space, Switch, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useState } from 'react';
import { Rule, createRule, disableRule, enableRule, getRules, updateRule } from '../api/rules';

export function RulesPage() {
  const [data, setData] = useState<Rule[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(10);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Rule | null>(null);
  const [form] = Form.useForm<Rule>();

  const load = async (p = page, s = size) => {
    setLoading(true);
    try {
      const res = await getRules(p, s);
      const stableRecords = [...res.records].sort((a, b) => Number(a.ruleId || 0) - Number(b.ruleId || 0));
      setData(stableRecords);
      setTotal(res.total);
      setPage(p);
      setSize(s);
    } catch (e) {
      setData([]);
      setTotal(0);
      message.error((e as Error).message || '规则列表加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const columns: ColumnsType<Rule> = [
    { title: '规则ID', dataIndex: 'ruleId', width: 100 },
    { title: '指标名称', dataIndex: 'metricName' },
    { title: '最小阈值', render: (_, r) => r.minValue ?? r.lower ?? '-' },
    { title: '最大阈值', render: (_, r) => r.maxValue ?? r.upper ?? '-' },
    { title: '持续时长(秒)', render: (_, r) => r.durationSeconds ?? (r.durationMinutes ? r.durationMinutes * 60 : '-') },
    {
      title: '启用状态',
      render: (_, r) => (
        <Switch
          checked={!!r.enabled}
          checkedChildren="启用"
          unCheckedChildren="停用"
          onChange={async (v) => {
            if (!r.ruleId) {
              message.error('规则ID缺失，无法切换状态');
              return;
            }
            await (v ? enableRule(r.ruleId) : disableRule(r.ruleId));
            await load(page, size);
          }}
        />
      )
    },
    {
      title: '操作',
      render: (_, row) => (
        <Button
          type="link"
          size="small"
          onClick={() => {
            setEditing(row);
            form.setFieldsValue({
              ...row,
              minValue: row.minValue ?? row.lower,
              maxValue: row.maxValue ?? row.upper,
              durationSeconds: (row.durationSeconds ?? row.durationMinutes ?? 0) * 60,
              enabled: row.enabled
            });
            setOpen(true);
          }}
        >
          编辑
        </Button>
      )
    }
  ];

  return (
    <>
      <div className="page-header">
        <Typography.Title level={4} style={{ margin: 0 }}>
          规则配置
        </Typography.Title>
        <Button
          type="primary"
          onClick={() => {
            setEditing(null);
            form.resetFields();
            form.setFieldsValue({ enabled: true });
            setOpen(true);
          }}
        >
          新增规则
        </Button>
      </div>

      <Table
        className="panel-card"
        rowKey="ruleId"
        loading={loading}
        columns={columns}
        dataSource={data}
        pagination={{
          current: page,
          pageSize: size,
          total,
          showTotal: (t) => `共 ${t} 条`,
          onChange: (p, s) => load(p, s)
        }}
      />

      <Modal
        title={editing ? '编辑规则' : '新增规则'}
        open={open}
        onCancel={() => setOpen(false)}
        onOk={async () => {
          const values = await form.validateFields();
          if (editing) {
            await updateRule({ ...values, ruleId: editing.ruleId });
            message.success('更新成功');
          } else {
            await createRule(values);
            message.success('创建成功');
          }
          setOpen(false);
          load(page, size);
        }}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="metricName" label="指标名称" rules={[{ required: true, message: '请输入指标名称' }]}>
            <Input placeholder="例如 resp_rate" />
          </Form.Item>
          <Form.Item name="minValue" label="最小阈值">
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="maxValue" label="最大阈值">
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="durationSeconds" label="持续时长（秒，可选）">
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>
          <Form.Item name="enabled" label="启用" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="停用" />
          </Form.Item>
          <Tag color="processing">若后端不支持持续时长，系统会自动降级提交</Tag>
        </Form>
      </Modal>
    </>
  );
}
