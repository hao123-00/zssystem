import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Select, Space, message, Popconfirm, Tag, DatePicker } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  getSite5sCheckList,
  deleteSite5sCheck,
  Site5sCheckInfo,
  Site5sCheckQueryParams,
} from '@/api/site5s';
import CheckModal from './CheckModal';
import dayjs from 'dayjs';
import './CheckList.less';

const CheckList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<Site5sCheckInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingCheck, setEditingCheck] = useState<Site5sCheckInfo | null>(null);

  useEffect(() => {
    fetchList();
  }, [pagination.current, pagination.pageSize]);

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: Site5sCheckQueryParams = {
        ...values,
        checkDate: values.checkDate ? values.checkDate.format('YYYY-MM-DD') : undefined,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getSite5sCheckList(params);
      setTableData(response.list || []);
      setPagination({ ...pagination, total: response.total || 0 });
    } catch (error: any) {
      message.error(error.message || '查询失败');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingCheck(null);
    setModalVisible(true);
  };

  const handleEdit = (check: Site5sCheckInfo) => {
    setEditingCheck(check);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteSite5sCheck(id);
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

  const columns = [
    {
      title: '检查单号',
      dataIndex: 'checkNo',
      key: 'checkNo',
      width: 150,
    },
    {
      title: '检查日期',
      dataIndex: 'checkDate',
      key: 'checkDate',
      width: 120,
    },
    {
      title: '检查区域',
      dataIndex: 'checkArea',
      key: 'checkArea',
      width: 120,
    },
    {
      title: '检查人员',
      dataIndex: 'checkerName',
      key: 'checkerName',
      width: 100,
    },
    {
      title: '整理得分',
      dataIndex: 'sortScore',
      key: 'sortScore',
      width: 100,
      render: (value: number | undefined) => value != null ? `${value}/20` : '-',
    },
    {
      title: '整顿得分',
      dataIndex: 'setScore',
      key: 'setScore',
      width: 100,
      render: (value: number | undefined) => value != null ? `${value}/20` : '-',
    },
    {
      title: '清扫得分',
      dataIndex: 'shineScore',
      key: 'shineScore',
      width: 100,
      render: (value: number | undefined) => value != null ? `${value}/20` : '-',
    },
    {
      title: '清洁得分',
      dataIndex: 'standardizeScore',
      key: 'standardizeScore',
      width: 100,
      render: (value: number | undefined) => value != null ? `${value}/20` : '-',
    },
    {
      title: '素养得分',
      dataIndex: 'sustainScore',
      key: 'sustainScore',
      width: 100,
      render: (value: number | undefined) => value != null ? `${value}/20` : '-',
    },
    {
      title: '总分',
      dataIndex: 'totalScore',
      key: 'totalScore',
      width: 100,
      render: (value: number | undefined) => {
        if (value == null) return '-';
        const color = value >= 90 ? 'green' : value >= 70 ? 'orange' : 'red';
        return <Tag color={color}>{value}/100</Tag>;
      },
    },
    {
      title: '问题描述',
      dataIndex: 'problemDescription',
      key: 'problemDescription',
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_: any, record: Site5sCheckInfo) => (
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
    <div className="site5s-check-list">
      <Form form={form} layout="inline" className="search-form">
        <Form.Item name="checkDate" label="检查日期">
          <DatePicker format="YYYY-MM-DD" placeholder="请选择检查日期" />
        </Form.Item>
        <Form.Item name="checkArea" label="检查区域">
          <Input placeholder="请输入检查区域" allowClear />
        </Form.Item>
        <Form.Item name="checkerName" label="检查人员">
          <Input placeholder="请输入检查人员" allowClear />
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
          新增检查记录
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

      <CheckModal
        visible={modalVisible}
        editingCheck={editingCheck}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleModalSuccess}
      />
    </div>
  );
};

export default CheckList;
