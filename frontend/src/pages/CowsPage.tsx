import { Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Cow, createCow, deleteCow, getCows, updateCow } from '../api/cows';

const statusColorMap: Record<string, string> = {
  NORMAL: 'green',
  OBSERVE: 'orange',
  TREATMENT: 'red'
};

const statusLabelMap: Record<string, string> = {
  NORMAL: '正常',
  OBSERVE: '观察中',
  TREATMENT: '治疗中'
};

export function CowsPage() {
  const [data, setData] = useState<Cow[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(10);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Cow | null>(null);
  const [form] = Form.useForm<Cow>();
  const navigate = useNavigate();

  const fetchData = async (p = page, s = size) => {
    setLoading(true);
    try {
      const res = await getCows(p, s);
      setData(res.records);
      setTotal(res.total);
      setPage(p);
      setSize(s);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData(1, 10);
  }, []);

  const columns: ColumnsType<Cow> = useMemo(
    () => [
      { title: '牛只编号', dataIndex: 'cowId', width: 120 },
      { title: '名称', dataIndex: 'name', width: 120 },
      { title: '耳标号', dataIndex: 'earTag', width: 120 },
      { title: '牛舍', dataIndex: 'barnId', width: 100 },
      { title: '日龄', dataIndex: 'ageDays', width: 100 },
      {
        title: '状态',
        dataIndex: 'status',
        width: 100,
        render: (value) => <Tag color={statusColorMap[value] || 'blue'}>{statusLabelMap[value] || value || '-'}</Tag>
      },
      {
        title: '操作',
        fixed: 'right',
        width: 220,
        render: (_, row) => (
          <Space size={4}>
            <Button type="link" size="small" onClick={() => navigate(`/cows/${row.cowId}`, { state: { barnId: row.barnId } })}>
              查看详情
            </Button>
            <Button
              type="link"
              size="small"
              onClick={() => {
                setEditing(row);
                form.setFieldsValue(row);
                setOpen(true);
              }}
            >
              编辑
            </Button>
            <Popconfirm
              title="确认删除这头牛吗？"
              onConfirm={async () => {
                await deleteCow(row.cowId);
                message.success('删除成功');
                fetchData();
              }}
            >
              <Button type="link" danger size="small">
                删除
              </Button>
            </Popconfirm>
          </Space>
        )
      }
    ],
    [form, navigate, page, size]
  );

  return (
    <>
      <div className="page-header">
        <Typography.Title level={4} style={{ margin: 0 }}>
          牛只管理
        </Typography.Title>
        <Button
          type="primary"
          onClick={() => {
            setEditing(null);
            form.resetFields();
            setOpen(true);
          }}
        >
          新增牛只
        </Button>
      </div>

      <Table
        className="panel-card"
        rowKey="cowId"
        loading={loading}
        columns={columns}
        dataSource={data}
        scroll={{ x: 980 }}
        pagination={{
          current: page,
          pageSize: size,
          total,
          showSizeChanger: true,
          showTotal: (t) => `共 ${t} 条`,
          onChange: (p, s) => fetchData(p, s)
        }}
      />

      <Modal
        title={editing ? '编辑牛只' : '新增牛只'}
        open={open}
        onCancel={() => setOpen(false)}
        onOk={async () => {
          const values = await form.validateFields();
          if (editing) {
            await updateCow(values);
            message.success('更新成功');
          } else {
            await createCow(values);
            message.success('创建成功');
          }
          setOpen(false);
          fetchData();
        }}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="cowId" label="牛只编号" rules={[{ required: true, message: '请输入牛只编号' }]}>
            <Input disabled={!!editing} />
          </Form.Item>
          <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="earTag" label="耳标号" rules={[{ required: true, message: '请输入耳标号' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="barnId" label="牛舍编号" rules={[{ required: true, message: '请输入牛舍编号' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="ageDays" label="日龄" rules={[{ required: true, message: '请输入日龄' }]}>
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
            <Select
              options={[
                { value: 'NORMAL', label: '正常' },
                { value: 'OBSERVE', label: '观察中' },
                { value: 'TREATMENT', label: '治疗中' }
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
