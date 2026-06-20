import { useState, useEffect, useMemo } from 'react';
import { Form, InputNumber, Select, Button, Card, Typography, message, Space, Alert, Descriptions, Statistic, Row, Col, Progress, Tag } from 'antd';
import { ArrowLeftOutlined, WarningOutlined, CheckCircleOutlined, ExclamationCircleOutlined, SafetyOutlined } from '@ant-design/icons';
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

const DETONATOR_TOLERANCE = 10;
const EXPLOSIVE_TOLERANCE = 10;

export default function CreateApplication() {
  const navigate = useNavigate();
  const [form] = Form.useForm<FormData>();
  const [loading, setLoading] = useState(false);
  const [shifts, setShifts] = useState<Shift[]>([]);
  const [selectedShift, setSelectedShift] = useState<Shift | null>(null);
  const [detonatorQty, setDetonatorQty] = useState<number>(0);
  const [explosiveQty, setExplosiveQty] = useState<number>(0);

  useEffect(() => {
    loadActiveShifts();
  }, []);

  useEffect(() => {
    form.setFieldValue('detonatorQuantity', detonatorQty);
    form.setFieldValue('explosiveQuantity', explosiveQty);
  }, [detonatorQty, explosiveQty, form]);

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
    setDetonatorQty(0);
    setExplosiveQty(0);
  };

  const mismatchAnalysis = useMemo(() => {
    if (!selectedShift?.workPlan) {
      return null;
    }
    const wp = selectedShift.workPlan;
    const designedDetonators = wp.estimatedDetonators;
    const designedExplosives = wp.estimatedExplosives;
    const designedHoles = wp.designedHoles;

    const detonatorDiff = designedDetonators > 0
      ? (detonatorQty - designedDetonators) * 100 / designedDetonators
      : 0;
    const explosiveDiff = designedExplosives > 0
      ? (explosiveQty - designedExplosives) * 100 / designedExplosives
      : 0;

    const holeCountMatch = detonatorQty === designedHoles;
    const detonatorInTolerance = Math.abs(detonatorDiff) <= DETONATOR_TOLERANCE;
    const explosiveInTolerance = Math.abs(explosiveDiff) <= EXPLOSIVE_TOLERANCE;

    const needReview = detonatorQty > 0 && (
      !holeCountMatch || !detonatorInTolerance || !explosiveInTolerance
    );

    return {
      designedHoles,
      designedDetonators,
      designedExplosives,
      detonatorDiff,
      explosiveDiff,
      holeCountMatch,
      detonatorInTolerance,
      explosiveInTolerance,
      needReview
    };
  }, [selectedShift, detonatorQty, explosiveQty]);

  const handleSubmit = async (values: FormData) => {
    setLoading(true);
    try {
      const res = await applicationApi.create(values);
      if (res.success) {
        if (res.data.status === 'NEED_REVIEW') {
          message.warning('领用数量与设计量/设计孔数不匹配，已提交安全负责人复核');
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

  const renderProgress = (actual: number, designed: number, tolerance: number, unit: string) => {
    if (designed === 0) return null;
    const percent = Math.min(100, (actual / designed) * 100);
    const diff = actual - designed;
    const diffPercent = designed > 0 ? (diff / designed) * 100 : 0;
    const isOver = Math.abs(diffPercent) > tolerance;
    return (
      <div>
        <Progress
          percent={Math.round(percent)}
          status={actual === 0 ? 'normal' : (isOver ? 'exception' : 'success')}
          format={() => `${actual}/${designed} ${unit}`}
        />
        {actual > 0 && (
          <Text type={isOver ? 'danger' : 'success'}>
            {diff >= 0 ? '+' : ''}{diff} {unit} ({diffPercent >= 0 ? '+' : ''}{diffPercent.toFixed(1)}%)
            {isOver && ' 超出容差'}
          </Text>
        )}
      </div>
    );
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
          description="您需要先创建当班作业并绑定作业计划，才能提交领用申请"
          style={{ marginBottom: '24px' }}
        />
      )}

      <Card style={{ maxWidth: 800 }}>
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
                  {shift.workPlan ? ` [${shift.workPlan.planNo}]` : ''}
                </Option>
              ))}
            </Select>
          </Form.Item>

          {selectedShift?.workPlan && (
            <Card
              size="small"
              title={
                <Space>
                  <SafetyOutlined style={{ color: '#1890ff' }} />
                  <span>作业计划设计信息</span>
                  <Tag color="blue">{selectedShift.workPlan.planNo}</Tag>
                </Space>
              }
              style={{ marginBottom: '24px', borderColor: '#1890ff', backgroundColor: '#f0f8ff' }}
            >
              <Descriptions column={3} size="small">
                <Descriptions.Item label="作业面">
                  <Text strong>{selectedShift.workPlan.workFace}</Text>
                </Descriptions.Item>
                <Descriptions.Item label="设计孔数">
                  <Text type="danger" strong>{selectedShift.workPlan.designedHoles} 个</Text>
                </Descriptions.Item>
                <Descriptions.Item label="设计单孔雷管">
                  <Text type="secondary">
                    {(selectedShift.workPlan.estimatedDetonators / selectedShift.workPlan.designedHoles).toFixed(1)} 发/孔
                  </Text>
                </Descriptions.Item>
                <Descriptions.Item label="设计雷管总量">
                  <Text type="warning" strong>{selectedShift.workPlan.estimatedDetonators} 发</Text>
                </Descriptions.Item>
                <Descriptions.Item label="设计炸药总量">
                  <Text type="warning" strong>{selectedShift.workPlan.estimatedExplosives} kg</Text>
                </Descriptions.Item>
                <Descriptions.Item label="设计单孔炸药">
                  <Text type="secondary">
                    {(selectedShift.workPlan.estimatedExplosives / selectedShift.workPlan.designedHoles).toFixed(2)} kg/孔
                  </Text>
                </Descriptions.Item>
              </Descriptions>

              <Alert
                type="info"
                showIcon
                message="复核规则说明"
                description={
                  <ul style={{ margin: 0, paddingLeft: '20px' }}>
                    <li>雷管数量必须与设计孔数一致（{selectedShift.workPlan.designedHoles}发），否则触发复核</li>
                    <li>雷管数量超出设计量±{DETONATOR_TOLERANCE}%，触发复核</li>
                    <li>炸药数量超出设计量±{EXPLOSIVE_TOLERANCE}%，触发复核</li>
                  </ul>
                }
                style={{ marginTop: '12px' }}
              />
            </Card>
          )}

          {mismatchAnalysis && (
            <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
              <Col span={12}>
                <Card size="small" title="雷管数量核对">
                  <Statistic
                    title="申请数量"
                    value={detonatorQty}
                    suffix="发"
                    valueStyle={{ color: detonatorQty > 0 ? (mismatchAnalysis.detonatorInTolerance && mismatchAnalysis.holeCountMatch ? '#3f8600' : '#cf1322') : 'inherit' }}
                  />
                  <div style={{ marginTop: '12px' }}>
                    {renderProgress(detonatorQty, mismatchAnalysis.designedDetonators, DETONATOR_TOLERANCE, '发')}
                  </div>
                  <div style={{ marginTop: '8px' }}>
                    <Space>
                      {mismatchAnalysis.holeCountMatch ? (
                        <Tag icon={<CheckCircleOutlined />} color="success">与设计孔数匹配</Tag>
                      ) : (
                        detonatorQty > 0 && (
                          <Tag icon={<ExclamationCircleOutlined />} color="warning">
                            孔数不匹配(设计{mismatchAnalysis.designedHoles}孔)
                          </Tag>
                        )
                      )}
                      {mismatchAnalysis.detonatorInTolerance ? (
                        <Tag icon={<CheckCircleOutlined />} color="success">偏差在容差内</Tag>
                      ) : (
                        detonatorQty > 0 && (
                          <Tag icon={<ExclamationCircleOutlined />} color="error">
                            偏差超出±{DETONATOR_TOLERANCE}%
                          </Tag>
                        )
                      )}
                    </Space>
                  </div>
                </Card>
              </Col>
              <Col span={12}>
                <Card size="small" title="炸药数量核对">
                  <Statistic
                    title="申请数量"
                    value={explosiveQty}
                    suffix="kg"
                    valueStyle={{ color: explosiveQty > 0 ? (mismatchAnalysis.explosiveInTolerance ? '#3f8600' : '#cf1322') : 'inherit' }}
                  />
                  <div style={{ marginTop: '12px' }}>
                    {renderProgress(explosiveQty, mismatchAnalysis.designedExplosives, EXPLOSIVE_TOLERANCE, 'kg')}
                  </div>
                  <div style={{ marginTop: '8px' }}>
                    <Space>
                      {mismatchAnalysis.explosiveInTolerance ? (
                        <Tag icon={<CheckCircleOutlined />} color="success">偏差在容差内</Tag>
                      ) : (
                        explosiveQty > 0 && (
                          <Tag icon={<ExclamationCircleOutlined />} color="error">
                            偏差超出±{EXPLOSIVE_TOLERANCE}%
                          </Tag>
                        )
                      )}
                    </Space>
                  </div>
                </Card>
              </Col>
            </Row>
          )}

          {mismatchAnalysis?.needReview && (
            <Alert
              type="warning"
              showIcon
              icon={<WarningOutlined />}
              message="申请将进入安全负责人复核流程"
              description="当前领用数量与设计孔数或设计量不匹配，系统将自动提交流程由安全负责人复核确认。"
              style={{ marginBottom: '24px' }}
            />
          )}

          <Form.Item
            name="detonatorQuantity"
            label="雷管数量（发）"
            rules={[{ required: true, message: '请输入雷管数量' }]}
          >
            <InputNumber
              min={0}
              style={{ width: '100%' }}
              placeholder="请输入雷管数量"
              value={detonatorQty}
              onChange={(value) => setDetonatorQty(value || 0)}
            />
          </Form.Item>

          <Form.Item
            name="explosiveQuantity"
            label="炸药数量（kg）"
            rules={[{ required: true, message: '请输入炸药数量' }]}
          >
            <InputNumber
              min={0}
              step={0.5}
              style={{ width: '100%' }}
              placeholder="请输入炸药数量"
              value={explosiveQty}
              onChange={(value) => setExplosiveQty(value || 0)}
            />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading} disabled={shifts.length === 0}>
                {mismatchAnalysis?.needReview ? '提交复核' : '提交申请'}
              </Button>
              <Button onClick={() => navigate('/blaster')}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
