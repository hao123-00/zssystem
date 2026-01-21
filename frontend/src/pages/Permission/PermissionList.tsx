import React, { useEffect, useState } from 'react';
import { Tree, Button, Space, message, Popconfirm, Modal, Form, Input, Select, InputNumber } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  getPermissionTree,
  createPermission,
  updatePermission,
  deletePermission,
  PermissionTreeVO,
  PermissionSaveParams,
} from '@/api/permission';
import './PermissionList.less';

const PermissionList: React.FC = () => {
  const [form] = Form.useForm();
  const [treeData, setTreeData] = useState<PermissionTreeVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingPermission, setEditingPermission] = useState<PermissionTreeVO | null>(null);
  const [selectedKeys, setSelectedKeys] = useState<React.Key[]>([]);
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);

  const fetchTree = async () => {
    setLoading(true);
    try {
      const response = await getPermissionTree();
      setTreeData(response || []);
      // 默认展开所有节点
      const keys: React.Key[] = [];
      const collectKeys = (nodes: PermissionTreeVO[]) => {
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
      permissionType: 1,
      status: 1,
      sortOrder: 0,
    });
    setEditingPermission(null);
    setModalVisible(true);
  };

  const handleEdit = (permission: PermissionTreeVO) => {
    form.resetFields();
    form.setFieldsValue({
      id: permission.id,
      parentId: permission.parentId,
      permissionCode: permission.permissionCode,
      permissionName: permission.permissionName,
      permissionType: permission.permissionType,
      path: permission.path,
      component: permission.component,
      icon: permission.icon,
      sortOrder: permission.sortOrder,
      status: permission.status,
    });
    setEditingPermission(permission);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deletePermission(id);
      message.success('删除成功');
      fetchTree();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const params: PermissionSaveParams = {
        ...values,
      };
      if (editingPermission) {
        await updatePermission(editingPermission.id, params);
        message.success('更新成功');
      } else {
        await createPermission(params);
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

  const convertToTreeData = (nodes: PermissionTreeVO[]): any[] => {
    return nodes.map((node) => ({
      key: node.id,
      title: (
        <div className="tree-node-title">
          <span>{node.permissionName}</span>
          <span className="tree-node-code">({node.permissionCode})</span>
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
              添加子权限
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
              title="确定要删除这个权限吗？"
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
    <div className="permission-list-container">
      <div className="toolbar">
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>
            添加根权限
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
        title={editingPermission ? '编辑权限' : '添加权限'}
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
            name="permissionCode"
            label="权限编码"
            rules={[{ required: true, message: '请输入权限编码' }]}
          >
            <Input placeholder="请输入权限编码" />
          </Form.Item>
          <Form.Item
            name="permissionName"
            label="权限名称"
            rules={[{ required: true, message: '请输入权限名称' }]}
          >
            <Input placeholder="请输入权限名称" />
          </Form.Item>
          <Form.Item
            name="permissionType"
            label="权限类型"
            rules={[{ required: true, message: '请选择权限类型' }]}
          >
            <Select placeholder="请选择权限类型">
              <Select.Option value={1}>菜单</Select.Option>
              <Select.Option value={2}>按钮</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="path" label="路由路径">
            <Input placeholder="请输入路由路径" />
          </Form.Item>
          <Form.Item name="component" label="组件路径">
            <Input placeholder="请输入组件路径" />
          </Form.Item>
          <Form.Item name="icon" label="图标">
            <Input placeholder="请输入图标名称" />
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

export default PermissionList;
