import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Select, Space, message, Popconfirm, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import {
  getEmployeeList,
  deleteEmployee,
  EmployeeInfo,
  EmployeeQueryParams,
} from '@/api/employee';
import { getDepartmentTree, DepartmentTreeVO } from '@/api/department';
import EmployeeModal from './EmployeeModal';
import { useResponsive } from '@/hooks/useResponsive';
import { ResponsiveSearch } from '@/components/ResponsiveSearch';
import { MobileCardList, FieldConfig, ActionConfig } from '@/components/MobileCard';
import './EmployeeList.less';

const EmployeeList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<EmployeeInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState<EmployeeInfo | null>(null);
  const [departmentTree, setDepartmentTree] = useState<DepartmentTreeVO[]>([]);
  const { isMobile } = useResponsive();

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: EmployeeQueryParams = {
        ...values,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getEmployeeList(params);
      setTableData(response.list || []);
      setPagination({ ...pagination, total: response.total || 0 });
    } catch (error: any) {
      message.error(error.message || '查询失败');
    } finally {
      setLoading(false);
    }
  };

  const loadDepartmentTree = async () => {
    try {
      const tree = await getDepartmentTree();
      setDepartmentTree(tree || []);
    } catch (error: any) {
      message.error('加载部门列表失败');
    }
  };

  useEffect(() => {
    fetchList();
    loadDepartmentTree();
  }, [pagination.current, pagination.pageSize]);

  const handleAdd = () => {
    setEditingEmployee(null);
    setModalVisible(true);
  };

  const handleEdit = (record: EmployeeInfo) => {
    setEditingEmployee(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteEmployee(id);
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

  // 扁平化部门树用于选择器
  const flattenDepartments = (nodes: DepartmentTreeVO[]): DepartmentTreeVO[] => {
    let result: DepartmentTreeVO[] = [];
    nodes.forEach((node) => {
      result.push(node);
      if (node.children && node.children.length > 0) {
        result = result.concat(flattenDepartments(node.children));
      }
    });
    return result;
  };

  const columns = [
    {
      title: '工号',
      dataIndex: 'employeeNo',
      key: 'employeeNo',
      width: 120,
    },
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
      width: 100,
    },
    {
      title: '性别',
      dataIndex: 'gender',
      key: 'gender',
      width: 80,
      render: (gender: number) => (gender === 1 ? '男' : gender === 0 ? '女' : '-'),
    },
    {
      title: '年龄',
      dataIndex: 'age',
      key: 'age',
      width: 80,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
      width: 120,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: 180,
    },
    {
      title: '部门',
      dataIndex: 'departmentName',
      key: 'departmentName',
      width: 150,
    },
    {
      title: '职位',
      dataIndex: 'position',
      key: 'position',
      width: 120,
    },
    {
      title: '入职日期',
      dataIndex: 'entryDate',
      key: 'entryDate',
      width: 120,
      render: (text: string) => (text ? text : '-'),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: number) => (
        <Tag color={status === 1 ? 'success' : 'error'}>{status === 1 ? '在职' : '离职'}</Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_: any, record: EmployeeInfo) => (
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
    { key: 'employeeNo', label: '工号' },
    { key: 'phone', label: '手机号' },
    { key: 'departmentName', label: '部门' },
    { key: 'position', label: '职位' },
    {
      key: 'status',
      label: '状态',
      render: (value: number) => (
        <Tag color={value === 1 ? 'success' : 'error'}>{value === 1 ? '在职' : '离职'}</Tag>
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
      key: 'delete',
      label: '删除',
      icon: <DeleteOutlined />,
      danger: true,
      onClick: (record) => handleDelete(record.id),
    },
  ];

  return (
    <div className={`employee-list-container ${isMobile ? 'employee-list-mobile' : ''}`}>
      <ResponsiveSearch
        form={form}
        onSearch={handleSearch}
        onReset={handleReset}
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            {isMobile ? '新增' : '新增员工'}
          </Button>
        }
      >
        <Form.Item name="name" label={isMobile ? '姓名' : undefined}>
          <Input placeholder="姓名" allowClear />
        </Form.Item>
        <Form.Item name="employeeNo" label={isMobile ? '工号' : undefined}>
          <Input placeholder="工号" allowClear />
        </Form.Item>
        <Form.Item name="departmentId" label={isMobile ? '部门' : undefined}>
          <Select placeholder="部门" allowClear style={{ width: isMobile ? '100%' : 200 }}>
            {flattenDepartments(departmentTree).map((dept) => (
              <Select.Option key={dept.id} value={dept.id}>
                {dept.deptName}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item name="status" label={isMobile ? '状态' : undefined}>
          <Select placeholder="状态" allowClear style={{ width: isMobile ? '100%' : 120 }}>
            <Select.Option value={1}>在职</Select.Option>
            <Select.Option value={0}>离职</Select.Option>
          </Select>
        </Form.Item>
      </ResponsiveSearch>

      {isMobile ? (
        <MobileCardList
          dataSource={tableData}
          loading={loading}
          rowKey="id"
          titleField={{ key: 'name', label: '姓名' }}
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
          scroll={{ x: 1400 }}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showTotal: (total) => `共 ${total} 条`,
            onChange: handleTableChange,
          }}
        />
      )}

      <EmployeeModal
        visible={modalVisible}
        employee={editingEmployee}
        departmentTree={departmentTree}
        onCancel={() => setModalVisible(false)}
        onSuccess={() => {
          setModalVisible(false);
          fetchList();
        }}
      />
    </div>
  );
};

export default EmployeeList;
