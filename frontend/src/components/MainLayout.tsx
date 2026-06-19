import { Layout, Menu, Button, Avatar, Dropdown, Space } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  SafetyOutlined,
  StockOutlined,
  UserOutlined,
  LogoutOutlined,
  DashboardOutlined,
  ScanOutlined,
  InboxOutlined,
  CheckCircleOutlined,
  WarningOutlined,
  PlusOutlined
} from '@ant-design/icons';
import { useAuthStore } from '../store/authStore';

const { Header, Sider, Content } = Layout;

const roleMenus = {
  BLASTER: [
    { key: '/blaster', icon: <DashboardOutlined />, label: '工作台' },
    { key: '/blaster/create-shift', icon: <PlusOutlined />, label: '创建当班作业' },
    { key: '/blaster/create-application', icon: <SafetyOutlined />, label: '领用申请' },
  ],
  STOREKEEPER: [
    { key: '/storekeeper', icon: <DashboardOutlined />, label: '工作台' },
    { key: '/storekeeper/outbound', icon: <ScanOutlined />, label: '扫码出库' },
    { key: '/storekeeper/inbound', icon: <InboxOutlined />, label: '扫码回库' },
    { key: '/safety/review', icon: <CheckCircleOutlined />, label: '复核申请' },
  ],
  SAFETY_OFFICER: [
    { key: '/safety', icon: <DashboardOutlined />, label: '工作台' },
    { key: '/safety/review', icon: <CheckCircleOutlined />, label: '复核申请' },
    { key: '/safety/verification', icon: <WarningOutlined />, label: '器材核对' },
    { key: '/safety/anomalies', icon: <WarningOutlined />, label: '异常管理' },
  ],
};

export default function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout, isAuthenticated } = useAuthStore();

  if (!isAuthenticated || !user) {
    return <Outlet />;
  }

  const menuItems = roleMenus[user.role] || [];

  const userMenu = {
    items: [
      {
        key: 'logout',
        icon: <LogoutOutlined />,
        label: '退出登录',
        onClick: () => {
          logout();
          navigate('/login');
        },
      },
    ],
  };

  const roleNameMap: Record<string, string> = {
    BLASTER: '爆破员',
    STOREKEEPER: '库管',
    SAFETY_OFFICER: '安全负责人',
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', background: '#001529', padding: '0 24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <SafetyOutlined style={{ fontSize: '28px', color: '#fff' }} />
          <span style={{ color: '#fff', fontSize: '20px', fontWeight: 'bold' }}>智能矿山爆破器材领退管理系统</span>
        </div>
        <Space>
          <span style={{ color: '#fff' }}>{roleNameMap[user.role]}: {user.name}</span>
          <Dropdown menu={userMenu} placement="bottomRight">
            <Button type="text" style={{ color: '#fff' }}>
              <Avatar size="small" icon={<UserOutlined />} />
              <span style={{ marginLeft: '8px' }}>{user.name}</span>
            </Button>
          </Dropdown>
        </Space>
      </Header>
      <Layout>
        <Sider width={220} style={{ background: '#fff' }}>
          <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems.map(item => ({
              ...item,
              onClick: () => navigate(item.key),
            }))}
            style={{ height: '100%', borderRight: 0 }}
          />
        </Sider>
        <Layout style={{ padding: '24px' }}>
          <Content
            style={{
              padding: '24px',
              margin: 0,
              minHeight: 'calc(100vh - 112px)',
              background: '#fff',
              borderRadius: '8px',
            }}
          >
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
}
