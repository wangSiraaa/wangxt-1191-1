import { useState, useEffect } from 'react';
import { Button, Card, Typography, message, Space, Table, Tag, Modal, Input } from 'antd';
import { ArrowLeftOutlined, CheckOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { anomalyApi } from '../../services/api';
import type { AnomalyRecord } from '../../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { TextArea } = Input;

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

export default function AnomalyManagement() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [anomalies, setAnomalies] = useState<AnomalyRecord[]>([]);
  const [resolveModalVisible, setResolveModalVisible] = useState(false);
  const [selectedAnomaly, setSelectedAnomaly] = useState<AnomalyRecord | null>(null);
  const [handlingResult, setHandlingResult] = useState('');

  useEffect(() => {
    loadAnomalies();
  }, []);

  const loadAnomalies = async () => {
    try {
      const res = await anomalyApi.list();
      if (res.success) setAnomalies(res.data);
    } catch (error) {
      message.error('加载异常记录失败');
    }
  };

  const handleResolve = (anomaly: AnomalyRecord) => {
    setSelectedAnomaly(anomaly);
    setHandlingResult('');
    setResolveModalVisible(true);
  };

  const handleSubmitResolve = async () => {
    if (!selectedAnomaly || !handlingResult.trim()) {
      message.error('请填写处理结果');
      return;
    }

    try {
      setLoading(true);
      const res = await anomalyApi.resolve(selectedAnomaly.id, handlingResult);
      if (res.success) {
        message.success('异常已处理');
        setResolveModalVisible(false);
        loadAnomalies();
      } else {
        message.error(res.message);
      }
    } catch (error: any) {
      message.error(error.response?.data?.message || '处理失败');
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    {
      title: '异常编号',
      dataIndex: 'recordNo',
      render: (text: string) => <Text strong>{text}</Text>,
    },
    {
      title: '异常类型',
      dataIndex: 'type',
      render: (type: string) => (
        <Tag color={anomalyTypeColors[type]}>
          {anomalyTypeNames[type]}
        </Tag>
      ),
    },
    { title: '异常描述', dataIndex: 'description' },
    { title: '器材编号', dataIndex: 'explosiveSerialNo' },
    { title: '涉及数量', dataIndex: 'anomalyQuantity' },
    { title: '上报人', dataIndex: ['reportedBy', 'name'] },
    { title: '上报时间', dataIndex: 'reportedAt', render: (t: string) => dayjs(t).format('YYYY-MM-DD HH:mm') },
    {
      title: '状态',
      dataIndex: 'resolved',
      render: (resolved: boolean) => (
        resolved ? <Tag color="green">已处理</Tag> : <Tag color="red">待处理</Tag>
      ),
    },
    { title: '处理人', dataIndex: ['handledBy', 'name'] },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: AnomalyRecord) => (
        !record.resolved && (
          <Button
            type="primary"
            size="small"
            icon={<CheckOutlined />}
            onClick={() => handleResolve(record)}
          >
            处理
          </Button>
        )
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '24px' }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/safety')} style={{ marginRight: '16px' }}>
          返回
        </Button>
        <Title level={3} style={{ margin: 0 }}>异常管理</Title>
      </div>

      <Card
        title={
          <Space>
            <ExclamationCircleOutlined style={{ color: '#ff4d4f' }} />
            <span>异常记录列表</span>
            <Tag color="red">
              待处理: {anomalies.filter(a => !a.resolved).length}
            </Tag>
          </Space>
        }
      >
        <Table
          dataSource={anomalies}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 10 }}
          expandable={{
            expandedRowRender: (record) => (
              <Space direction="vertical" size="small" style={{ width: '100%' }}>
                {record.handlingResult && (
                  <div>
                    <Text strong>处理结果：</Text>
                    {record.handlingResult}
                  </div>
                )}
                {record.handledAt && (
                  <div>
                    <Text strong>处理时间：</Text>
                    {dayjs(record.handledAt).format('YYYY-MM-DD HH:mm:ss')}
                  </div>
                )}
              </Space>
            ),
          }}
        />
      </Card>

      <Modal
        title="处理异常"
        open={resolveModalVisible}
        onCancel={() => setResolveModalVisible(false)}
        footer={[
          <Button key="cancel" onClick={() => setResolveModalVisible(false)}>取消</Button>,
          <Button
            key="submit"
            type="primary"
            loading={loading}
            onClick={handleSubmitResolve}
          >
            确认处理
          </Button>,
        ]}
      >
        {selectedAnomaly && (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <div>
              <Text strong>异常编号：</Text>
              {selectedAnomaly.recordNo}
            </div>
            <div>
              <Text strong>异常类型：</Text>
              <Tag color={anomalyTypeColors[selectedAnomaly.type]}>
                {anomalyTypeNames[selectedAnomaly.type]}
              </Tag>
            </div>
            <div>
              <Text strong>异常描述：</Text>
              {selectedAnomaly.description}
            </div>
            <div>
              <Text strong>上报人：</Text>
              {selectedAnomaly.reportedBy.name}
            </div>
            <div>
              <Text strong type="danger">处理结果：</Text>
              <TextArea
                rows={4}
                value={handlingResult}
                onChange={(e) => setHandlingResult(e.target.value)}
                placeholder="请详细描述处理措施和结果"
              />
            </div>
          </Space>
        )}
      </Modal>
    </div>
  );
}
