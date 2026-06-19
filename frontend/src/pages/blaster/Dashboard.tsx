import { useState, useEffect } from 'react';
import { Card, List, Tag, Button, Space, Statistic, Row, Col, Typography, Empty, message } from 'antd';
import { PlusOutlined, ClockCircleOutlined, CheckCircleOutlined, WarningOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { shiftApi, applicationApi } from '../../services/api';
import type { Shift, PickupApplication } from '../../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const statusColors: Record<string, string> = {
  OPEN: 'blue',
  IN_PROGRESS: 'orange',
  WAITING_RETURN: 'gold',
  WAITING_VERIFY: 'cyan',
  CLOSED: 'green',
};

const statusNames: Record<string, string> = {
  OPEN: '待开始',
  IN_PROGRESS: '进行中',
  WAITING_RETURN: '待回库',
  WAITING_VERIFY: '待核对',
  CLOSED: '已关闭',
};

export default function BlasterDashboard() {
  const navigate = useNavigate();
  const [shifts, setShifts] = useState<Shift[]>([]);
  const [applications, setApplications] = useState<PickupApplication[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [shiftRes, appRes] = await Promise.all([
        shiftApi.list(),
        applicationApi.list(),
      ]);
      if (shiftRes.success) setShifts(shiftRes.data);
      if (appRes.success) setApplications(appRes.data);
    } catch (error) {
      message.error('加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  const activeShifts = shifts.filter(s => s.status !== 'CLOSED');
  const pendingApps = applications.filter(a => a.status === 'PENDING' || a.status === 'NEED_REVIEW');
  const closedApps = applications.filter(a => a.status === 'CLOSED');

  const handleCloseShift = async (shiftId: number) => {
    try {
      const res = await shiftApi.close(shiftId);
      if (res.success) {
        message.success('当班作业已关闭');
        loadData();
      } else {
        message.error(res.message);
      }
    } catch (error: any) {
      message.error(error.response?.data?.message || '关闭失败');
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <Title level={3} style={{ margin: 0 }}>爆破员工作台</Title>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/blaster/create-shift')}>
            创建当班作业
          </Button>
          <Button icon={<PlusOutlined />} onClick={() => navigate('/blaster/create-application')}>
            领用申请
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
            <Statistic title="待审核申请" value={pendingApps.length} prefix={<WarningOutlined style={{ color: '#faad14' }} />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="已完成申请" value={closedApps.length} prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="累计当班作业" value={shifts.length} />
          </Card>
        </Col>
      </Row>

      <Card title="我的当班作业" style={{ marginBottom: '24px' }} loading={loading}>
        {shifts.length === 0 ? (
          <Empty description="暂无当班作业" />
        ) : (
          <List
            dataSource={shifts}
            renderItem={(shift) => (
              <List.Item
                key={shift.id}
                actions={[
                  shift.status !== 'CLOSED' && (
                    <Button type="link" onClick={() => handleCloseShift(shift.id)}>
                      关闭当班
                    </Button>
                  ),
                ].filter(Boolean)}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      <Text strong>{shift.shiftNo}</Text>
                      <Tag color={statusColors[shift.status]}>{statusNames[shift.status]}</Tag>
                    </Space>
                  }
                  description={
                    <Space split="|" size="small">
                      <span>作业面: {shift.workFace}</span>
                      <span>计划: {shift.workPlan?.planNo || '无'}</span>
                      <span>创建时间: {dayjs(shift.startTime).format('YYYY-MM-DD HH:mm')}</span>
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </Card>

      <Card title="我的领用申请" loading={loading}>
        {applications.length === 0 ? (
          <Empty description="暂无领用申请" />
        ) : (
          <List
            dataSource={applications}
            renderItem={(app) => (
              <List.Item key={app.id}>
                <List.Item.Meta
                  title={
                    <Space>
                      <Text strong>{app.applicationNo}</Text>
                      <Tag color={
                        app.status === 'NEED_REVIEW' ? 'orange' :
                        app.status === 'APPROVED' ? 'green' :
                        app.status === 'REJECTED' ? 'red' : 'blue'
                      }>
                        {app.status === 'NEED_REVIEW' ? '待复核' :
                         app.status === 'APPROVED' ? '已批准' :
                         app.status === 'REJECTED' ? '已拒绝' :
                         app.status === 'OUTBOUND_COMPLETED' ? '已出库' :
                         app.status === 'INBOUND_COMPLETED' ? '已回库' :
                         app.status === 'CLOSED' ? '已完成' : '待处理'}
                      </Tag>
                    </Space>
                  }
                  description={
                    <Space split="|" size="small">
                      <span>当班: {app.shift.shiftNo}</span>
                      <span>雷管: {app.detonatorQuantity}发</span>
                      <span>炸药: {app.explosiveQuantity}kg</span>
                      <span>创建时间: {dayjs(app.createdAt).format('YYYY-MM-DD HH:mm')}</span>
                      {app.reviewRemark && <Text type="warning">备注: {app.reviewRemark}</Text>}
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
