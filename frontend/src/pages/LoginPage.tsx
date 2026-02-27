import { useState } from 'react';
import { Button, Card, Form, Input, message, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { login } from '../api/auth';
import { useAuthStore } from '../store/auth';

export function LoginPage() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);

  return (
    <div className="login-page">
      <Card className="login-card" bordered={false}>
        <Typography.Title level={3} style={{ marginTop: 0 }}>
          登录系统
        </Typography.Title>
        <Typography.Paragraph type="secondary">请输入管理员账号密码进入平台。</Typography.Paragraph>
        <Form
          layout="vertical"
          initialValues={{ username: 'admin', password: 'admin' }}
          onFinish={async (values) => {
            try {
              setLoading(true);
              const token = await login(values.username, values.password);
              setAuth(token, values.username);
              navigate('/dashboard', { replace: true });
            } catch (e) {
              message.error((e as Error).message || '登录失败，请检查后端是否启动');
            } finally {
              setLoading(false);
            }
          }}
        >
          <Form.Item label="用户名" name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input size="large" placeholder="请输入用户名" />
          </Form.Item>
          <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password size="large" placeholder="请输入密码" />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} size="large" block>
            立即登录
          </Button>
        </Form>
      </Card>
    </div>
  );
}
