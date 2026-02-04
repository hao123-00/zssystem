import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Select, Space, message, Popconfirm, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import {
  getSite5sAreaList,
  deleteSite5sArea,
  getAreaCanManage,
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
  const [canManage, setCanManage] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingArea, setEditingArea] = useState<Site5sAreaInfo | null>(null);
  const { isMobile } = useResponsive();

  useEffect(() => {
    getAreaCanManage().then((v) => setCanManage(!!v)).catch(() => setCanManage(false));
  }, []);

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
      width: 100,
    },
    {
      title: '区域名称',
      dataIndex: 'areaName',
      key: 'areaName',
      width: 120,
    },
    {
      title: '检查项目',
      dataIndex: 'checkItem',
      key: 'checkItem',
      width: 120,
    },
    {
      title: '负责人',
      key: 'responsible',
      width: 160,
      render: (_: any, record: Site5sAreaInfo) => {
        const a = record.responsibleUserName || '';
        const b = record.responsibleUserName2 || '';
        return a && b ? `${a}、${b}` : a || b || '-';
      },
    },
    {
      title: '早间拍照',
      dataIndex: 'morningPhotoTime',
      key: 'morningPhotoTime',
      width: 90,
      render: (v: string) => (v ? String(v).slice(0, 5) : '-'),
    },
    {
      title: '晚间拍照',
      dataIndex: 'eveningPhotoTime',
      key: 'eveningPhotoTime',
      width: 90,
      render: (v: string) => (v ? String(v).slice(0, 5) : '-'),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (v: number) => (v === 1 ? <Tag color="success">启用</Tag> : <Tag color="default">停用</Tag>),
    },
    ...(canManage
      ? [
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
        ]
      : []),
  ];

  const mobileFields: FieldConfig[] = [
    { key: 'areaName', label: '区域名称' },
    { key: 'checkItem', label: '检查项目' },
    {
      key: 'responsible',
      label: '负责人',
      render: (_: any, record: Site5sAreaInfo) => {
        const a = record.responsibleUserName || '';
        const b = record.responsibleUserName2 || '';
        return a && b ? `${a}、${b}` : a || b || '-';
      },
    },
    { key: 'status', label: '状态', render: (v: number) => (v === 1 ? '启用' : '停用') },
  ];

  const mobileActions: ActionConfig[] = canManage
    ? [
        { label: '编辑', onClick: (record) => handleEdit(record) },
        { label: '删除', danger: true, onClick: (record) => handleDelete(record.id!), confirm: '确定删除？' },
      ]
    : [];

  return (
    <div className="site5s-check-list">
      <ResponsiveSearch
        form={form}
        onSearch={handleSearch}
        onReset={handleReset}
        extra={
          canManage ? (
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              {isMobile ? '新增' : '新增区域'}
            </Button>
          ) : undefined
        }
      >
        <Form.Item name="areaName" label={isMobile ? '区域名称' : '区域名称'}>
          <Input placeholder="区域名称" allowClear />
        </Form.Item>
        <Form.Item name="checkItem" label={isMobile ? '检查项目' : '检查项目'}>
          <Input placeholder="检查项目" allowClear />
        </Form.Item>
        <Form.Item name="status" label={isMobile ? '状态' : '状态'}>
          <Select placeholder="状态" allowClear options={[
            { value: 1, label: '启用' },
            { value: 0, label: '停用' },
          ]} />
        </Form.Item>
      </ResponsiveSearch>

      {isMobile ? (
        <MobileCardList
          dataSource={tableData}
          loading={loading}
          titleField="areaName"
          subtitleField="checkItem"
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
