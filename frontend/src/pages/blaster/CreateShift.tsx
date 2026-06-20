import { useState, useEffect } from 'react';
import { Form, Select, Button, Card, Typography, message, Space, Descriptions, Input } from 'antd';
import { ArrowLeftOutlined, SafetyOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { shiftApi, workPlanApi } from '../../services/api';
import type { WorkPlan } from '../../types';

const { Title, Text } = Typography;
const { Option } = Select;

interface FormData {
  workPlanId: number;
  remarks?: string;
}

export default function CreateShift() {
  const navigate = useNavigate();
  const [form] = Form.useForm<FormData>();
  const [loading, setLoading] = useState(false);
  const [workPlans, setWorkPlans] = useState<WorkPlan[]>([]);
  const [selectedPlan, setSelectedPlan] = useState<WorkPlan | null>(null);

  useEffect(() => {
    loadWorkPlans();
  }, []);

  const loadWorkPlans = async () => {
    try {
      const res = await workPlanApi.list();
      if (res.success) {
        setWorkPlans(res.data);
      }
    } catch (error) {
      console.error('Failed to load work plans');
      message.error('加载作业计划失败');
    }
  };

  const handlePlanChange = (planId: number) => {
    const plan = workPlans.find(p => p.id === planId);
    setSelectedPlan(plan || null);
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
            name="workPlanId"
            label="作业计划"
            rules={[{ required: true, message: '请选择作业计划' }]}
          >
            <Select
              placeholder="请选择作业计划"
              onChange={handlePlanChange}
              showSearch
              optionFilterProp="children"
            >
              {workPlans.map(plan => (
                <Option key={plan.id} value={plan.id}>
                  {plan.planNo} - {plan.workFace}（{plan.designedHoles}孔）
                </Option>
              ))}
            </Select>
          </Form.Item>

          {selectedPlan && (
            <Card
              size="small"
              title="作业计划详情"
              style={{ marginBottom: '24px', backgroundColor: '#f5f5f5' }}
            >
              <Descriptions column={2} size="small">
                <Descriptions.Item label="计划编号">{selectedPlan.planNo}</Descriptions.Item>
                <Descriptions.Item label="作业面">{selectedPlan.workFace}</Descriptions.Item>
                <Descriptions.Item label="设计孔数">
                  <Text type="danger">{selectedPlan.designedHoles} 个</Text>
                </Descriptions.Item>
                <Descriptions.Item label="设计雷管">
                  <Text type="success">{selectedPlan.estimatedDetonators} 发</Text>
                </Descriptions.Item>
                <Descriptions.Item label="设计炸药">
                  <Text type="success">{selectedPlan.estimatedExplosives} kg</Text>
                </Descriptions.Item>
              </Descriptions>
              <div style={{ marginTop: '8px' }}>
                <Text type="secondary">注：领用数量超出设计量±10%将触发安全负责人复核</Text>
              </div>
            </Card>
          )}

          <Form.Item name="remarks" label="备注">
            <Input.TextArea rows={3} placeholder="请输入备注信息（可选）" style={{ width: '100%' }} />
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
