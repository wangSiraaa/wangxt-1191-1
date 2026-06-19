import { useState, useEffect } from 'react';
import { Form, Select, Input, InputNumber, Button, Card, Typography, message, Space, Table, Tag, Divider } from 'antd';
import { ArrowLeftOutlined, ScanOutlined, PlusOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { applicationApi, outboundApi, explosiveApi } from '../../services/api';
import type { PickupApplication, Explosive, OutboundRecord } from '../../types';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;

interface OutboundItem {
  explosiveId: number;
  type: string;
  serialNo: string;
  quantity: number;
}

export default function Outbound() {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [scanning, setScanning] = useState(false);
  const [applications, setApplications] = useState<PickupApplication[]>([]);
  const [selectedApp, setSelectedApp] = useState<PickupApplication | null>(null);
  const [explosives, setExplosives] = useState<Explosive[]>([]);
  const [outboundItems, setOutboundItems] = useState<OutboundItem[]>([]);
  const [scannedRecords, setScannedRecords] = useState<OutboundRecord[]>([]);

  useEffect(() => {
    loadApprovedApplications();
    loadExplosives();
  }, []);

  const loadApprovedApplications = async () => {
    try {
      const res = await applicationApi.list({ status: 'APPROVED' });
      if (res.success) setApplications(res.data);
    } catch (error) {
      message.error('加载申请单失败');
    }
  };

  const loadExplosives = async () => {
    try {
      const res = await explosiveApi.list();
      if (res.success) setExplosives(res.data);
    } catch (error) {
      message.error('加载库存失败');
    }
  };

  const handleAppChange = (appId: number) => {
    const app = applications.find(a => a.id === appId);
    setSelectedApp(app || null);
    setOutboundItems([]);
    form.resetFields(['explosiveId', 'quantity']);
  };

  const handleAddItem = () => {
    form.validateFields(['explosiveId', 'quantity']).then(values => {
      const explosive = explosives.find(e => e.id === values.explosiveId);
      if (!explosive) return;

      const existingItem = outboundItems.find(item => item.explosiveId === values.explosiveId);
      if (existingItem) {
        existingItem.quantity += values.quantity;
        setOutboundItems([...outboundItems]);
      } else {
        setOutboundItems([...outboundItems, {
          explosiveId: values.explosiveId,
          type: explosive.type,
          serialNo: explosive.serialNo,
          quantity: values.quantity,
        }]);
      }
      form.resetFields(['explosiveId', 'quantity']);
      message.success('已添加');
    });
  };

  const handleRemoveItem = (index: number) => {
    const newItems = [...outboundItems];
    newItems.splice(index, 1);
    setOutboundItems(newItems);
  };

  const handleSimulateScan = () => {
    setScanning(true);
    setTimeout(() => {
      const available = explosives.filter(e => e.availableQuantity > 0);
      if (available.length > 0 && selectedApp) {
        const random = available[Math.floor(Math.random() * available.length)];
        const maxQty = random.type === 'DETONATOR' 
          ? Math.min(random.availableQuantity, selectedApp.detonatorQuantity - getTotalByType('DETONATOR'))
          : Math.min(random.availableQuantity, selectedApp.explosiveQuantity - getTotalByType('EXPLOSIVE'));
        if (maxQty > 0) {
          const qty = random.type === 'DETONATOR' ? Math.min(5, maxQty) : Math.min(10, maxQty);
          form.setFieldsValue({
            explosiveId: random.id,
            quantity: qty,
          });
          handleAddItem();
        }
      }
      setScanning(false);
    }, 800);
  };

  const getTotalByType = (type: string) => {
    return outboundItems.filter(i => i.type === type).reduce((sum, i) => sum + i.quantity, 0);
  };

  const handleSubmit = async () => {
    if (!selectedApp || outboundItems.length === 0) {
      message.error('请选择申请单并添加出库器材');
      return;
    }

    const detonatorTotal = getTotalByType('DETONATOR');
    const explosiveTotal = getTotalByType('EXPLOSIVE');

    if (detonatorTotal !== selectedApp.detonatorQuantity) {
      message.error(`雷管数量不匹配，应出${selectedApp.detonatorQuantity}发，实际${detonatorTotal}发`);
      return;
    }
    if (explosiveTotal !== selectedApp.explosiveQuantity) {
      message.error(`炸药数量不匹配，应出${selectedApp.explosiveQuantity}kg，实际${explosiveTotal}kg`);
      return;
    }

    setLoading(true);
    try {
      const values = await form.validateFields();
      const requestData = {
        applicationId: selectedApp.id,
        workFace: selectedApp.shift.workFace,
        items: outboundItems.map(item => ({
          explosiveSerialNo: item.serialNo,
          type: item.type,
          quantity: item.quantity,
        })),
        remarks: values.remarks,
      };

      const res = await outboundApi.create(requestData);
      if (res.success) {
        message.success('出库完成');
        setScannedRecords(res.data);
        setOutboundItems([]);
        setSelectedApp(null);
        form.resetFields();
        loadApprovedApplications();
        loadExplosives();
      } else {
        message.error(res.message);
      }
    } catch (error: any) {
      message.error(error.response?.data?.message || '出库失败');
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
    { title: '数量', dataIndex: 'quantity', render: (q: number, record: OutboundItem) => `${q}${record.type === 'DETONATOR' ? '发' : 'kg'}` },
    {
      title: '操作',
      render: (_: any, __: any, index: number) => (
        <Button type="link" danger onClick={() => handleRemoveItem(index)}>移除</Button>
      ),
    },
  ];

  const recordColumns = [
    { title: '出库单号', dataIndex: 'outboundNo' },
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
    { title: '数量', dataIndex: 'quantity', render: (q: number, record: OutboundRecord) => `${q}${record.type === 'DETONATOR' ? '发' : 'kg'}` },
    { title: '出库时间', dataIndex: 'outboundTime', render: (t: string) => dayjs(t).format('HH:mm:ss') },
  ];

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '24px' }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/storekeeper')} style={{ marginRight: '16px' }}>
          返回
        </Button>
        <Title level={3} style={{ margin: 0 }}>扫码出库</Title>
      </div>

      <Card style={{ marginBottom: '24px' }}>
        <Form form={form} layout="vertical" size="large">
          <Form.Item
            name="applicationId"
            label="领用申请单"
            rules={[{ required: true, message: '请选择申请单' }]}
          >
            <Select
              placeholder="请选择待出库的申请单"
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
            <div style={{ padding: '16px', background: '#f0f5ff', borderRadius: '8px', marginBottom: '24px' }}>
              <Space direction="vertical" size="small" style={{ width: '100%' }}>
                <Space split="|" size="small">
                  <span><Text strong>申请单:</Text> {selectedApp.applicationNo}</span>
                  <span><Text strong>爆破员:</Text> {selectedApp.blaster.name}</span>
                  <span><Text strong>当班:</Text> {selectedApp.shift.shiftNo}</span>
                  <span><Text strong>作业面:</Text> {selectedApp.shift.workFace}</span>
                </Space>
                <Space split="|" size="small">
                  <span><Text strong>申请雷管:</Text> {selectedApp.detonatorQuantity}发</span>
                  <span><Text strong>已扫描雷管:</Text> <Text type={getTotalByType('DETONATOR') === selectedApp.detonatorQuantity ? 'success' : 'warning'}>{getTotalByType('DETONATOR')}发</Text></span>
                </Space>
                <Space split="|" size="small">
                  <span><Text strong>申请炸药:</Text> {selectedApp.explosiveQuantity}kg</span>
                  <span><Text strong>已扫描炸药:</Text> <Text type={getTotalByType('EXPLOSIVE') === selectedApp.explosiveQuantity ? 'success' : 'warning'}>{getTotalByType('EXPLOSIVE')}kg</Text></span>
                </Space>
              </Space>
            </div>
          )}

          <Divider>扫描器材</Divider>

          <Space.Compact style={{ width: '100%', marginBottom: '16px' }}>
            <Form.Item name="explosiveId" noStyle rules={[{ required: true, message: '请选择器材' }]}>
              <Select placeholder="选择或扫描器材编号" style={{ width: '60%' }}>
                {explosives.filter(e => e.availableQuantity > 0).map(exp => (
                  <Option key={exp.id} value={exp.id}>
                    [{exp.type === 'DETONATOR' ? '雷管' : '炸药'}] {exp.serialNo} - 库存{exp.availableQuantity}
                  </Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item name="quantity" noStyle rules={[{ required: true, message: '请输入数量' }]}>
              <InputNumber min={1} placeholder="数量" style={{ width: '20%' }} />
            </Form.Item>
            <Button icon={<PlusOutlined />} onClick={handleAddItem}>添加</Button>
            <Button type="primary" icon={<ScanOutlined />} loading={scanning} onClick={handleSimulateScan}>
              模拟扫码
            </Button>
          </Space.Compact>

          {outboundItems.length > 0 && (
            <>
              <Table
                dataSource={outboundItems}
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
                    确认出库
                  </Button>
                  <Button onClick={() => { setOutboundItems([]); form.resetFields(['remarks']); }}>
                    清空
                  </Button>
                </Space>
              </Form.Item>
            </>
          )}
        </Form>
      </Card>

      {scannedRecords.length > 0 && (
        <Card title="本次出库记录">
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
