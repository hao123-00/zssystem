import React, { useEffect, useState } from 'react';
import { Tree, Button, Space, message, Popconfirm, Modal, Form, Input, Select, InputNumber } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  getDepartmentTree,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  DepartmentTreeVO,
  DepartmentSaveParams,
} from '@/api/department';
import './DepartmentList.less';

const DepartmentList: React.FC = () => {
  const [form] = Form.useForm();
  const [treeData, setTreeData] = useState<DepartmentTreeVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingDepartment, setEditingDepartment] = useState<DepartmentTreeVO | null>(null);
  const [selectedKeys, setSelectedKeys] = useState<React.Key[]>([]);
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);

  const fetchTree = async () => {
    setLoading(true);
    try {
      const response = await getDepartmentTree();
      setTreeData(response || []);
      // 默认展开所有节点
      const keys: React.Key[] = [];
      const collectKeys = (nodes: DepartmentTreeVO[]) => {
        nodes.forEach((node) => {
          keys.push(node.id);
          if (node.children && node.children.length > 0) {
            collectKeys(node.children);
          }
        });
      };
      collectKeys(response || []);
      setExpandedKeys(keys);
    } catch (error: any) {
      message.error(error.message || '查询失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTree();
  }, []);

  const handleAdd = (parentId?: number) => {
    form.resetFields();
    form.setFieldsValue({
      parentId: parentId || 0,
      status: 1,
      sortOrder: 0,
    });
    setEditingDepartment(null);
    setModalVisible(true);
  };

  const handleEdit = (department: DepartmentTreeVO) => {
    form.resetFields();
    form.setFieldsValue({
      id: department.id,
      parentId: department.parentId,
      deptName: department.deptName,
      deptCode: department.deptCode,
      leader: department.leader,
      phone: department.phone,
      email: department.email,
      sortOrder: department.sortOrder,
      status: department.status,
    });
    setEditingDepartment(department);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteDepartment(id);
      message.success('删除成功');
      fetchTree();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const params: DepartmentSaveParams = {
        ...values,
      };
      if (editingDepartment) {
        await updateDepartment(editingDepartment.id, params);
        message.success('更新成功');
      } else {
        await createDepartment(params);
        message.success('创建成功');
      }
      setModalVisible(false);
      fetchTree();
    } catch (error: any) {
      if (error.errorFields) {
        return;
      }
      message.error(error.message || '操作失败');
    }
  };

  const convertToTreeData = (nodes: DepartmentTreeVO[]): any[] => {
    return nodes.map((node) => ({
      key: node.id,
      title: (
        <div className="tree-node-title">
          <span>{node.deptName}</span>
          {node.deptCode && <span className="tree-node-code">({node.deptCode})</span>}
          <Space className="tree-node-actions">
            <Button
              type="link"
              size="small"
              icon={<PlusOutlined />}
              onClick={(e) => {
                e.stopPropagation();
                handleAdd(node.id);
              }}
            >
              添加子部门
            </Button>
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={(e) => {
                e.stopPropagation();
                handleEdit(node);
              }}
            >
              编辑
            </Button>
            <Popconfirm
              title="确定要删除这个部门吗？"
              onConfirm={(e) => {
                e?.stopPropagation();
                handleDelete(node.id);
              }}
              onClick={(e) => e.stopPropagation()}
            >
              <Button
                type="link"
                size="small"
                danger
                icon={<DeleteOutlined />}
                onClick={(e) => e.stopPropagation()}
              >
                删除
              </Button>
            </Popconfirm>
          </Space>
        </div>
      ),
      children: node.children && node.children.length > 0 ? convertToTreeData(node.children) : undefined,
    }));
  };

  return (
    <div className="department-list-container">
      <div className="toolbar">
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>
            添加根部门
          </Button>
          <Button icon={<ReloadOutlined />} onClick={fetchTree}>
            刷新
          </Button>
        </Space>
      </div>

      <div className="tree-container">
        <Tree
          showLine
          loading={loading}
          expandedKeys={expandedKeys}
          selectedKeys={selectedKeys}
          onExpand={setExpandedKeys}
          onSelect={setSelectedKeys}
          treeData={convertToTreeData(treeData)}
        />
      </div>

      <Modal
        title={editingDepartment ? '编辑部门' : '添加部门'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="id" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="parentId" hidden>
            <InputNumber />
          </Form.Item>
          <Form.Item
            name="deptName"
            label="部门名称"
            rules={[{ required: true, message: '请输入部门名称' }]}
          >
            <Input placeholder="请输入部门名称" />
          </Form.Item>
          <Form.Item name="deptCode" label="部门编码">
            <Input placeholder="请输入部门编码" />
          </Form.Item>
          <Form.Item name="leader" label="负责人">
            <Input placeholder="请输入负责人" />
          </Form.Item>
          <Form.Item
            name="phone"
            label="联系电话"
            rules={[
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' },
            ]}
          >
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item
            name="email"
            label="邮箱"
            rules={[{ type: 'email', message: '请输入正确的邮箱格式' }]}
          >
            <Input placeholder="请输入邮箱" />
          </Form.Item>
          <Form.Item name="sortOrder" label="排序">
            <InputNumber min={0} placeholder="请输入排序" style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
            <Select placeholder="请选择状态">
              <Select.Option value={1}>启用</Select.Option>
              <Select.Option value={0}>禁用</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default DepartmentList;
