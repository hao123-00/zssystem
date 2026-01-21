import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, DatePicker, Space, message, Popconfirm, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  getRecordList,
  deleteRecord,
  ProductionRecordInfo,
  ProductionRecordQueryParams,
} from '@/api/production';
import RecordModal from './RecordModal';
import './ProductionList.less';
import dayjs from 'dayjs';

const RecordList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<ProductionRecordInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<ProductionRecordInfo | null>(null);

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: ProductionRecordQueryParams = {
        ...values,
        startDate: values.startDate ? values.startDate.format('YYYY-MM-DD') : undefined,
        endDate: values.endDate ? values.endDate.format('YYYY-MM-DD') : undefined,
        productionDate: values.productionDate ? values.productionDate.format('YYYY-MM-DD') : undefined,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getRecordList(params);
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
    setEditingRecord(null);
    setModalVisible(true);
  };

  const handleEdit = (record: ProductionRecordInfo) => {
    setEditingRecord(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteRecord(id);
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

  const columns = [
    {
      title: '记录编号',
      dataIndex: 'recordNo',
      key: 'recordNo',
      width: 150,
    },
    {
      title: '订单编号',
      dataIndex: 'orderNo',
      key: 'orderNo',
      width: 150,
    },
    {
      title: '产品名称',
      dataIndex: 'productName',
      key: 'productName',
      width: 150,
    },
    {
      title: '计划编号',
      dataIndex: 'planNo',
      key: 'planNo',
      width: 150,
    },
    {
      title: '生产日期',
      dataIndex: 'productionDate',
      key: 'productionDate',
      width: 120,
    },
    {
      title: '产量',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 100,
    },
    {
      title: '不良品数量',
      dataIndex: 'defectQuantity',
      key: 'defectQuantity',
      width: 120,
    },
    {
      title: '合格率',
      key: 'passRate',
      width: 100,
      render: (_: any, record: ProductionRecordInfo) => {
        const total = record.quantity + record.defectQuantity;
        const rate = total > 0 
          ? ((record.quantity / total) * 100).toFixed(1) 
          : '0.0';
        return `${rate}%`;
      },
    },
    {
      title: '操作员',
      dataIndex: 'operatorName',
      key: 'operatorName',
      width: 100,
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      width: 180,
      render: (text: string) => (text ? new Date(text).toLocaleString() : '-'),
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      key: 'endTime',
      width: 180,
      render: (text: string) => (text ? new Date(text).toLocaleString() : '-'),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_: any, record: ProductionRecordInfo) => (
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
          <Form.Item name="recordNo">
            <Input placeholder="记录编号" allowClear />
          </Form.Item>
          <Form.Item name="orderId">
            <Input placeholder="订单ID" allowClear />
          </Form.Item>
          <Form.Item name="productionDate">
            <DatePicker placeholder="生产日期" format="YYYY-MM-DD" />
          </Form.Item>
          <Form.Item name="startDate">
            <DatePicker placeholder="开始日期" format="YYYY-MM-DD" />
          </Form.Item>
          <Form.Item name="endDate">
            <DatePicker placeholder="结束日期" format="YYYY-MM-DD" />
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
          新增记录
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
        scroll={{ x: 1600 }}
        pagination={{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showTotal: (total) => `共 ${total} 条`,
          onChange: handleTableChange,
        }}
      />

      <RecordModal
        visible={modalVisible}
        record={editingRecord}
        onCancel={() => setModalVisible(false)}
        onSuccess={() => {
          setModalVisible(false);
          fetchList();
        }}
      />
    </div>
  );
};

export default RecordList;
