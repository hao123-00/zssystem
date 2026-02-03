import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Select, Space, message, Popconfirm, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import {
  getSite5sAreaList,
  deleteSite5sArea,
  Site5sAreaInfo,
  Site5sAreaQueryParams,
} from '@/api/site5s';
import AreaModal from './AreaModal';
import { useResponsive } from '@/hooks/useResponsive';
import { ResponsiveSearch } from '@/components/ResponsiveSearch';
import { MobileCardList, FieldConfig, ActionConfig } from '@/components/MobileCard';
import './CheckList.less';

const AreaList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<Site5sAreaInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingArea, setEditingArea] = useState<Site5sAreaInfo | null>(null);
  const { isMobile } = useResponsive();

  useEffect(() => {
    fetchList();
  }, [pagination.current, pagination.pageSize]);

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: Site5sAreaQueryParams = {
        ...values,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getSite5sAreaList(params);
      setTableData(response.list || []);
      setPagination({ ...pagination, total: response.total || 0 });
    } catch (error: any) {
      message.error(error.message || '查询失败');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingArea(null);
    setModalVisible(true);
  };

  const handleEdit = (area: Site5sAreaInfo) => {
    setEditingArea(area);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteSite5sArea(id);
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
      title: '区域编码',
      dataIndex: 'areaCode',
      key: 'areaCode',
      width: 120,
    },
    {
      title: '区域名称',
      dataIndex: 'areaName',
      key: 'areaName',
      width: 120,
    },
    {
      title: '职能名称',
      dataIndex: 'dutyName',
      key: 'dutyName',
      width: 120,
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      key: 'sortOrder',
      width: 80,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (v: number) => (v === 1 ? <Tag color="success">启用</Tag> : <Tag color="default">停用</Tag>),
    },
    {
      title: '时段配置',
      key: 'schedules',
      width: 180,
      render: (_: any, record: Site5sAreaInfo) => {
        const schedules = record.schedules || [];
        return schedules.length > 0
          ? schedules.map((s) => `${s.scheduledTime}`).join('、')
          : '-';
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 160,
      fixed: 'right' as const,
      render: (_: any, record: Site5sAreaInfo) => (
        <Space>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm title="确定删除该区域？" onConfirm={() => handleDelete(record.id!)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const mobileFields: FieldConfig[] = [
    { key: 'areaCode', label: '区域编码' },
    { key: 'areaName', label: '区域名称' },
    { key: 'dutyName', label: '职能名称' },
    { key: 'status', label: '状态', render: (v: number) => (v === 1 ? '启用' : '停用') },
  ];

  const mobileActions: ActionConfig[] = [
    { label: '编辑', onClick: (record) => handleEdit(record) },
    { label: '删除', danger: true, onClick: (record) => handleDelete(record.id!), confirm: '确定删除？' },
  ];

  return (
    <div className="site5s-check-list">
      <ResponsiveSearch
        form={form}
        onSearch={handleSearch}
        onReset={handleReset}
        addButton={{ text: '新增区域', onClick: handleAdd }}
        items={[
          { name: 'areaCode', label: '区域编码', component: <Input placeholder="区域编码" allowClear /> },
          { name: 'areaName', label: '区域名称', component: <Input placeholder="区域名称" allowClear /> },
          { name: 'dutyName', label: '职能名称', component: <Input placeholder="职能名称" allowClear /> },
          {
            name: 'status',
            label: '状态',
            component: (
              <Select placeholder="状态" allowClear options={[
                { value: 1, label: '启用' },
                { value: 0, label: '停用' },
              ]} />
            ),
          },
        ]}
      />

      {isMobile ? (
        <MobileCardList
          dataSource={tableData}
          loading={loading}
          titleField="areaName"
          subtitleField="dutyName"
          fields={mobileFields}
          actions={mobileActions}
        />
      ) : (
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
            showTotal: (t) => `共 ${t} 条`,
            onChange: handleTableChange,
          }}
          scroll={{ x: 800 }}
        />
      )}

      <AreaModal
        visible={modalVisible}
        area={editingArea}
        onCancel={() => setModalVisible(false)}
        onSuccess={handleModalSuccess}
      />
    </div>
  );
};

export default AreaList;
