import { useState, useEffect } from 'react';
import { Form, Select, Button, Card, Typography, message, Space, Table, Tag, Divider, Modal, Input, Empty } from 'antd';
import { ArrowLeftOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { applicationApi } from '../../services/api';
import type { PickupApplication } from '../../types';
import dayjs from 'dayjs';

const { Title, Text, Paragraph } = Typography;
const { Option } = Select;
const { TextArea } = Input;

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
        action: reviewAction,
        reviewRemark: values.reviewRemark,
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
        const designed = record.shift.workPlan?.estimatedDetonators || 0;
        const diff = v - designed;
        return (
          <Space>
            <span>{v} 发</span>
            {diff !== 0 && (
              <Tag color={diff > 0 ? 'orange' : 'blue'}>
                {diff > 0 ? '+' : ''}{diff}
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
        const designed = record.shift.workPlan?.estimatedExplosives || 0;
        const diff = v - designed;
        return (
          <Space>
            <span>{v} kg</span>
            {diff !== 0 && (
              <Tag color={diff > 0 ? 'orange' : 'blue'}>
                {diff > 0 ? '+' : ''}{diff}
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

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '24px' }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/safety')} style={{ marginRight: '16px' }}>
          返回
        </Button>
        <Title level={3} style={{ margin: 0 }}>复核申请</Title>
      </div>

      <Card style={{ marginBottom: '24px' }}>
        <Paragraph type="secondary">
          <Text strong>复核规则说明：</Text>
          <br />• 当领用雷管数量与设计孔数偏差超过 ±10% 时，需要安全负责人复核
          <br />• 当领用炸药数量与设计用量偏差超过 ±200% 时，需要安全负责人复核
          <br />• 复核通过后库管可进行出库操作，拒绝后需要爆破员重新提交申请
        </Paragraph>
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
        title={reviewAction === 'APPROVED' ? '批准申请' : '拒绝申请'}
        open={reviewModalVisible}
        onCancel={() => setReviewModalVisible(false)}
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
            <div>
              <Text strong>申请单：</Text>
              {selectedApp.applicationNo}
            </div>
            <div>
              <Text strong>爆破员：</Text>
              {selectedApp.blaster.name}
            </div>
            <div>
              <Text strong>申请数量：</Text>
              雷管 {selectedApp.detonatorQuantity} 发，炸药 {selectedApp.explosiveQuantity} kg
            </div>
            <div>
              <Text strong>设计数量：</Text>
              雷管 {selectedApp.shift.workPlan?.estimatedDetonators || '无'} 发，炸药 {selectedApp.shift.workPlan?.estimatedExplosives || '无'} kg
            </div>
            <Divider style={{ margin: '8px 0' }} />
            <Form form={form} layout="vertical">
              <Form.Item
                name="reviewRemark"
                label={reviewAction === 'APPROVED' ? '批准意见（可选）' : '拒绝原因（必填）'}
                rules={reviewAction === 'REJECTED' ? [{ required: true, message: '请填写拒绝原因' }] : []}
              >
                <TextArea rows={3} placeholder={reviewAction === 'APPROVED' ? '请输入意见' : '请说明拒绝原因'} />
              </Form.Item>
            </Form>
          </Space>
        )}
      </Modal>
    </div>
  );
}
