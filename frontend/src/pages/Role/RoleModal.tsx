import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, Select, message } from 'antd';
import { RoleInfo, RoleSaveParams, createRole, updateRole, getRoleById } from '@/api/role';
import { getPermissionTree, PermissionTreeVO } from '@/api/permission';
import PermissionTree from '@/components/PermissionTree/PermissionTree';
import { useResponsive } from '@/hooks/useResponsive';

interface RoleModalProps {
  visible: boolean;
  role: RoleInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const RoleModal: React.FC<RoleModalProps> = ({ visible, role, onCancel, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [permissionTree, setPermissionTree] = useState<PermissionTreeVO[]>([]);
  const [selectedPermissions, setSelectedPermissions] = useState<number[]>([]);
  const { isMobile } = useResponsive();

  useEffect(() => {
    if (visible) {
      loadPermissionTree();
      if (role) {
        loadRoleDetail(role.id);
      } else {
        form.resetFields();
        form.setFieldsValue({
          status: 1,
        });
        setSelectedPermissions([]);
      }
    }
  }, [visible, role]);

  const loadPermissionTree = async () => {
    try {
      const tree = await getPermissionTree();
      setPermissionTree(tree || []);
    } catch (error: any) {
      message.error('加载权限树失败');
    }
  };

  const loadRoleDetail = async (id: number) => {
    try {
      const roleDetail = await getRoleById(id);
      form.setFieldsValue({
        id: roleDetail.id,
        roleCode: roleDetail.roleCode,
        roleName: roleDetail.roleName,
        description: roleDetail.description,
        status: roleDetail.status,
      });
      setSelectedPermissions(roleDetail.permissionIds || []);
    } catch (error: any) {
      message.error('加载角色详情失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const data: RoleSaveParams = {
        ...values,
        permissionIds: selectedPermissions,
      };

      if (role) {
        await updateRole(role.id, data);
        message.success('更新成功');
      } else {
        await createRole(data);
        message.success('创建成功');
      }

      onSuccess();
    } catch (error: any) {
      if (error.errorFields) {
        return;
      }
      message.error(error.message || '操作失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={role ? '编辑角色' : '新增角色'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      confirmLoading={loading}
      width={isMobile ? '100%' : 700}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          status: 1,
        }}
      >
        <Form.Item name="id" hidden>
          <Input />
        </Form.Item>
        <Form.Item
          name="roleCode"
          label="角色编码"
          rules={[{ required: true, message: '请输入角色编码' }]}
        >
          <Input placeholder="请输入角色编码" disabled={!!role} />
        </Form.Item>
        <Form.Item
          name="roleName"
          label="角色名称"
          rules={[{ required: true, message: '请输入角色名称' }]}
        >
          <Input placeholder="请输入角色名称" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={3} placeholder="请输入角色描述" />
        </Form.Item>
        <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
          <Select placeholder="请选择状态">
            <Select.Option value={1}>启用</Select.Option>
            <Select.Option value={0}>禁用</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item label="权限分配">
          <PermissionTree
            value={selectedPermissions}
            onChange={setSelectedPermissions}
            treeData={permissionTree}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default RoleModal;
