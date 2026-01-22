import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Select, Space, message, Popconfirm, Tag, DatePicker } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  getCheckList,
  deleteCheck,
  EquipmentCheckInfo,
  EquipmentCheckQueryParams,
} from '@/api/equipment';
import CheckModal from './CheckModal';
import { getEquipmentList } from '@/api/equipment';
import dayjs from 'dayjs';
import './CheckList.less';

const CheckList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<EquipmentCheckInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 30, // 默认30条（一个月）
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingCheck, setEditingCheck] = useState<EquipmentCheckInfo | null>(null);
  const [equipmentList, setEquipmentList] = useState<any[]>([]);

  useEffect(() => {
    loadEquipmentList();
    fetchList();
  }, [pagination.current, pagination.pageSize]);

  const loadEquipmentList = async () => {
    try {
      const response = await getEquipmentList({ pageNum: 1, pageSize: 1000 });
      setEquipmentList(response.list || []);
    } catch (error: any) {
      console.error('加载设备列表失败', error);
    }
  };

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: EquipmentCheckQueryParams = {
        ...values,
        checkMonth: values.checkMonth ? values.checkMonth.format('YYYY-MM') : undefined,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getCheckList(params);
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

  const handleEdit = (check: EquipmentCheckInfo) => {
    setEditingCheck(check);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteCheck(id);
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

  const renderCheckItem = (value: number | undefined) => {
    if (value === undefined || value === null) {
      return <Tag>-</Tag>;
    }
    return value === 1 ? (
      <Tag color="success">正常</Tag>
    ) : (
      <Tag color="error">异常</Tag>
    );
  };

  const columns = [
    {
      title: '检查日期',
      dataIndex: 'checkDate',
      key: 'checkDate',
      width: 120,
      fixed: 'left' as const,
    },
    {
      title: '设备编号',
      dataIndex: 'equipmentNo',
      key: 'equipmentNo',
      width: 120,
      fixed: 'left' as const,
    },
    {
      title: '设备名称',
      dataIndex: 'equipmentName',
      key: 'equipmentName',
      width: 150,
      fixed: 'left' as const,
    },
    {
      title: '检点人',
      dataIndex: 'checkerName',
      key: 'checkerName',
      width: 100,
    },
    {
      title: '电路1',
      dataIndex: 'circuitItem1',
      key: 'circuitItem1',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '电路2',
      dataIndex: 'circuitItem2',
      key: 'circuitItem2',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '电路3',
      dataIndex: 'circuitItem3',
      key: 'circuitItem3',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '机架1',
      dataIndex: 'frameItem1',
      key: 'frameItem1',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '机架2',
      dataIndex: 'frameItem2',
      key: 'frameItem2',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '机架3',
      dataIndex: 'frameItem3',
      key: 'frameItem3',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '油路1',
      dataIndex: 'oilItem1',
      key: 'oilItem1',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '油路2',
      dataIndex: 'oilItem2',
      key: 'oilItem2',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '油路3',
      dataIndex: 'oilItem3',
      key: 'oilItem3',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '油路4',
      dataIndex: 'oilItem4',
      key: 'oilItem4',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '油路5',
      dataIndex: 'oilItem5',
      key: 'oilItem5',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '周边1',
      dataIndex: 'peripheralItem1',
      key: 'peripheralItem1',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '周边2',
      dataIndex: 'peripheralItem2',
      key: 'peripheralItem2',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '周边3',
      dataIndex: 'peripheralItem3',
      key: 'peripheralItem3',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '周边4',
      dataIndex: 'peripheralItem4',
      key: 'peripheralItem4',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '周边5',
      dataIndex: 'peripheralItem5',
      key: 'peripheralItem5',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      width: 200,
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_: any, record: EquipmentCheckInfo) => (
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
    <div className="check-list-container">
      <div className="search-form">
        <Form form={form} layout="inline">
          <Form.Item name="equipmentNo">
            <Select
              placeholder="设备编号"
              allowClear
              showSearch
              filterOption={(input, option) =>
                (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
              }
              options={equipmentList.map((eq) => ({
                value: eq.equipmentNo,
                label: `${eq.equipmentNo} - ${eq.equipmentName}`,
              }))}
              style={{ width: 200 }}
            />
          </Form.Item>
          <Form.Item name="checkMonth">
            <DatePicker picker="month" placeholder="检查月份" format="YYYY-MM" />
          </Form.Item>
          <Form.Item name="checkerName">
            <Input placeholder="检点人" allowClear />
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
          新增点检
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
        scroll={{ x: 2000 }}
        pagination={{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showTotal: (total) => `共 ${total} 条`,
          onChange: handleTableChange,
        }}
      />

      <CheckModal
        visible={modalVisible}
        check={editingCheck}
        onCancel={() => setModalVisible(false)}
        onSuccess={() => {
          setModalVisible(false);
          fetchList();
        }}
      />
    </div>
  );
};

export default CheckList;
