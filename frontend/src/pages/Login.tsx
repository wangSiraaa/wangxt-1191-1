import { Form, Input, Button, Card, Typography, message } from 'antd';
import { UserOutlined, LockOutlined, SafetyOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../services/api';
import { useAuthStore } from '../store/authStore';
import type { LoginRequest } from '../types';

const { Title, Text } = Typography;

export default function Login() {
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);
  const [form] = Form.useForm<LoginRequest>();

  const handleLogin = async (values: LoginRequest) => {
    try {
      const response = await authApi.login(values);
      if (response.success) {
        login(response.data);
        message.success('登录成功');
        switch (response.data.role) {
          case 'BLASTER':
            navigate('/blaster');
            break;
          case 'STOREKEEPER':
            navigate('/storekeeper');
            break;
          case 'SAFETY_OFFICER':
            navigate('/safety');
            break;
        }
      } else {
        message.error(response.message);
      }
    } catch (error: any) {
      message.error(error.response?.data?.message || '登录失败');
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }}>
      <Card style={{ width: 420, boxShadow: '0 4px 20px rgba(0,0,0,0.15)' }}>
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <SafetyOutlined style={{ fontSize: '48px', color: '#1890ff' }} />
          <Title level={3} style={{ marginTop: '16px', marginBottom: '8px' }}>
            智能矿山爆破器材领退管理系统
          </Title>
          <Text type="secondary">请登录以继续</Text>
        </div>

        <Form form={form} onFinish={handleLogin} size="large">
          <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block style={{ height: '44px', fontSize: '16px' }}>
              登 录
            </Button>
          </Form.Item>
        </Form>

        <div style={{ marginTop: '24px', padding: '16px', background: '#f5f5f5', borderRadius: '8px' }}>
          <Text strong>演示账号：</Text>
          <div style={{ marginTop: '8px', fontSize: '13px' }}>
            <div>爆破员: blaster / password</div>
            <div>库管: storekeeper / password</div>
            <div>安全负责人: safety / password</div>
          </div>
        </div>
      </Card>
    </div>
  );
}
