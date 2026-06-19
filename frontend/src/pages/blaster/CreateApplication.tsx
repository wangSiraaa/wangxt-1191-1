import { useState, useEffect } from 'react';
import { Form, InputNumber, Select, Button, Card, Typography, message, Space, Alert } from 'antd';
import { ArrowLeftOutlined, WarningOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { shiftApi, applicationApi } from '../../services/api';
import type { Shift } from '../../types';

const { Title, Text } = Typography;
const { Option } = Select;

interface FormData {
  shiftId: number;
  detonatorQuantity: number;
  explosiveQuantity: number;
}

export default function CreateApplication() {
  const navigate = useNavigate();
  const [form] = Form.useForm<FormData>();
  const [loading, setLoading] = useState(false);
  const [shifts, setShifts] = useState<Shift[]>([]);
  const [selectedShift, setSelectedShift] = useState<Shift | null>(null);

  useEffect(() => {
    loadActiveShifts();
  }, []);

  const loadActiveShifts = async () => {
    try {
      const res = await shiftApi.getMyActive();
      if (res.success) {
        setShifts(res.data);
      }
    } catch (error) {
      message.error('加载当班作业失败');
    }
  };

  const handleShiftChange = (shiftId: number) => {
    const shift = shifts.find(s => s.id === shiftId);
    setSelectedShift(shift || null);
  };

  const handleSubmit = async (values: FormData) => {
    setLoading(true);
    try {
      const res = await applicationApi.create(values);
      if (res.success) {
        if (res.data.status === 'NEED_REVIEW') {
          message.warning('领用数量与设计孔数不匹配，已提交安全负责人复核');
        } else {
          message.success('领用申请已提交');
        }
        navigate('/blaster');
      } else {
        message.error(res.message);
      }
    } catch (error: any) {
      message.error(error.response?.data?.message || '提交失败');
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
        <Title level={3} style={{ margin: 0 }}>领用申请</Title>
      </div>

      {shifts.length === 0 && (
        <Alert
          type="warning"
          showIcon
          icon={<WarningOutlined />}
          message="请先创建当班作业"
          description="您需要先创建当班作业才能提交领用申请"
          style={{ marginBottom: '24px' }}
        />
      )}

      <Card style={{ maxWidth: 600 }}>
        <Form form={form} layout="vertical" onFinish={handleSubmit} size="large">
          <Form.Item
            name="shiftId"
            label="当班作业"
            rules={[{ required: true, message: '请选择当班作业' }]}
          >
            <Select
              placeholder="请选择当班作业"
              onChange={handleShiftChange}
              disabled={shifts.length === 0}
            >
              {shifts.map(shift => (
                <Option key={shift.id} value={shift.id}>
                  {shift.shiftNo} - {shift.workFace}
                </Option>
              ))}
            </Select>
          </Form.Item>

          {selectedShift?.workPlan && (
            <Alert
              type="info"
              message="作业计划信息"
              description={
                <Space direction="vertical" size="small">
                  <div>设计孔数: {selectedShift.workPlan.designedHoles} 个</div>
                  <div>设计雷管: {selectedShift.workPlan.estimatedDetonators} 发</div>
                  <div>设计炸药: {selectedShift.workPlan.estimatedExplosives} kg</div>
                  <Text type="secondary">注：领用数量超出设计10%将触发复核流程</Text>
                </Space>
              }
              style={{ marginBottom: '24px' }}
            />
          )}

          <Form.Item
            name="detonatorQuantity"
            label="雷管数量（发）"
            rules={[{ required: true, message: '请输入雷管数量' }]}
          >
            <InputNumber min={0} style={{ width: '100%' }} placeholder="请输入雷管数量" />
          </Form.Item>

          <Form.Item
            name="explosiveQuantity"
            label="炸药数量（kg）"
            rules={[{ required: true, message: '请输入炸药数量' }]}
          >
            <InputNumber min={0} step={0.5} style={{ width: '100%' }} placeholder="请输入炸药数量" />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading} disabled={shifts.length === 0}>
                提交申请
              </Button>
              <Button onClick={() => navigate('/blaster')}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
