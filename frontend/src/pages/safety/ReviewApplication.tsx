import { useState, useEffect } from 'react';
import { Form, Select, Button, Card, Typography, message, Space, Table, Tag, Divider, Modal, Input, Empty, Descriptions, Alert, Row, Col, Progress, Statistic } from 'antd';
import { ArrowLeftOutlined, CheckOutlined, CloseOutlined, WarningOutlined, SafetyOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { applicationApi } from '../../services/api';
import type { PickupApplication } from '../../types';
import dayjs from 'dayjs';

const { Title, Text, Paragraph } = Typography;
const { Option } = Select;
const { TextArea } = Input;

const DETONATOR_TOLERANCE = 10;
const EXPLOSIVE_TOLERANCE = 10;

export default function ReviewApplication() {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [applications, setApplications] = useState<PickupApplication[]>([]);
  const [selectedApp, setSelectedApp] = useState<PickupApplication | null>(null);
  const [reviewModalVisible, setReviewModalVisible] = useState(false);
  const [reviewAction, setReviewAction] = useState<'APPROVED' | 'REJECTED'>('APPROVED');

  useEffect(() => {
    loadNeedReviewApplications();
  }, []);

  const loadNeedReviewApplications = async () => {
    try {
      const res = await applicationApi.needReview();
      if (res.success) setApplications(res.data.filter(a => a.status === 'NEED_REVIEW'));
    } catch (error) {
      message.error('加载待复核申请失败');
    }
  };

  const handleAppChange = (appId: number) => {
    const app = applications.find(a => a.id === appId);
    setSelectedApp(app || null);
  };

  const handleReview = (action: 'APPROVED' | 'REJECTED') => {
    setReviewAction(action);
    setReviewModalVisible(true);
    form.resetFields();
  };

  const handleSubmitReview = async () => {
    if (!selectedApp) return;

    try {
      const values = await form.validateFields();
      setLoading(true);
      const res = await applicationApi.review({
        applicationId: selectedApp.id,
        approved: reviewAction === 'APPROVED',
        remark: values.reviewRemark,
      });
      if (res.success) {
        message.success(reviewAction === 'APPROVED' ? '已批准' : '已拒绝');
        setReviewModalVisible(false);
        setSelectedApp(null);
        loadNeedReviewApplications();
      } else {
        message.error(res.message);
      }
    } catch (error: any) {
      message.error(error.response?.data?.message || '操作失败');
    } finally {
      setLoading(false);
    }
  };

  const calculateAnalysis = (app: PickupApplication) => {
    const wp = app.shift?.workPlan;
    if (!wp) return null;
    const designedHoles = wp.designedHoles;
    const designedDetonators = wp.estimatedDetonators;
    const designedExplosives = wp.estimatedExplosives;
    const appDetonators = app.detonatorQuantity;
    const appExplosives = app.explosiveQuantity;

    const detonatorDiff = designedDetonators > 0
      ? (appDetonators - designedDetonators) * 100 / designedDetonators
      : 0;
    const explosiveDiff = designedExplosives > 0
      ? (appExplosives - designedExplosives) * 100 / designedExplosives
      : 0;

    return {
      designedHoles,
      designedDetonators,
      designedExplosives,
      detonatorDiff,
      explosiveDiff,
      holeCountMatch: appDetonators === designedHoles,
      detonatorInTolerance: Math.abs(detonatorDiff) <= DETONATOR_TOLERANCE,
      explosiveInTolerance: Math.abs(explosiveDiff) <= EXPLOSIVE_TOLERANCE,
    };
  };

  const columns = [
    {
      title: '申请单号',
      dataIndex: 'applicationNo',
      render: (text: string) => <Text strong>{text}</Text>,
    },
    { title: '爆破员', dataIndex: ['blaster', 'name'] },
    { title: '当班作业', dataIndex: ['shift', 'shiftNo'] },
    { title: '作业面', dataIndex: ['shift', 'workFace'] },
    {
      title: '设计孔数',
      dataIndex: ['shift', 'workPlan', 'designedHoles'],
      render: (v: number) => v ? `${v} 个` : '无计划',
    },
    {
      title: '申请雷管',
      dataIndex: 'detonatorQuantity',
      render: (v: number, record: PickupApplication) => {
        const analysis = calculateAnalysis(record);
        if (!analysis) return <span>{v} 发</span>;
        const tagColor = analysis.detonatorInTolerance && analysis.holeCountMatch ? 'green' : 'orange';
        return (
          <Space direction="vertical" size={2}>
            <span><Text strong>{v}</Text> 发 / 设计 {analysis.designedDetonators} 发</span>
            {!analysis.holeCountMatch && (
              <Tag icon={<ExclamationCircleOutlined />} color="warning">
                孔数不匹配
              </Tag>
            )}
            {!analysis.detonatorInTolerance && (
              <Tag icon={<WarningOutlined />} color={tagColor}>
                偏差{analysis.detonatorDiff >= 0 ? '+' : ''}{analysis.detonatorDiff.toFixed(1)}%
              </Tag>
            )}
          </Space>
        );
      },
    },
    {
      title: '申请炸药',
      dataIndex: 'explosiveQuantity',
      render: (v: number, record: PickupApplication) => {
        const analysis = calculateAnalysis(record);
        if (!analysis) return <span>{v} kg</span>;
        const tagColor = analysis.explosiveInTolerance ? 'green' : 'orange';
        return (
          <Space direction="vertical" size={2}>
            <span><Text strong>{v}</Text> kg / 设计 {analysis.designedExplosives} kg</span>
            {!analysis.explosiveInTolerance && (
              <Tag icon={<WarningOutlined />} color={tagColor}>
                偏差{analysis.explosiveDiff >= 0 ? '+' : ''}{analysis.explosiveDiff.toFixed(1)}%
              </Tag>
            )}
          </Space>
        );
      },
    },
    { title: '申请时间', dataIndex: 'createdAt', render: (t: string) => dayjs(t).format('MM-DD HH:mm') },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: PickupApplication) => (
        <Space>
          <Button
            type="primary"
            size="small"
            icon={<CheckOutlined />}
            onClick={() => { setSelectedApp(record); handleReview('APPROVED'); }}
          >
            批准
          </Button>
          <Button
            danger
            size="small"
            icon={<CloseOutlined />}
            onClick={() => { setSelectedApp(record); handleReview('REJECTED'); }}
          >
            拒绝
          </Button>
        </Space>
      ),
    },
  ];

  const renderMismatchAnalysis = (app: PickupApplication) => {
    const analysis = calculateAnalysis(app);
    if (!analysis) return null;
    const issues: string[] = [];
    if (!analysis.holeCountMatch) {
      issues.push(`雷管数量与设计孔数不匹配：设计孔数${analysis.designedHoles}个，申请雷管${app.detonatorQuantity}发`);
    }
    if (!analysis.detonatorInTolerance) {
      issues.push(`雷管数量超出设计量±${DETONATOR_TOLERANCE}%：设计${analysis.designedDetonators}发，申请${app.detonatorQuantity}发（偏差${analysis.detonatorDiff >= 0 ? '+' : ''}${analysis.detonatorDiff.toFixed(1)}%）`);
    }
    if (!analysis.explosiveInTolerance) {
      issues.push(`炸药数量超出设计量±${EXPLOSIVE_TOLERANCE}%：设计${analysis.designedExplosives}kg，申请${app.explosiveQuantity}kg（偏差${analysis.explosiveDiff >= 0 ? '+' : ''}${analysis.explosiveDiff.toFixed(1)}%）`);
    }
    return issues;
  };

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '24px' }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/safety')} style={{ marginRight: '16px' }}>
          返回
        </Button>
        <Title level={3} style={{ margin: 0 }}>复核申请</Title>
      </div>

      <Card style={{ marginBottom: '24px' }}>
        <Alert
          type="warning"
          showIcon
          icon={<SafetyOutlined />}
          message="安全复核规则"
          description={
            <ul style={{ margin: 0, paddingLeft: '20px' }}>
              <li>雷管数量必须与设计孔数完全一致，否则需要复核</li>
              <li>雷管数量与设计量偏差超过 ±{DETONATOR_TOLERANCE}%，需要复核</li>
              <li>炸药数量与设计量偏差超过 ±{EXPLOSIVE_TOLERANCE}%，需要复核</li>
              <li>复核通过后库管可进行出库操作，拒绝后需要爆破员重新提交申请</li>
            </ul>
          }
        />
      </Card>

      <Card title="待复核申请列表">
        {applications.length === 0 ? (
          <Empty description="暂无待复核申请" />
        ) : (
          <Table
            dataSource={applications}
            columns={columns}
            rowKey="id"
            pagination={false}
          />
        )}
      </Card>

      <Modal
        title={
          <Space>
            <SafetyOutlined style={{ color: reviewAction === 'APPROVED' ? '#52c41a' : '#ff4d4f' }} />
            <span>{reviewAction === 'APPROVED' ? '批准领用申请' : '拒绝领用申请'}</span>
          </Space>
        }
        open={reviewModalVisible}
        onCancel={() => setReviewModalVisible(false)}
        width={720}
        footer={[
          <Button key="cancel" onClick={() => setReviewModalVisible(false)}>取消</Button>,
          <Button
            key="submit"
            type={reviewAction === 'APPROVED' ? 'primary' : 'primary'}
            danger={reviewAction === 'REJECTED'}
            loading={loading}
            onClick={handleSubmitReview}
          >
            {reviewAction === 'APPROVED' ? '确认批准' : '确认拒绝'}
          </Button>,
        ]}
      >
        {selectedApp && (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            {selectedApp.shift.workPlan && (
              <Card
                size="small"
                title={
                  <Space>
                    <SafetyOutlined style={{ color: '#1890ff' }} />
                    <span>作业计划设计信息</span>
                    <Tag color="blue">{selectedApp.shift.workPlan.planNo}</Tag>
                  </Space>
                }
                style={{ backgroundColor: '#f0f8ff' }}
              >
                <Descriptions column={2} size="small">
                  <Descriptions.Item label="作业面">{selectedApp.shift.workFace}</Descriptions.Item>
                  <Descriptions.Item label="设计孔数">
                    <Text type="danger" strong>{selectedApp.shift.workPlan.designedHoles} 个</Text>
                  </Descriptions.Item>
                  <Descriptions.Item label="设计雷管">
                    <Text strong>{selectedApp.shift.workPlan.estimatedDetonators} 发</Text>
                  </Descriptions.Item>
                  <Descriptions.Item label="设计炸药">
                    <Text strong>{selectedApp.shift.workPlan.estimatedExplosives} kg</Text>
                  </Descriptions.Item>
                </Descriptions>
              </Card>
            )}

            {selectedApp.reviewRemark && (
              <Alert
                type="warning"
                showIcon
                icon={<WarningOutlined />}
                message="系统检测到的不匹配问题"
                description={selectedApp.reviewRemark}
              />
            )}

            {(() => {
              const issues = renderMismatchAnalysis(selectedApp);
              const analysis = calculateAnalysis(selectedApp);
              if (!analysis) return null;
              return (
                <Row gutter={[16, 16]}>
                  <Col span={12}>
                    <Card size="small" title="雷管数量核对">
                      <Statistic
                        title="申请/设计"
                        value={`${selectedApp.detonatorQuantity} / ${analysis.designedDetonators}`}
                        suffix="发"
                        valueStyle={{ color: analysis.detonatorInTolerance && analysis.holeCountMatch ? '#3f8600' : '#cf1322' }}
                      />
                      <div style={{ marginTop: '12px' }}>
                        <Progress
                          percent={Math.round((selectedApp.detonatorQuantity / analysis.designedDetonators) * 100)}
                          status={analysis.detonatorInTolerance && analysis.holeCountMatch ? 'success' : 'exception'}
                        />
                      </div>
                      <div style={{ marginTop: '8px' }}>
                        <Space direction="vertical" size={4}>
                          {analysis.holeCountMatch ? (
                            <Tag color="success">✓ 与设计孔数匹配</Tag>
                          ) : (
                            <Tag color="warning">✗ 孔数不匹配</Tag>
                          )}
                          {analysis.detonatorInTolerance ? (
                            <Tag color="success">✓ 偏差在±{DETONATOR_TOLERANCE}%内</Tag>
                          ) : (
                            <Tag color="error">✗ 偏差{analysis.detonatorDiff >= 0 ? '+' : ''}{analysis.detonatorDiff.toFixed(1)}%</Tag>
                          )}
                        </Space>
                      </div>
                    </Card>
                  </Col>
                  <Col span={12}>
                    <Card size="small" title="炸药数量核对">
                      <Statistic
                        title="申请/设计"
                        value={`${selectedApp.explosiveQuantity} / ${analysis.designedExplosives}`}
                        suffix="kg"
                        valueStyle={{ color: analysis.explosiveInTolerance ? '#3f8600' : '#cf1322' }}
                      />
                      <div style={{ marginTop: '12px' }}>
                        <Progress
                          percent={Math.round((selectedApp.explosiveQuantity / analysis.designedExplosives) * 100)}
                          status={analysis.explosiveInTolerance ? 'success' : 'exception'}
                        />
                      </div>
                      <div style={{ marginTop: '8px' }}>
                        <Space direction="vertical" size={4}>
                          {analysis.explosiveInTolerance ? (
                            <Tag color="success">✓ 偏差在±{EXPLOSIVE_TOLERANCE}%内</Tag>
                          ) : (
                            <Tag color="error">✗ 偏差{analysis.explosiveDiff >= 0 ? '+' : ''}{analysis.explosiveDiff.toFixed(1)}%</Tag>
                          )}
                        </Space>
                      </div>
                    </Card>
                  </Col>
                </Row>
              );
            })()}

            <Divider style={{ margin: '8px 0' }} />

            <Descriptions column={2} size="small" bordered>
              <Descriptions.Item label="申请单号">{selectedApp.applicationNo}</Descriptions.Item>
              <Descriptions.Item label="爆破员">{selectedApp.blaster.name}</Descriptions.Item>
              <Descriptions.Item label="当班作业">{selectedApp.shift.shiftNo}</Descriptions.Item>
              <Descriptions.Item label="申请时间">
                {dayjs(selectedApp.createdAt).format('YYYY-MM-DD HH:mm')}
              </Descriptions.Item>
            </Descriptions>

            <Form form={form} layout="vertical">
              <Form.Item
                name="reviewRemark"
                label={
                  <Space>
                    {reviewAction === 'APPROVED' ? '批准意见' : '拒绝原因'}
                    {reviewAction === 'REJECTED' && <Text type="danger">（必填）</Text>}
                  </Space>
                }
                rules={reviewAction === 'REJECTED' ? [{ required: true, message: '请填写拒绝原因' }] : []}
              >
                <TextArea
                  rows={4}
                  placeholder={reviewAction === 'APPROVED'
                    ? '请输入批准意见（可选），例如：经现场核实作业条件变化，同意增加用量'
                    : '请详细说明拒绝原因，例如：数量偏差过大，请重新核实设计参数'}
                />
              </Form.Item>
            </Form>
          </Space>
        )}
      </Modal>
    </div>
  );
}
