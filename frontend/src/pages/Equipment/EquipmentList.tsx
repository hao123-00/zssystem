import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Select, Space, message, Popconfirm, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import {
  getEquipmentList,
  deleteEquipment,
  EquipmentInfo,
  EquipmentQueryParams,
} from '@/api/equipment';
import EquipmentModal from './EquipmentModal';
import { useResponsive } from '@/hooks/useResponsive';
import { ResponsiveSearch } from '@/components/ResponsiveSearch';
import { MobileCardList, FieldConfig, ActionConfig } from '@/components/MobileCard';
import './EquipmentList.less';

const EquipmentList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<EquipmentInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingEquipment, setEditingEquipment] = useState<EquipmentInfo | null>(null);
  const { isMobile } = useResponsive();

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: EquipmentQueryParams = {
        ...values,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getEquipmentList(params);
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
    setEditingEquipment(null);
    setModalVisible(true);
  };

  const handleEdit = (equipment: EquipmentInfo) => {
    setEditingEquipment(equipment);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteEquipment(id);
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

  const renderStatus = (status: number) => {
    const statusMap: Record<number, { text: string; color: string }> = {
      0: { text: '停用', color: 'default' },
      1: { text: '正常', color: 'success' },
      2: { text: '维修中', color: 'warning' },
    };
    const statusInfo = statusMap[status] || { text: '未知', color: 'default' };
    return <Tag color={statusInfo.color}>{statusInfo.text}</Tag>;
  };

  const columns = [
    {
      title: '设备编号',
      dataIndex: 'equipmentNo',
      key: 'equipmentNo',
      width: 150,
    },
    {
      title: '设备名称',
      dataIndex: 'equipmentName',
      key: 'equipmentName',
      width: 150,
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
      width: 100,
    },
    {
      title: '设备型号',
      dataIndex: 'equipmentModel',
      key: 'equipmentModel',
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: renderStatus,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_: any, record: EquipmentInfo) => (
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

  // 移动端卡片字段配置
  const mobileFields: FieldConfig[] = [
    { key: 'equipmentNo', label: '编号' },
    { key: 'groupName', label: '组别' },
    { key: 'machineNo', label: '机台号' },
    { key: 'equipmentModel', label: '型号' },
    {
      key: 'status',
      label: '状态',
      render: renderStatus,
    },
  ];

  // 移动端操作按钮配置
  const mobileActions: ActionConfig[] = [
    {
      key: 'edit',
      label: '编辑',
      icon: <EditOutlined />,
      onClick: (record) => handleEdit(record),
    },
    {
      key: 'delete',
      label: '删除',
      icon: <DeleteOutlined />,
      danger: true,
      onClick: (record) => handleDelete(record.id),
    },
  ];

  return (
    <div className={`equipment-list-container ${isMobile ? 'equipment-list-mobile' : ''}`}>
      <ResponsiveSearch
        form={form}
        onSearch={handleSearch}
        onReset={handleReset}
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            {isMobile ? '新增' : '新增设备'}
          </Button>
        }
      >
        <Form.Item name="equipmentNo" label={isMobile ? '设备编号' : undefined}>
          <Input placeholder="设备编号" allowClear />
        </Form.Item>
        <Form.Item name="equipmentName" label={isMobile ? '设备名称' : undefined}>
          <Input placeholder="设备名称" allowClear />
        </Form.Item>
        <Form.Item name="groupName" label={isMobile ? '组别' : undefined}>
          <Input placeholder="组别" allowClear />
        </Form.Item>
        <Form.Item name="status" label={isMobile ? '状态' : undefined}>
          <Select placeholder="状态" allowClear style={{ width: isMobile ? '100%' : 120 }}>
            <Select.Option value={0}>停用</Select.Option>
            <Select.Option value={1}>正常</Select.Option>
            <Select.Option value={2}>维修中</Select.Option>
          </Select>
        </Form.Item>
      </ResponsiveSearch>

      {isMobile ? (
        <MobileCardList
          dataSource={tableData}
          loading={loading}
          rowKey="id"
          titleField={{ key: 'equipmentName', label: '设备名称' }}
          fields={mobileFields}
          actions={mobileActions}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            onChange: handleTableChange,
          }}
        />
      ) : (
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
      )}

      <EquipmentModal
        visible={modalVisible}
        equipment={editingEquipment}
        onCancel={() => setModalVisible(false)}
        onSuccess={() => {
          setModalVisible(false);
          fetchList();
        }}
      />
    </div>
  );
};

export default EquipmentList;
