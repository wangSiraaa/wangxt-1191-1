import { useState, useEffect } from 'react';
import { Card, List, Tag, Button, Space, Statistic, Row, Col, Typography, Empty, message } from 'antd';
import { CheckCircleOutlined, WarningOutlined, ExclamationCircleOutlined, SafetyOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { anomalyApi, applicationApi, verificationApi, shiftApi } from '../../services/api';
import type { AnomalyRecord, PickupApplication, VerificationRecord, Shift } from '../../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const anomalyTypeNames: Record<string, string> = {
  EXPIRED_LICENSE: '作业证过期',
  QUANTITY_MISMATCH: '数量不匹配',
  NOT_RETURNED: '未退回器材',
  DAMAGE: '器材损坏',
  OTHER: '其他异常',
};

const anomalyTypeColors: Record<string, string> = {
  EXPIRED_LICENSE: 'red',
  QUANTITY_MISMATCH: 'orange',
  NOT_RETURNED: 'gold',
  DAMAGE: 'magenta',
  OTHER: 'default',
};

export default function SafetyDashboard() {
  const navigate = useNavigate();
  const [anomalies, setAnomalies] = useState<AnomalyRecord[]>([]);
  const [applications, setApplications] = useState<PickupApplication[]>([]);
  const [verifications, setVerifications] = useState<VerificationRecord[]>([]);
  const [shifts, setShifts] = useState<Shift[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [anomRes, appRes, verRes, shiftRes] = await Promise.all([
        anomalyApi.list(),
        applicationApi.needReview(),
        verificationApi.list(),
        shiftApi.list(),
      ]);
      if (anomRes.success) setAnomalies(anomRes.data);
      if (appRes.success) setApplications(appRes.data);
      if (verRes.success) setVerifications(verRes.data);
      if (shiftRes.success) setShifts(shiftRes.data);
    } catch (error) {
      message.error('加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  const unresolvedAnomalies = anomalies.filter(a => !a.resolved);
  const pendingReview = applications.filter(a => a.status === 'NEED_REVIEW');
  const pendingVerify = applications.filter(a => a.status === 'INBOUND_COMPLETED');
  const activeShifts = shifts.filter(s => s.status !== 'CLOSED');

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <Title level={3} style={{ margin: 0 }}>安全负责人工作台</Title>
        <Space>
          <Button type="primary" icon={<CheckCircleOutlined />} onClick={() => navigate('/safety/review')}>
            复核申请
          </Button>
          <Button icon={<SafetyOutlined />} onClick={() => navigate('/safety/verification')}>
            器材核对
          </Button>
          <Button icon={<ExclamationCircleOutlined />} onClick={() => navigate('/safety/anomalies')}>
            异常管理
          </Button>
        </Space>
      </div>

      <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
        <Col span={6}>
          <Card>
            <Statistic title="进行中的当班" value={activeShifts.length} prefix={<CheckCircleOutlined style={{ color: '#1890ff' }} />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="待复核申请" value={pendingReview.length} prefix={<WarningOutlined style={{ color: '#faad14' }} />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="待核对申请" value={pendingVerify.length} prefix={<SafetyOutlined style={{ color: '#13c2c2' }} />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="未处理异常" value={unresolvedAnomalies.length} prefix={<ExclamationCircleOutlined style={{ color: '#ff4d4f' }} />} />
          </Card>
        </Col>
      </Row>

      <Card title="待复核申请" style={{ marginBottom: '24px' }} loading={loading}>
        {pendingReview.length === 0 ? (
          <Empty description="暂无待复核申请" />
        ) : (
          <List
            dataSource={pendingReview}
            renderItem={(app) => (
              <List.Item
                key={app.id}
                actions={[
                  <Button type="primary" size="small" onClick={() => navigate('/safety/review')}>
                    去复核
                  </Button>,
                ]}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      <Text strong>{app.applicationNo}</Text>
                      <Tag color="orange">待复核</Tag>
                    </Space>
                  }
                  description={
                    <Space split="|" size="small">
                      <span>爆破员: {app.blaster.name}</span>
                      <span>当班: {app.shift.shiftNo}</span>
                      <span>申请雷管: {app.detonatorQuantity}发</span>
                      <span>申请炸药: {app.explosiveQuantity}kg</span>
                      <Text type="warning">原因: 领用数量与设计孔数不匹配</Text>
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </Card>

      <Card title="待核对申请" style={{ marginBottom: '24px' }} loading={loading}>
        {pendingVerify.length === 0 ? (
          <Empty description="暂无待核对申请" />
        ) : (
          <List
            dataSource={pendingVerify}
            renderItem={(app) => (
              <List.Item
                key={app.id}
                actions={[
                  <Button type="primary" size="small" onClick={() => navigate('/safety/verification')}>
                    去核对
                  </Button>,
                ]}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      <Text strong>{app.applicationNo}</Text>
                      <Tag color="cyan">待核对</Tag>
                    </Space>
                  }
                  description={
                    <Space split="|" size="small">
                      <span>爆破员: {app.blaster.name}</span>
                      <span>当班: {app.shift.shiftNo}</span>
                      <span>雷管: {app.detonatorQuantity}发</span>
                      <span>炸药: {app.explosiveQuantity}kg</span>
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </Card>

      <Card title="未处理异常" loading={loading}>
        {unresolvedAnomalies.length === 0 ? (
          <Empty description="暂无未处理异常" />
        ) : (
          <List
            dataSource={unresolvedAnomalies}
            renderItem={(anomaly) => (
              <List.Item
                key={anomaly.id}
                actions={[
                  <Button type="link" onClick={() => navigate('/safety/anomalies')}>
                    处理
                  </Button>,
                ]}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      <Text strong>{anomaly.recordNo}</Text>
                      <Tag color={anomalyTypeColors[anomaly.type]}>
                        {anomalyTypeNames[anomaly.type]}
                      </Tag>
                    </Space>
                  }
                  description={
                    <Space split="|" size="small">
                      <span>描述: {anomaly.description}</span>
                      {anomaly.explosiveSerialNo && <span>器材: {anomaly.explosiveSerialNo}</span>}
                      {anomaly.anomalyQuantity && <span>涉及数量: {anomaly.anomalyQuantity}</span>}
                      <span>上报人: {anomaly.reportedBy.name}</span>
                      <span>上报时间: {dayjs(anomaly.reportedAt).format('YYYY-MM-DD HH:mm')}</span>
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </Card>
    </div>
  );
}
