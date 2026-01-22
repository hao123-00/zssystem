import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, DatePicker, Space, message, Popconfirm, Tag, Descriptions, Card, Collapse } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, DownloadOutlined } from '@ant-design/icons';
import {
  getRecordList,
  deleteRecord,
  exportRecord,
  ProductionRecordInfo,
  ProductionRecordQueryParams,
} from '@/api/production';
import { exportExcel } from '@/utils/excel';
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

  const handleExport = async () => {
    try {
      const values = form.getFieldsValue();
      const params: ProductionRecordQueryParams = {
        ...values,
        startDate: values.startDate ? values.startDate.format('YYYY-MM-DD') : undefined,
        endDate: values.endDate ? values.endDate.format('YYYY-MM-DD') : undefined,
        productionDate: values.productionDate ? values.productionDate.format('YYYY-MM-DD') : undefined,
      };
      
      const date = new Date().toISOString().split('T')[0].replace(/-/g, '');
      const filename = `生产管理_生产记录_${date}`;
      await exportExcel('/api/production/record/export', params, filename);
    } catch (error: any) {
      message.error('导出失败：' + (error.message || '未知错误'));
    }
  };

  const columns = [
    {
      title: '记录编号',
      dataIndex: 'recordNo',
      key: 'recordNo',
      width: 150,
      fixed: 'left' as const,
    },
    {
      title: '组别',
      dataIndex: 'groupName',
      key: 'groupName',
      width: 100,
    },
    {
      title: '机台号',
      dataIndex: 'machineNo',
      key: 'machineNo',
      width: 120,
    },
    {
      title: '设备型号',
      dataIndex: 'equipmentModel',
      key: 'equipmentModel',
      width: 150,
    },
    {
      title: '产品名称',
      key: 'productName',
      width: 200,
      render: (_: any, record: ProductionRecordInfo) => {
        if (record.products && record.products.length > 0) {
          return record.products.map((p, idx) => (
            <div key={idx} style={{ marginBottom: 4 }}>
              <div>{p.productName}</div>
              <div style={{ fontSize: '12px', color: '#666' }}>
                数量: {p.orderQuantity} | 产能: {p.dailyCapacity}/天 | 剩余: {p.remainingQuantity ?? p.orderQuantity}
              </div>
            </div>
          ));
        }
        return record.productName || '-';
      },
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
        const total = record.quantity + (record.defectQuantity || 0);
        const rate = total > 0 
          ? ((record.quantity / total) * 100).toFixed(1) 
          : '0.0';
        return `${rate}%`;
      },
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

  // 展开行内容
  const expandedRowRender = (record: ProductionRecordInfo) => {
    return (
      <div style={{ padding: '16px', background: '#fafafa' }}>
        <Collapse defaultActiveKey={['equipment', 'product', 'schedule']}>
          <Collapse.Panel header="设备详细信息" key="equipment">
            <Descriptions column={3} bordered size="small">
              <Descriptions.Item label="组别">{record.groupName || '-'}</Descriptions.Item>
              <Descriptions.Item label="机台号">{record.machineNo || '-'}</Descriptions.Item>
              <Descriptions.Item label="设备编号">{record.equipmentNo || '-'}</Descriptions.Item>
              <Descriptions.Item label="设备名称">{record.equipmentName || '-'}</Descriptions.Item>
              <Descriptions.Item label="设备型号">{record.equipmentModel || '-'}</Descriptions.Item>
              <Descriptions.Item label="机械手型号">{record.robotModel || '-'}</Descriptions.Item>
              <Descriptions.Item label="启用日期">{record.enableDate || '-'}</Descriptions.Item>
              <Descriptions.Item label="使用年限">{record.serviceLife ? `${record.serviceLife}年` : '-'}</Descriptions.Item>
              <Descriptions.Item label="模温机">{record.moldTempMachine || '-'}</Descriptions.Item>
              <Descriptions.Item label="冻水机">{record.chiller || '-'}</Descriptions.Item>
              <Descriptions.Item label="基本排模">{record.basicMold || '-'}</Descriptions.Item>
              <Descriptions.Item label="备用排模1">{record.spareMold1 || '-'}</Descriptions.Item>
              <Descriptions.Item label="备用排模2">{record.spareMold2 || '-'}</Descriptions.Item>
              <Descriptions.Item label="备用排模3">{record.spareMold3 || '-'}</Descriptions.Item>
            </Descriptions>
          </Collapse.Panel>
          
          <Collapse.Panel header="产品信息" key="product">
            {record.products && record.products.length > 0 ? (
              <Table
                dataSource={record.products}
                columns={[
                  { title: '产品名称', dataIndex: 'productName', key: 'productName' },
                  { title: '产品编码', dataIndex: 'productCode', key: 'productCode' },
                  { title: '订单数量', dataIndex: 'orderQuantity', key: 'orderQuantity' },
                  { title: '产能', dataIndex: 'dailyCapacity', key: 'dailyCapacity' },
                  { 
                    title: '剩余数量', 
                    key: 'remainingQuantity',
                    render: (_: any, p: any) => p.remainingQuantity ?? p.orderQuantity
                  },
                ]}
                pagination={false}
                size="small"
                rowKey={(_, index) => `product-${index}`}
              />
            ) : (
              <div>暂无产品信息</div>
            )}
          </Collapse.Panel>
          
          <Collapse.Panel header="排程情况" key="schedule">
            {record.schedules && record.schedules.length > 0 ? (
              <Table
                dataSource={record.schedules}
                columns={[
                  { title: '排程日期', dataIndex: 'scheduleDate', key: 'scheduleDate', width: 120 },
                  { title: '第几天', dataIndex: 'dayNumber', key: 'dayNumber', width: 80 },
                  { title: '产品名称', dataIndex: 'productName', key: 'productName', width: 150 },
                  { title: '排产数量', dataIndex: 'productionQuantity', key: 'productionQuantity', width: 100 },
                  { title: '产能', dataIndex: 'dailyCapacity', key: 'dailyCapacity', width: 100 },
                  { title: '剩余数量', dataIndex: 'remainingQuantity', key: 'remainingQuantity', width: 100 },
                ]}
                pagination={false}
                size="small"
                rowKey={(_, index) => `schedule-${index}`}
                scroll={{ x: 650 }}
              />
            ) : (
              <div>暂无排程信息</div>
            )}
          </Collapse.Panel>
        </Collapse>
      </div>
    );
  };

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
        <Button type="primary" icon={<DownloadOutlined />} onClick={handleExport}>
          导出Excel
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
        expandable={{
          expandedRowRender,
          rowExpandable: () => true,
        }}
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
