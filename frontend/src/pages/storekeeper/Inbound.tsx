import { useState, useEffect } from 'react';
import { Form, Select, Input, InputNumber, Button, Card, Typography, message, Space, Table, Tag, Divider } from 'antd';
import { ArrowLeftOutlined, ScanOutlined, PlusOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { applicationApi, inboundApi } from '../../services/api';
import type { PickupApplication, InboundRecord } from '../../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;

interface InboundItem {
  serialNo: string;
  type: string;
  usedQuantity: number;
  returnedQuantity: number;
  expectedQuantity: number;
}

export default function Inbound() {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [scanning, setScanning] = useState(false);
  const [applications, setApplications] = useState<PickupApplication[]>([]);
  const [selectedApp, setSelectedApp] = useState<PickupApplication | null>(null);
  const [inboundItems, setInboundItems] = useState<InboundItem[]>([]);
  const [scannedRecords, setScannedRecords] = useState<InboundRecord[]>([]);

  useEffect(() => {
    loadApplications();
  }, []);

  const loadApplications = async () => {
    try {
      const res = await applicationApi.list({ status: 'OUTBOUND_COMPLETED' });
      if (res.success) setApplications(res.data);
    } catch (error) {
      message.error('加载申请单失败');
    }
  };

  const handleAppChange = (appId: number) => {
    const app = applications.find(a => a.id === appId);
    setSelectedApp(app || null);
    setInboundItems([]);
    form.resetFields(['serialNo', 'usedQuantity', 'returnedQuantity']);

    if (app) {
      const detonatorItem: InboundItem = {
        serialNo: `DTN000001-DTN0000${app.detonatorQuantity}`,
        type: 'DETONATOR',
        usedQuantity: 0,
        returnedQuantity: 0,
        expectedQuantity: app.detonatorQuantity,
      };
      const explosiveItem: InboundItem = {
        serialNo: `EXP000001-EXP0000${Math.min(app.explosiveQuantity, 15)}`,
        type: 'EXPLOSIVE',
        usedQuantity: 0,
        returnedQuantity: 0,
        expectedQuantity: app.explosiveQuantity,
      };
      setInboundItems([detonatorItem, explosiveItem]);
    }
  };

  const handleAddItem = () => {
    form.validateFields(['serialNo', 'usedQuantity', 'returnedQuantity']).then(values => {
      const type = values.serialNo.startsWith('DTN') ? 'DETONATOR' : 'EXPLOSIVE';
      const existingItem = inboundItems.find(item => item.serialNo === values.serialNo);
      if (existingItem) {
        existingItem.usedQuantity += values.usedQuantity;
        existingItem.returnedQuantity += values.returnedQuantity;
        setInboundItems([...inboundItems]);
      } else {
        setInboundItems([...inboundItems, {
          serialNo: values.serialNo,
          type,
          usedQuantity: values.usedQuantity,
          returnedQuantity: values.returnedQuantity,
          expectedQuantity: values.usedQuantity + values.returnedQuantity,
        }]);
      }
      form.resetFields(['serialNo', 'usedQuantity', 'returnedQuantity']);
      message.success('已添加');
    });
  };

  const handleSimulateScan = () => {
    if (!selectedApp) return;
    setScanning(true);
    setTimeout(() => {
      const detonatorItem = inboundItems.find(i => i.type === 'DETONATOR');
      const explosiveItem = inboundItems.find(i => i.type === 'EXPLOSIVE');

      if (detonatorItem) {
        detonatorItem.usedQuantity = Math.floor(selectedApp.detonatorQuantity * 0.9);
        detonatorItem.returnedQuantity = selectedApp.detonatorQuantity - detonatorItem.usedQuantity;
      }
      if (explosiveItem) {
        explosiveItem.usedQuantity = Math.floor(selectedApp.explosiveQuantity * 0.85);
        explosiveItem.returnedQuantity = selectedApp.explosiveQuantity - explosiveItem.usedQuantity;
      }

      setInboundItems([...inboundItems]);
      setScanning(false);
      message.success('模拟扫码完成，已自动填充使用/退回数量');
    }, 800);
  };

  const handleQuantityChange = (index: number, field: 'usedQuantity' | 'returnedQuantity', value: number) => {
    const newItems = [...inboundItems];
    newItems[index][field] = value || 0;
    setInboundItems(newItems);
  };

  const handleRemoveItem = (index: number) => {
    const newItems = [...inboundItems];
    newItems.splice(index, 1);
    setInboundItems(newItems);
  };

  const getTotalByType = (type: string, field: 'usedQuantity' | 'returnedQuantity') => {
    return inboundItems.filter(i => i.type === type).reduce((sum, i) => sum + i[field], 0);
  };

  const handleSubmit = async () => {
    if (!selectedApp || inboundItems.length === 0) {
      message.error('请选择申请单');
      return;
    }

    for (const item of inboundItems) {
      if (item.usedQuantity + item.returnedQuantity !== item.expectedQuantity) {
        message.error(`${item.type === 'DETONATOR' ? '雷管' : '炸药'}数量不匹配，使用+退回应等于出库数量`);
        return;
      }
    }

    setLoading(true);
    try {
      const values = await form.validateFields();
      const requestData = {
        applicationId: selectedApp.id,
        items: inboundItems.map(item => ({
          explosiveSerialNo: item.serialNo,
          type: item.type,
          usedQuantity: item.usedQuantity,
          returnedQuantity: item.returnedQuantity,
        })),
        remarks: values.remarks,
      };

      const res = await inboundApi.create(requestData);
      if (res.success) {
        message.success('回库完成');
        setScannedRecords(res.data);
        setInboundItems([]);
        setSelectedApp(null);
        form.resetFields();
        loadApplications();
      } else {
        message.error(res.message);
      }
    } catch (error: any) {
      message.error(error.response?.data?.message || '回库失败');
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    {
      title: '器材类型',
      dataIndex: 'type',
      render: (type: string) => (
        <Tag color={type === 'DETONATOR' ? 'orange' : 'red'}>
          {type === 'DETONATOR' ? '雷管' : '炸药'}
        </Tag>
      ),
    },
    { title: '器材编号', dataIndex: 'serialNo' },
    { title: '应退数量', dataIndex: 'expectedQuantity', render: (q: number, record: InboundItem) => `${q}${record.type === 'DETONATOR' ? '发' : 'kg'}` },
    {
      title: '使用数量',
      dataIndex: 'usedQuantity',
      render: (q: number, record: InboundItem, index: number) => (
        <InputNumber
          min={0}
          value={q}
          onChange={(v) => handleQuantityChange(index, 'usedQuantity', v as number)}
        />
      ),
    },
    {
      title: '退回数量',
      dataIndex: 'returnedQuantity',
      render: (q: number, record: InboundItem, index: number) => (
        <InputNumber
          min={0}
          value={q}
          onChange={(v) => handleQuantityChange(index, 'returnedQuantity', v as number)}
        />
      ),
    },
    {
      title: '状态',
      render: (_: any, record: InboundItem) => {
        const total = record.usedQuantity + record.returnedQuantity;
        return total === record.expectedQuantity ? (
          <Tag color="green">已核对</Tag>
        ) : (
          <Tag color="red">数量不符</Tag>
        );
      },
    },
    {
      title: '操作',
      render: (_: any, __: any, index: number) => (
        <Button type="link" danger onClick={() => handleRemoveItem(index)}>移除</Button>
      ),
    },
  ];

  const recordColumns = [
    { title: '回库单号', dataIndex: 'inboundNo' },
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

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '24px' }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/storekeeper')} style={{ marginRight: '16px' }}>
          返回
        </Button>
        <Title level={3} style={{ margin: 0 }}>扫码回库</Title>
      </div>

      <Card style={{ marginBottom: '24px' }}>
        <Form form={form} layout="vertical" size="large">
          <Form.Item
            name="applicationId"
            label="领用申请单"
            rules={[{ required: true, message: '请选择申请单' }]}
          >
            <Select
              placeholder="请选择待回库的申请单"
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

          {selectedApp && (
            <div style={{ padding: '16px', background: '#f6ffed', borderRadius: '8px', marginBottom: '24px' }}>
              <Space direction="vertical" size="small" style={{ width: '100%' }}>
                <Space split="|" size="small">
                  <span><Text strong>申请单:</Text> {selectedApp.applicationNo}</span>
                  <span><Text strong>爆破员:</Text> {selectedApp.blaster.name}</span>
                  <span><Text strong>当班:</Text> {selectedApp.shift.shiftNo}</span>
                </Space>
                <Space split="|" size="small">
                  <span><Text strong>出库雷管:</Text> {selectedApp.detonatorQuantity}发</span>
                  <span><Text strong>已使用:</Text> {getTotalByType('DETONATOR', 'usedQuantity')}发</span>
                  <span><Text strong>已退回:</Text> {getTotalByType('DETONATOR', 'returnedQuantity')}发</span>
                </Space>
                <Space split="|" size="small">
                  <span><Text strong>出库炸药:</Text> {selectedApp.explosiveQuantity}kg</span>
                  <span><Text strong>已使用:</Text> {getTotalByType('EXPLOSIVE', 'usedQuantity')}kg</span>
                  <span><Text strong>已退回:</Text> {getTotalByType('EXPLOSIVE', 'returnedQuantity')}kg</span>
                </Space>
              </Space>
            </div>
          )}

          <Divider>扫描退回器材</Divider>

          <Button type="primary" icon={<ScanOutlined />} loading={scanning} onClick={handleSimulateScan} disabled={!selectedApp} style={{ marginBottom: '16px' }}>
            模拟扫码并自动填充
          </Button>

          {inboundItems.length > 0 && (
            <>
              <Table
                dataSource={inboundItems}
                columns={columns}
                rowKey="serialNo"
                pagination={false}
                size="small"
                style={{ marginBottom: '16px' }}
              />
              <Form.Item name="remarks" label="备注">
                <TextArea rows={2} placeholder="请输入备注信息（可选）" />
              </Form.Item>
              <Form.Item>
                <Space>
                  <Button type="primary" onClick={handleSubmit} loading={loading} disabled={!selectedApp}>
                    确认回库
                  </Button>
                  <Button onClick={() => { setInboundItems([]); form.resetFields(['remarks']); }}>
                    清空
                  </Button>
                </Space>
              </Form.Item>
            </>
          )}
        </Form>
      </Card>

      {scannedRecords.length > 0 && (
        <Card title="本次回库记录">
          <Table
            dataSource={scannedRecords}
            columns={recordColumns}
            rowKey="id"
            pagination={false}
          />
        </Card>
      )}
    </div>
  );
}
