import { useState, useEffect } from 'react';
import { Card, List, Tag, Button, Space, Statistic, Row, Col, Typography, Empty, message } from 'antd';
import { ScanOutlined, InboxOutlined, ClockCircleOutlined, CheckCircleOutlined, WarningOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { outboundApi, inboundApi, applicationApi, shiftApi } from '../../services/api';
import type { OutboundRecord, InboundRecord, PickupApplication, Shift } from '../../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const appStatusColors: Record<string, string> = {
  APPROVED: 'green',
  OUTBOUND_COMPLETED: 'blue',
  INBOUND_COMPLETED: 'cyan',
  NEED_REVIEW: 'orange',
};

const appStatusNames: Record<string, string> = {
  APPROVED: '已批准待出库',
  OUTBOUND_COMPLETED: '已出库待回库',
  INBOUND_COMPLETED: '已回库待核对',
  NEED_REVIEW: '待复核',
};

export default function StorekeeperDashboard() {
  const navigate = useNavigate();
  const [outboundRecords, setOutboundRecords] = useState<OutboundRecord[]>([]);
  const [inboundRecords, setInboundRecords] = useState<InboundRecord[]>([]);
  const [applications, setApplications] = useState<PickupApplication[]>([]);
  const [shifts, setShifts] = useState<Shift[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [outRes, inRes, appRes, shiftRes] = await Promise.all([
        outboundApi.list(),
        inboundApi.list(),
        applicationApi.list(),
        shiftApi.list(),
      ]);
      if (outRes.success) setOutboundRecords(outRes.data);
      if (inRes.success) setInboundRecords(inRes.data);
      if (appRes.success) setApplications(appRes.data);
      if (shiftRes.success) setShifts(shiftRes.data);
    } catch (error) {
      message.error('加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  const pendingOutbound = applications.filter(a => a.status === 'APPROVED');
  const pendingInbound = applications.filter(a => a.status === 'OUTBOUND_COMPLETED');
  const activeShifts = shifts.filter(s => s.status !== 'CLOSED');

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <Title level={3} style={{ margin: 0 }}>库管工作台</Title>
        <Space>
          <Button type="primary" icon={<ScanOutlined />} onClick={() => navigate('/storekeeper/outbound')}>
            扫码出库
          </Button>
          <Button icon={<InboxOutlined />} onClick={() => navigate('/storekeeper/inbound')}>
            扫码回库
          </Button>
        </Space>
      </div>

      <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
        <Col span={6}>
          <Card>
            <Statistic title="进行中的当班" value={activeShifts.length} prefix={<ClockCircleOutlined style={{ color: '#1890ff' }} />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="待出库申请" value={pendingOutbound.length} prefix={<ScanOutlined style={{ color: '#faad14' }} />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="待回库申请" value={pendingInbound.length} prefix={<InboxOutlined style={{ color: '#13c2c2' }} />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="今日出库记录" value={outboundRecords.filter(r => dayjs(r.outboundTime).isSame(dayjs(), 'day')).length} prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />} />
          </Card>
        </Col>
      </Row>

      <Card title="待处理申请" style={{ marginBottom: '24px' }} loading={loading}>
        {[...pendingOutbound, ...pendingInbound].length === 0 ? (
          <Empty description="暂无待处理申请" />
        ) : (
          <List
            dataSource={[...pendingOutbound, ...pendingInbound]}
            renderItem={(app) => (
              <List.Item
                key={app.id}
                actions={[
                  app.status === 'APPROVED' && (
                    <Button type="primary" size="small" onClick={() => navigate('/storekeeper/outbound')}>
                      去出库
                    </Button>
                  ),
                  app.status === 'OUTBOUND_COMPLETED' && (
                    <Button type="primary" size="small" onClick={() => navigate('/storekeeper/inbound')}>
                      去回库
                    </Button>
                  ),
                ].filter(Boolean)}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      <Text strong>{app.applicationNo}</Text>
                      <Tag color={appStatusColors[app.status]}>{appStatusNames[app.status]}</Tag>
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

      <Card title="今日出库记录" loading={loading}>
        {outboundRecords.filter(r => dayjs(r.outboundTime).isSame(dayjs(), 'day')).length === 0 ? (
          <Empty description="今日暂无出库记录" />
        ) : (
          <List
            dataSource={outboundRecords.filter(r => dayjs(r.outboundTime).isSame(dayjs(), 'day'))}
            renderItem={(record) => (
              <List.Item key={record.id}>
                <List.Item.Meta
                  title={
                    <Space>
                      <Text strong>{record.outboundNo}</Text>
                      <Tag color={record.type === 'DETONATOR' ? 'orange' : 'red'}>
                        {record.type === 'DETONATOR' ? '雷管' : '炸药'}
                      </Tag>
                    </Space>
                  }
                  description={
                    <Space split="|" size="small">
                      <span>器材编号: {record.explosiveSerialNo}</span>
                      <span>数量: {record.quantity}{record.type === 'DETONATOR' ? '发' : 'kg'}</span>
                      <span>爆破员: {record.blaster.name}</span>
                      <span>作业面: {record.workFace}</span>
                      <span>出库时间: {dayjs(record.outboundTime).format('HH:mm:ss')}</span>
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
