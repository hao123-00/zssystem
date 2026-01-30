import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Select, Space, message, Popconfirm, Tag, Modal } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SettingOutlined } from '@ant-design/icons';
import {
  getRoleList,
  deleteRole,
  getRoleById,
  RoleInfo,
  RoleQueryParams,
} from '@/api/role';
import { getPermissionTree, PermissionTreeVO } from '@/api/permission';
import { assignPermissions, getRolePermissions } from '@/api/role';
import PermissionTree from '@/components/PermissionTree/PermissionTree';
import RoleModal from './RoleModal';
import { useResponsive } from '@/hooks/useResponsive';
import { ResponsiveSearch } from '@/components/ResponsiveSearch';
import { MobileCardList, FieldConfig, ActionConfig } from '@/components/MobileCard';
import './RoleList.less';

const RoleList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<RoleInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [permissionModalVisible, setPermissionModalVisible] = useState(false);
  const [editingRole, setEditingRole] = useState<RoleInfo | null>(null);
  const [currentRoleId, setCurrentRoleId] = useState<number | null>(null);
  const [permissionTree, setPermissionTree] = useState<PermissionTreeVO[]>([]);
  const [selectedPermissions, setSelectedPermissions] = useState<number[]>([]);
  const { isMobile } = useResponsive();

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: RoleQueryParams = {
        ...values,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getRoleList(params);
      setTableData(response.list || []);
      setPagination({ ...pagination, total: response.total || 0 });
    } catch (error: any) {
      message.error(error.message || '查询失败');
    } finally {
      setLoading(false);
    }
  };

  const loadPermissionTree = async () => {
    try {
      const tree = await getPermissionTree();
      setPermissionTree(tree || []);
    } catch (error: any) {
      message.error('加载权限树失败');
    }
  };

  useEffect(() => {
    fetchList();
    loadPermissionTree();
  }, [pagination.current, pagination.pageSize]);

  const handleAdd = () => {
    setEditingRole(null);
    setModalVisible(true);
  };

  const handleEdit = async (record: RoleInfo) => {
    try {
      const roleDetail = await getRoleById(record.id);
      setEditingRole(roleDetail);
      setModalVisible(true);
    } catch (error: any) {
      message.error('加载角色详情失败');
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteRole(id);
      message.success('删除成功');
      fetchList();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  const handleAssignPermissions = async (roleId: number) => {
    try {
      const permissionIds = await getRolePermissions(roleId);
      setSelectedPermissions(permissionIds || []);
      setCurrentRoleId(roleId);
      setPermissionModalVisible(true);
    } catch (error: any) {
      message.error('加载角色权限失败');
    }
  };

  const handleSavePermissions = async () => {
    if (currentRoleId === null) {
      return;
    }
    try {
      await assignPermissions(currentRoleId, selectedPermissions);
      message.success('权限分配成功');
      setPermissionModalVisible(false);
      fetchList();
    } catch (error: any) {
      message.error(error.message || '权限分配失败');
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
      title: '角色编码',
      dataIndex: 'roleCode',
      key: 'roleCode',
    },
    {
      title: '角色名称',
      dataIndex: 'roleName',
      key: 'roleName',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>{status === 1 ? '启用' : '禁用'}</Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (text: string) => (text ? new Date(text).toLocaleString() : '-'),
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      render: (_: any, record: RoleInfo) => (
        <Space>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            icon={<SettingOutlined />}
            onClick={() => handleAssignPermissions(record.id)}
          >
            分配权限
          </Button>
          <Popconfirm title="确定要删除这个角色吗？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // 移动端卡片字段配置
  const mobileFields: FieldConfig[] = [
    { key: 'roleCode', label: '编码' },
    { key: 'description', label: '描述' },
    {
      key: 'status',
      label: '状态',
      render: (value: number) => (
        <Tag color={value === 1 ? 'green' : 'red'}>{value === 1 ? '启用' : '禁用'}</Tag>
      ),
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
      key: 'permission',
      label: '分配权限',
      icon: <SettingOutlined />,
      onClick: (record) => handleAssignPermissions(record.id),
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
    <div className={`role-list-container ${isMobile ? 'role-list-mobile' : ''}`}>
      <ResponsiveSearch
        form={form}
        onSearch={handleSearch}
        onReset={handleReset}
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            {isMobile ? '新增' : '新增角色'}
          </Button>
        }
      >
        <Form.Item name="roleName" label={isMobile ? '角色名称' : undefined}>
          <Input placeholder="角色名称" allowClear />
        </Form.Item>
        <Form.Item name="status" label={isMobile ? '状态' : undefined}>
          <Select placeholder="状态" allowClear style={{ width: isMobile ? '100%' : 120 }}>
            <Select.Option value={1}>启用</Select.Option>
            <Select.Option value={0}>禁用</Select.Option>
          </Select>
        </Form.Item>
      </ResponsiveSearch>

      {isMobile ? (
        <MobileCardList
          dataSource={tableData}
          loading={loading}
          rowKey="id"
          titleField={{ key: 'roleName', label: '角色名称' }}
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
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showTotal: (total) => `共 ${total} 条`,
            onChange: handleTableChange,
          }}
        />
      )}

      <RoleModal
        visible={modalVisible}
        role={editingRole}
        onCancel={() => setModalVisible(false)}
        onSuccess={() => {
          setModalVisible(false);
          fetchList();
        }}
      />

      <Modal
        title="分配权限"
        open={permissionModalVisible}
        onOk={handleSavePermissions}
        onCancel={() => setPermissionModalVisible(false)}
        width={isMobile ? '100%' : 600}
        className={isMobile ? '' : undefined}
      >
        <PermissionTree
          value={selectedPermissions}
          onChange={setSelectedPermissions}
          treeData={permissionTree}
        />
      </Modal>
    </div>
  );
};

export default RoleList;
