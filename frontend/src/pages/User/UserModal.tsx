import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, Select, message } from 'antd';
import { UserInfo, UserSaveParams, createUser, updateUser, getUserById } from '@/api/user';
import { getAllRoles, RoleInfo } from '@/api/role';

interface UserModalProps {
  visible: boolean;
  user: UserInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const UserModal: React.FC<UserModalProps> = ({ visible, user, onCancel, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [roleList, setRoleList] = useState<RoleInfo[]>([]);

  useEffect(() => {
    if (visible) {
      loadRoles();
      if (user) {
        loadUserDetail(user.id);
      } else {
        form.resetFields();
        form.setFieldsValue({
          status: 1,
        });
      }
    }
  }, [visible, user]);

  const loadRoles = async () => {
    try {
      const roles = await getAllRoles();
      setRoleList(roles.filter((r) => r.status === 1));
    } catch (error: any) {
      message.error('加载角色列表失败');
    }
  };

  const loadUserDetail = async (id: number) => {
    try {
      const userDetail = await getUserById(id);
      form.setFieldsValue({
        ...userDetail,
        realName: userDetail.name || userDetail.realName,
        roleIds: userDetail.roles?.map((r) => r.id) || [],
      });
    } catch (error: any) {
      message.error('加载用户详情失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const data: UserSaveParams = {
        ...values,
      };

      if (user) {
        // 更新
        await updateUser(user.id, data);
        message.success('更新成功');
      } else {
        // 新增
        await createUser(data);
        message.success('创建成功');
      }

      onSuccess();
    } catch (error: any) {
      if (error.errorFields) {
        // 表单验证错误
        return;
      }
      message.error(error.message || '操作失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={user ? '编辑用户' : '新增用户'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      confirmLoading={loading}
      width={600}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          status: 1,
        }}
      >
        {!user && (
          <>
            <Form.Item
              name="username"
              label="用户名"
              rules={[
                { required: true, message: '请输入用户名' },
                { min: 3, message: '用户名长度不能少于3位' },
              ]}
            >
              <Input placeholder="请输入用户名" />
            </Form.Item>
            <Form.Item
              name="password"
              label="密码"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 6, message: '密码长度不能少于6位' },
              ]}
            >
              <Input.Password placeholder="请输入密码" />
            </Form.Item>
          </>
        )}

        {user && (
          <Form.Item
            name="password"
            label="密码"
            help="不填写则不修改密码"
          >
            <Input.Password placeholder="不填写则不修改密码" />
          </Form.Item>
        )}

        <Form.Item
          name="realName"
          label="真实姓名"
          rules={[{ required: true, message: '请输入真实姓名' }]}
        >
          <Input placeholder="请输入真实姓名" />
        </Form.Item>

        <Form.Item
          name="email"
          label="邮箱"
          rules={[
            { type: 'email', message: '请输入正确的邮箱格式' },
          ]}
        >
          <Input placeholder="请输入邮箱" />
        </Form.Item>

        <Form.Item
          name="phone"
          label="手机号"
          rules={[
            { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' },
          ]}
        >
          <Input placeholder="请输入手机号" />
        </Form.Item>

        <Form.Item
          name="status"
          label="状态"
          rules={[{ required: true, message: '请选择状态' }]}
        >
          <Select placeholder="请选择状态">
            <Select.Option value={1}>启用</Select.Option>
            <Select.Option value={0}>禁用</Select.Option>
          </Select>
        </Form.Item>

        <Form.Item
          name="roleIds"
          label="角色"
        >
          <Select
            mode="multiple"
            placeholder="请选择角色"
            allowClear
            showSearch
            optionFilterProp="children"
          >
            {roleList.map((role) => (
              <Select.Option key={role.id} value={role.id}>
                {role.roleName}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default UserModal;
