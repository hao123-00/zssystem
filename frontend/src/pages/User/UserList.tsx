import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Select, Space, message, Popconfirm, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, LockOutlined } from '@ant-design/icons';
import { getUserList, deleteUser, resetPassword, enableUser, disableUser, UserInfo, UserQueryParams } from '@/api/user';
import UserModal from './UserModal';
import { useResponsive } from '@/hooks/useResponsive';
import { ResponsiveSearch } from '@/components/ResponsiveSearch';
import { MobileCardList, FieldConfig, ActionConfig } from '@/components/MobileCard';
import './UserList.less';

const UserList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<UserInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState<UserInfo | null>(null);
  const { isMobile } = useResponsive();

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: UserQueryParams = {
        ...values,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getUserList(params);
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
    setEditingUser(null);
    setModalVisible(true);
  };

  const handleEdit = (record: UserInfo) => {
    setEditingUser(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteUser(id);
      message.success('删除成功');
      fetchList();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  const handleResetPassword = async (id: number) => {
    try {
      await resetPassword(id);
      message.success('密码重置成功，默认密码：123456');
    } catch (error: any) {
      message.error(error.message || '重置失败');
    }
  };

  const handleEnable = async (id: number) => {
    try {
      await enableUser(id);
      message.success('启用成功');
      fetchList();
    } catch (error: any) {
      message.error(error.message || '启用失败');
    }
  };

  const handleDisable = async (id: number) => {
    try {
      await disableUser(id);
      message.success('禁用成功');
      fetchList();
    } catch (error: any) {
      message.error(error.message || '禁用失败');
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

  // PC 端表格列配置：工号、姓名、班组、岗位、类别、入职日期
  const columns = [
    {
      title: '工号',
      dataIndex: 'employeeNo',
      key: 'employeeNo',
      width: 100,
    },
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
      width: 100,
    },
    {
      title: '班组',
      dataIndex: 'team',
      key: 'team',
      width: 100,
    },
    {
      title: '岗位',
      dataIndex: 'position',
      key: 'position',
      width: 100,
    },
    {
      title: '类别',
      dataIndex: 'category',
      key: 'category',
      width: 100,
    },
    {
      title: '入职日期',
      dataIndex: 'hireDate',
      key: 'hireDate',
      width: 110,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: number) => (
        <Tag color={status === 1 ? 'success' : 'error'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '角色',
      key: 'roles',
      width: 200,
      render: (_: any, record: UserInfo) => {
        if (record.roles && record.roles.length > 0) {
          return (
            <Space>
              {record.roles.map((role) => (
                <Tag key={role.id}>{role.roleName}</Tag>
              ))}
            </Space>
          );
        }
        return '-';
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 280,
      fixed: 'right' as const,
      render: (_: any, record: UserInfo) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          {record.status === 1 ? (
            <Button type="link" onClick={() => handleDisable(record.id)}>
              禁用
            </Button>
          ) : (
            <Button type="link" onClick={() => handleEnable(record.id)}>
              启用
            </Button>
          )}
          <Popconfirm
            title="确定要重置密码吗？"
            onConfirm={() => handleResetPassword(record.id)}
          >
            <Button type="link" icon={<LockOutlined />}>
              重置密码
            </Button>
          </Popconfirm>
          <Popconfirm
            title="确定要删除吗？"
            onConfirm={() => handleDelete(record.id)}
          >
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
    { key: 'employeeNo', label: '工号' },
    { key: 'team', label: '班组' },
    { key: 'position', label: '岗位' },
    { key: 'category', label: '类别' },
    { key: 'hireDate', label: '入职日期' },
    { key: 'phone', label: '手机号' },
    { key: 'email', label: '邮箱' },
    {
      key: 'status',
      label: '状态',
      render: (value: number) => (
        <Tag color={value === 1 ? 'success' : 'error'}>
          {value === 1 ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      key: 'roles',
      label: '角色',
      render: (_: any, record: UserInfo) => {
        if (record.roles && record.roles.length > 0) {
          return record.roles.map((r) => r.roleName).join(', ');
        }
        return '-';
      },
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
      key: 'toggle',
      label: '启用/禁用',
      onClick: (record) => {
        if (record.status === 1) {
          handleDisable(record.id);
        } else {
          handleEnable(record.id);
        }
      },
    },
    {
      key: 'reset',
      label: '重置密码',
      icon: <LockOutlined />,
      onClick: (record) => handleResetPassword(record.id),
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
    <div className={`user-list-container ${isMobile ? 'user-list-mobile' : ''}`}>
      <ResponsiveSearch
        form={form}
        onSearch={handleSearch}
        onReset={handleReset}
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            {isMobile ? '新增' : '新增用户'}
          </Button>
        }
      >
        <Form.Item name="username" label={isMobile ? '用户名' : undefined}>
          <Input placeholder="用户名" allowClear />
        </Form.Item>
        <Form.Item name="realName" label={isMobile ? '真实姓名' : undefined}>
          <Input placeholder="真实姓名" allowClear />
        </Form.Item>
        <Form.Item name="status" label={isMobile ? '状态' : undefined}>
          <Select placeholder="状态" style={{ width: isMobile ? '100%' : 120 }} allowClear>
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
          titleField={{ key: 'employeeNo', label: '工号' }}
          subtitleField={{ key: 'name', label: '姓名' }}
          fields={mobileFields}
          actions={mobileActions}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            onChange: (page, pageSize) => {
              setPagination({ ...pagination, current: page, pageSize: pageSize || 10 });
            },
          }}
        />
      ) : (
        <Table
          columns={columns}
          dataSource={tableData}
          loading={loading}
          rowKey="id"
          scroll={{ x: 1200 }}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, pageSize) => {
              setPagination({ ...pagination, current: page, pageSize: pageSize || 10 });
            },
          }}
        />
      )}

      <UserModal
        visible={modalVisible}
        user={editingUser}
        onCancel={() => setModalVisible(false)}
        onSuccess={() => {
          setModalVisible(false);
          fetchList();
        }}
      />
    </div>
  );
};

export default UserList;
