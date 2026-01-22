import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Select, Space, message, Popconfirm, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  getOrderList,
  deleteOrder,
  ProductionOrderInfo,
  ProductionOrderQueryParams,
} from '@/api/production';
import OrderModal from './OrderModal';
import './ProductionList.less';

const OrderList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<ProductionOrderInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingOrder, setEditingOrder] = useState<ProductionOrderInfo | null>(null);

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: ProductionOrderQueryParams = {
        ...values,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getOrderList(params);
      setTableData(response.list || []);
      setPagination({ ...pagination, total: response.total || 0 });
    } catch (error: any) {
      message.error(error.message || '查询失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList();
  }, [pagination.current, pagination.pageSize]);

  const handleAdd = () => {
    setEditingOrder(null);
    setModalVisible(true);
  };

  const handleEdit = (record: ProductionOrderInfo) => {
    setEditingOrder(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteOrder(id);
      message.success('删除成功');
      fetchList();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  const handleSearch = () => {
    setPagination({ ...pagination, current: 1 });
    fetchList();
  };

  const handleReset = () => {
    form.resetFields();
    setPagination({ ...pagination, current: 1 });
    fetchList();
  };

  const handleTableChange = (page: number, pageSize: number) => {
    setPagination({ ...pagination, current: page, pageSize });
  };

  const getStatusTag = (status: number) => {
    const statusMap: Record<number, { color: string; text: string }> = {
      0: { color: 'default', text: '待排程' },
      1: { color: 'processing', text: '排程中' },
      2: { color: 'success', text: '已完成' },
    };
    const statusInfo = statusMap[status] || { color: 'default', text: '未知' };
    return <Tag color={statusInfo.color}>{statusInfo.text}</Tag>;
  };

  const columns = [
    {
      title: '订单编号',
      dataIndex: 'orderNo',
      key: 'orderNo',
      width: 150,
    },
    {
      title: '机台号',
      dataIndex: 'machineNo',
      key: 'machineNo',
      width: 120,
    },
    {
      title: '产品信息',
      key: 'products',
      width: 300,
      render: (_: any, record: ProductionOrderInfo) => {
        if (!record.products || record.products.length === 0) {
          return '-';
        }
        return (
          <div>
            {record.products.map((p, index) => (
              <div key={index} style={{ marginBottom: 4 }}>
                <div>
                  <strong>{p.productName}</strong>
                  {p.productCode && <span style={{ color: '#999', marginLeft: 8 }}>({p.productCode})</span>}
                </div>
                <div style={{ fontSize: '12px', color: '#666' }}>
                  数量: {p.orderQuantity} | 产能: {p.dailyCapacity}/天
                </div>
              </div>
            ))}
          </div>
        );
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number) => getStatusTag(status),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
      render: (text: string) => (text ? new Date(text).toLocaleString() : '-'),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_: any, record: ProductionOrderInfo) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm title="确定要删除吗？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="production-list-container">
      <div className="search-form">
        <Form form={form} layout="inline">
          <Form.Item name="orderNo">
            <Input placeholder="订单编号" allowClear />
          </Form.Item>
          <Form.Item name="machineNo">
            <Input placeholder="机台号" allowClear />
          </Form.Item>
          <Form.Item name="productName">
            <Input placeholder="产品名称" allowClear />
          </Form.Item>
          <Form.Item name="status">
            <Select placeholder="状态" allowClear style={{ width: 120 }}>
              <Select.Option value={0}>待排程</Select.Option>
              <Select.Option value={1}>排程中</Select.Option>
              <Select.Option value={2}>已完成</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" onClick={handleSearch}>
                查询
              </Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Form.Item>
        </Form>
      </div>

      <div className="toolbar">
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增订单
        </Button>
        <Button icon={<ReloadOutlined />} onClick={fetchList}>
          刷新
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={tableData}
        rowKey="id"
        loading={loading}
        scroll={{ x: 1200 }}
        pagination={{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showTotal: (total) => `共 ${total} 条`,
          onChange: handleTableChange,
        }}
      />

      <OrderModal
        visible={modalVisible}
        order={editingOrder}
        onCancel={() => setModalVisible(false)}
        onSuccess={() => {
          setModalVisible(false);
          fetchList();
        }}
      />
    </div>
  );
};

export default OrderList;
