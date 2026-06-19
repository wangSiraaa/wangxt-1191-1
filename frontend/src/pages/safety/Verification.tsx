import { useState, useEffect } from 'react';
import { Form, Select, Input, InputNumber, Button, Card, Typography, message, Space, Table, Tag, Divider, Alert, Row, Col, Empty } from 'antd';
import { ArrowLeftOutlined, SafetyOutlined, CheckCircleOutlined, WarningOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { applicationApi, verificationApi, inboundApi, outboundApi } from '../../services/api';
import type { PickupApplication, VerificationRecord, OutboundRecord, InboundRecord } from '../../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;

interface VerifyData {
  expectedDetonators: number;
  expectedExplosives: number;
  usedDetonators: number;
  usedExplosives: number;
  returnedDetonators: number;
  returnedExplosives: number;
  allReturned: boolean;
}

export default function Verification() {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [applications, setApplications] = useState<PickupApplication[]>([]);
  const [selectedApp, setSelectedApp] = useState<PickupApplication | null>(null);
  const [verifyData, setVerifyData] = useState<VerifyData | null>(null);
  const [outboundRecords, setOutboundRecords] = useState<OutboundRecord[]>([]);
  const [inboundRecords, setInboundRecords] = useState<InboundRecord[]>([]);
  const [verificationRecords, setVerificationRecords] = useState<VerificationRecord[]>([]);

  useEffect(() => {
    loadApplications();
    loadVerifications();
  }, []);

  const loadApplications = async () => {
    try {
      const res = await applicationApi.list({ status: 'INBOUND_COMPLETED' });
      if (res.success) setApplications(res.data);
    } catch (error) {
      message.error('加载申请单失败');
    }
  };

  const loadVerifications = async () => {
    try {
      const res = await verificationApi.list();
      if (res.success) setVerificationRecords(res.data);
    } catch (error) {
      message.error('加载核对记录失败');
    }
  };

  const handleAppChange = async (appId: number) => {
    const app = applications.find(a => a.id === appId);
    setSelectedApp(app || null);

    if (app) {
      try {
        const [outRes, inRes] = await Promise.all([
          outboundApi.list({ applicationId: appId }),
          inboundApi.list({ applicationId: appId }),
        ]);

        if (outRes.success) setOutboundRecords(outRes.data);
        if (inRes.success) setInboundRecords(inRes.data);

        const expectedDetonators = app.detonatorQuantity;
        const expectedExplosives = app.explosiveQuantity;

        const usedDetonators = inRes.data
          .filter(r => r.type === 'DETONATOR')
          .reduce((sum, r) => sum + r.usedQuantity, 0);
        const usedExplosives = inRes.data
          .filter(r => r.type === 'EXPLOSIVE')
          .reduce((sum, r) => sum + r.usedQuantity, 0);

        const returnedDetonators = inRes.data
          .filter(r => r.type === 'DETONATOR')
          .reduce((sum, r) => sum + r.returnedQuantity, 0);
        const returnedExplosives = inRes.data
          .filter(r => r.type === 'EXPLOSIVE')
          .reduce((sum, r) => sum + r.returnedQuantity, 0);

        const allReturned =
          (usedDetonators + returnedDetonators === expectedDetonators) &&
          (usedExplosives + returnedExplosives === expectedExplosives) &&
          (returnedDetonators + usedDetonators === expectedDetonators) &&
          (returnedExplosives + usedExplosives === expectedExplosives);

        const data: VerifyData = {
          expectedDetonators,
          expectedExplosives,
          usedDetonators,
          usedExplosives,
          returnedDetonators,
          returnedExplosives,
          allReturned,
        };

        setVerifyData(data);
        form.setFieldsValue(data);
      } catch (error) {
        message.error('加载数据失败');
      }
    }
  };

  const handleSubmit = async () => {
    if (!selectedApp || !verifyData) {
      message.error('请选择申请单');
      return;
    }

    try {
      const values = await form.validateFields();
      setLoading(true);

      const requestData = {
        applicationId: selectedApp.id,
        expectedDetonators: verifyData.expectedDetonators,
        usedDetonators: values.usedDetonators,
        returnedDetonators: values.returnedDetonators,
        expectedExplosives: verifyData.expectedExplosives,
        usedExplosives: values.usedExplosives,
        returnedExplosives: values.returnedExplosives,
        allReturned: values.allReturned,
        verificationRemark: values.verificationRemark,
      };

      const res = await verificationApi.create(requestData);
      if (res.success) {
        message.success('核对完成');
        setSelectedApp(null);
        setVerifyData(null);
        form.resetFields();
        loadApplications();
        loadVerifications();
      } else {
        message.error(res.message);
      }
    } catch (error: any) {
      message.error(error.response?.data?.message || '核对失败');
    } finally {
      setLoading(false);
    }
  };

  const recordColumns = [
    {
      title: '类型',
      dataIndex: 'type',
      render: (type: string) => (
        <Tag color={type === 'DETONATOR' ? 'orange' : 'red'}>
          {type === 'DETONATOR' ? '雷管' : '炸药'}
        </Tag>
      ),
    },
    { title: '器材编号', dataIndex: 'explosiveSerialNo' },
    { title: '出库数量', dataIndex: 'quantity', render: (q: number, record: OutboundRecord) => `${q}${record.type === 'DETONATOR' ? '发' : 'kg'}` },
    { title: '出库时间', dataIndex: 'outboundTime', render: (t: string) => dayjs(t).format('HH:mm:ss') },
  ];

  const inboundColumns = [
    {
      title: '类型',
      dataIndex: 'type',
      render: (type: string) => (
        <Tag color={type === 'DETONATOR' ? 'orange' : 'red'}>
          {type === 'DETONATOR' ? '雷管' : '炸药'}
        </Tag>
      ),
    },
    { title: '器材编号', dataIndex: 'explosiveSerialNo' },
    { title: '使用数量', dataIndex: 'usedQuantity', render: (q: number, record: InboundRecord) => `${q}${record.type === 'DETONATOR' ? '发' : 'kg'}` },
    { title: '退回数量', dataIndex: 'returnedQuantity', render: (q: number, record: InboundRecord) => `${q}${record.type === 'DETONATOR' ? '发' : 'kg'}` },
    { title: '回库时间', dataIndex: 'inboundTime', render: (t: string) => dayjs(t).format('HH:mm:ss') },
  ];

  const verifyColumns = [
    { title: '核对单号', dataIndex: 'verificationNo' },
    { title: '安全负责人', dataIndex: ['safetyOfficer', 'name'] },
    {
      title: '雷管核对',
      render: (_: any, record: VerificationRecord) => (
        <Space>
          <span>应发: {record.expectedDetonators}发</span>
          <Text type="success">使用: {record.usedDetonators}发</Text>
          <Text type="secondary">退回: {record.returnedDetonators}发</Text>
          {record.allReturned ? <Tag color="green">已清零</Tag> : <Tag color="red">未清零</Tag>}
        </Space>
      ),
    },
    {
      title: '炸药核对',
      render: (_: any, record: VerificationRecord) => (
        <Space>
          <span>应发: {record.expectedExplosives}kg</span>
          <Text type="success">使用: {record.usedExplosives}kg</Text>
          <Text type="secondary">退回: {record.returnedExplosives}kg</Text>
        </Space>
      ),
    },
    { title: '核对时间', dataIndex: 'verificationTime', render: (t: string) => dayjs(t).format('YYYY-MM-DD HH:mm') },
  ];

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '24px' }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/safety')} style={{ marginRight: '16px' }}>
          返回
        </Button>
        <Title level={3} style={{ margin: 0 }}>器材核对</Title>
      </div>

      <Card style={{ marginBottom: '24px' }}>
        <Form form={form} layout="vertical" size="large">
          <Form.Item
            name="applicationId"
            label="选择申请单"
            rules={[{ required: true, message: '请选择申请单' }]}
          >
            <Select
              placeholder="请选择待核对的申请单"
              onChange={handleAppChange}
              disabled={applications.length === 0}
            >
              {applications.map(app => (
                <Option key={app.id} value={app.id}>
                  {app.applicationNo} - {app.blaster.name} - 雷管{app.detonatorQuantity}发/炸药{app.explosiveQuantity}kg
                </Option>
              ))}
            </Select>
          </Form.Item>
        </Form>
      </Card>

      {verifyData && (
        <>
          <Alert
            type={verifyData.allReturned ? 'success' : 'warning'}
            showIcon
            icon={verifyData.allReturned ? <CheckCircleOutlined /> : <WarningOutlined />}
            message={verifyData.allReturned ? '器材已全部退回' : '存在未退回器材，请确认'}
            style={{ marginBottom: '24px' }}
          />

          <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
            <Col span={12}>
              <Card title="雷管核对" size="small">
                <Space direction="vertical" style={{ width: '100%' }}>
                  <div><Text strong>应发数量：</Text>{verifyData.expectedDetonators} 发</div>
                  <div><Text type="success">使用数量：</Text>{verifyData.usedDetonators} 发</div>
                  <div><Text type="secondary">退回数量：</Text>{verifyData.returnedDetonators} 发</div>
                  <div>
                    <Text strong>核对结果：</Text>
                    {verifyData.usedDetonators + verifyData.returnedDetonators === verifyData.expectedDetonators ? (
                      <Tag color="green">数量一致</Tag>
                    ) : (
                      <Tag color="red">数量不符</Tag>
                    )}
                  </div>
                </Space>
              </Card>
            </Col>
            <Col span={12}>
              <Card title="炸药核对" size="small">
                <Space direction="vertical" style={{ width: '100%' }}>
                  <div><Text strong>应发数量：</Text>{verifyData.expectedExplosives} kg</div>
                  <div><Text type="success">使用数量：</Text>{verifyData.usedExplosives} kg</div>
                  <div><Text type="secondary">退回数量：</Text>{verifyData.returnedExplosives} kg</div>
                  <div>
                    <Text strong>核对结果：</Text>
                    {verifyData.usedExplosives + verifyData.returnedExplosives === verifyData.expectedExplosives ? (
                      <Tag color="green">数量一致</Tag>
                    ) : (
                      <Tag color="red">数量不符</Tag>
                    )}
                  </div>
                </Space>
              </Card>
            </Col>
          </Row>

          <Card title="出库记录" size="small" style={{ marginBottom: '24px' }}>
            <Table
              dataSource={outboundRecords}
              columns={recordColumns}
              rowKey="id"
              pagination={false}
              size="small"
            />
          </Card>

          <Card title="回库记录" size="small" style={{ marginBottom: '24px' }}>
            <Table
              dataSource={inboundRecords}
              columns={inboundColumns}
              rowKey="id"
              pagination={false}
              size="small"
            />
          </Card>

          <Card>
            <Form form={form} layout="vertical">
              <Form.Item name="verificationRemark" label="核对备注">
                <TextArea rows={2} placeholder="请输入备注信息（可选）" />
              </Form.Item>
              <Form.Item>
                <Space>
                  <Button
                    type="primary"
                    icon={<SafetyOutlined />}
                    onClick={handleSubmit}
                    loading={loading}
                  >
                    确认核对并关闭申请
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </Card>
        </>
      )}

      <Divider />

      <Card title="历史核对记录">
        {verificationRecords.length === 0 ? (
          <Empty description="暂无核对记录" />
        ) : (
          <Table
            dataSource={verificationRecords}
            columns={verifyColumns}
            rowKey="id"
            pagination={{ pageSize: 10 }}
          />
        )}
      </Card>
    </div>
  );
}
