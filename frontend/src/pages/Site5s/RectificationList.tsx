import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Select, Space, message, Popconfirm, Tag, DatePicker } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  getSite5sRectificationList,
  deleteSite5sRectification,
  Site5sRectificationInfo,
  Site5sRectificationQueryParams,
} from '@/api/site5s';
import RectificationModal from './RectificationModal';
import dayjs from 'dayjs';
import './RectificationList.less';

const RectificationList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<Site5sRectificationInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRectification, setEditingRectification] = useState<Site5sRectificationInfo | null>(null);

  useEffect(() => {
    fetchList();
  }, [pagination.current, pagination.pageSize]);

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: Site5sRectificationQueryParams = {
        ...values,
        deadline: values.deadline ? values.deadline.format('YYYY-MM-DD') : undefined,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getSite5sRectificationList(params);
      setTableData(response.list || []);
      setPagination({ ...pagination, total: response.total || 0 });
    } catch (error: any) {
      message.error(error.message || '查询失败');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingRectification(null);
    setModalVisible(true);
  };

  const handleEdit = (rectification: Site5sRectificationInfo) => {
    setEditingRectification(rectification);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteSite5sRectification(id);
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

  const handleModalSuccess = () => {
    setModalVisible(false);
    fetchList();
  };

  const getStatusTag = (status: number | undefined) => {
    const statusMap: Record<number, { color: string; text: string }> = {
      0: { color: 'default', text: '待整改' },
      1: { color: 'processing', text: '整改中' },
      2: { color: 'warning', text: '待验证' },
      3: { color: 'success', text: '已完成' },
    };
    if (status === undefined || status === null) return <Tag>-</Tag>;
    const statusInfo = statusMap[status] || { color: 'default', text: '未知' };
    return <Tag color={statusInfo.color}>{statusInfo.text}</Tag>;
  };

  const columns = [
    {
      title: '任务编号',
      dataIndex: 'taskNo',
      key: 'taskNo',
      width: 150,
    },
    {
      title: '检查单号',
      dataIndex: 'checkNo',
      key: 'checkNo',
      width: 150,
    },
    {
      title: '区域',
      dataIndex: 'area',
      key: 'area',
      width: 120,
    },
    {
      title: '问题描述',
      dataIndex: 'problemDescription',
      key: 'problemDescription',
      ellipsis: true,
    },
    {
      title: '责任部门',
      dataIndex: 'department',
      key: 'department',
      width: 120,
    },
    {
      title: '责任人',
      dataIndex: 'responsiblePerson',
      key: 'responsiblePerson',
      width: 100,
    },
    {
      title: '整改期限',
      dataIndex: 'deadline',
      key: 'deadline',
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number) => getStatusTag(status),
    },
    {
      title: '整改日期',
      dataIndex: 'rectificationDate',
      key: 'rectificationDate',
      width: 120,
    },
    {
      title: '验证人员',
      dataIndex: 'verifierName',
      key: 'verifierName',
      width: 100,
    },
    {
      title: '验证日期',
      dataIndex: 'verificationDate',
      key: 'verificationDate',
      width: 120,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_: any, record: Site5sRectificationInfo) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这条记录吗？"
            onConfirm={() => handleDelete(record.id!)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="site5s-rectification-list">
      <Form form={form} layout="inline" className="search-form">
        <Form.Item name="taskNo" label="任务编号">
          <Input placeholder="请输入任务编号" allowClear />
        </Form.Item>
        <Form.Item name="area" label="区域">
          <Input placeholder="请输入区域" allowClear />
        </Form.Item>
        <Form.Item name="department" label="责任部门">
          <Input placeholder="请输入责任部门" allowClear />
        </Form.Item>
        <Form.Item name="responsiblePerson" label="责任人">
          <Input placeholder="请输入责任人" allowClear />
        </Form.Item>
        <Form.Item name="status" label="状态">
          <Select placeholder="请选择状态" allowClear style={{ width: 120 }}>
            <Select.Option value={0}>待整改</Select.Option>
            <Select.Option value={1}>整改中</Select.Option>
            <Select.Option value={2}>待验证</Select.Option>
            <Select.Option value={3}>已完成</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="deadline" label="整改期限">
          <DatePicker format="YYYY-MM-DD" placeholder="请选择整改期限" />
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

      <div className="table-toolbar">
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={handleAdd}
        >
          新增整改任务
        </Button>
        <Button
          icon={<ReloadOutlined />}
          onClick={fetchList}
        >
          刷新
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={tableData}
        rowKey="id"
        loading={loading}
        pagination={{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`,
          onChange: handleTableChange,
          onShowSizeChange: handleTableChange,
        }}
        scroll={{ x: 1500 }}
      />

      <RectificationModal
        visible={modalVisible}
        editingRectification={editingRectification}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleModalSuccess}
      />
    </div>
  );
};

export default RectificationList;
