import { useState, useEffect } from 'react';
import { Form, Input, Select, Button, Card, Typography, message, Space } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { shiftApi } from '../../services/api';
import type { WorkPlan } from '../../types';

const { Title } = Typography;
const { TextArea } = Input;
const { Option } = Select;

interface FormData {
  workFace: string;
  workPlanId?: number;
  remarks?: string;
}

export default function CreateShift() {
  const navigate = useNavigate();
  const [form] = Form.useForm<FormData>();
  const [loading, setLoading] = useState(false);
  const [workPlans, setWorkPlans] = useState<WorkPlan[]>([]);

  useEffect(() => {
    loadWorkPlans();
  }, []);

  const loadWorkPlans = async () => {
    try {
      const res = await fetch('/api/shifts').then(r => r.json());
    } catch (error) {
      console.error('Failed to load work plans');
    }
  };

  const handleSubmit = async (values: FormData) => {
    setLoading(true);
    try {
      const res = await shiftApi.create(values);
      if (res.success) {
        message.success('当班作业创建成功');
        navigate('/blaster');
      } else {
        message.error(res.message);
      }
    } catch (error: any) {
      message.error(error.response?.data?.message || '创建失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '24px' }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/blaster')} style={{ marginRight: '16px' }}>
          返回
        </Button>
        <Title level={3} style={{ margin: 0 }}>创建当班作业</Title>
      </div>

      <Card style={{ maxWidth: 600 }}>
        <Form form={form} layout="vertical" onFinish={handleSubmit} size="large">
          <Form.Item
            name="workFace"
            label="作业面"
            rules={[{ required: true, message: '请输入作业面' }]}
          >
            <Select placeholder="请选择或输入作业面">
              <Option value="1101工作面">1101工作面</Option>
              <Option value="1202工作面">1202工作面</Option>
              <Option value="1303工作面">1303工作面</Option>
              <Option value="1404工作面">1404工作面</Option>
            </Select>
          </Form.Item>

          <Form.Item name="remarks" label="备注">
            <TextArea rows={3} placeholder="请输入备注信息（可选）" />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading}>
                创建当班
              </Button>
              <Button onClick={() => navigate('/blaster')}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
