import { BarChartOutlined, BellOutlined, DashboardOutlined, EnvironmentOutlined, LogoutOutlined, SettingOutlined } from '@ant-design/icons';
import { Avatar, Button, Layout, Menu, Space, Typography } from 'antd';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/auth';

const { Header, Sider, Content } = Layout;

export function AppLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const clearAuth = useAuthStore((s) => s.clearAuth);

  return (
    <Layout className="app-shell">
      <Sider breakpoint="lg" collapsedWidth="0" className="app-sider" width={230}>
        <div className="logo">
          <span className="logo-badge">牛</span>
          <span>牛场健康平台</span>
        </div>
        <Menu
          theme="dark"
          selectedKeys={[location.pathname]}
          items={[
            { key: '/dashboard', icon: <DashboardOutlined />, label: <Link to="/dashboard">数据概览</Link> },
            { key: '/cows', icon: <BarChartOutlined />, label: <Link to="/cows">牛只管理</Link> },
            { key: '/barn-env', icon: <EnvironmentOutlined />, label: <Link to="/barn-env">环境监测</Link> },
            { key: '/alarms', icon: <BellOutlined />, label: <Link to="/alarms">告警中心</Link> },
            { key: '/rules', icon: <SettingOutlined />, label: <Link to="/rules">规则配置</Link> }
          ]}
        />
      </Sider>
      <Layout>
        <Header className="app-header">
          <div />
          <Space>
            <Avatar style={{ backgroundColor: '#1677ff' }}>{(user || 'A').slice(0, 1).toUpperCase()}</Avatar>
            <Typography.Text>当前用户：{user || 'admin'}</Typography.Text>
            <Button
              icon={<LogoutOutlined />}
              onClick={() => {
                clearAuth();
                navigate('/login');
              }}
            >
              退出登录
            </Button>
          </Space>
        </Header>
        <Content className="app-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
