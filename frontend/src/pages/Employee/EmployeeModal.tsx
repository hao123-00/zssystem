import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, Select, DatePicker, InputNumber, message } from 'antd';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { EmployeeInfo, EmployeeSaveParams, createEmployee, updateEmployee, getEmployeeById } from '@/api/employee';
import { DepartmentTreeVO } from '@/api/department';

interface EmployeeModalProps {
  visible: boolean;
  employee: EmployeeInfo | null;
  departmentTree: DepartmentTreeVO[];
  onCancel: () => void;
  onSuccess: () => void;
}

const EmployeeModal: React.FC<EmployeeModalProps> = ({
  visible,
  employee,
  departmentTree,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (visible) {
      if (employee) {
        loadEmployeeDetail(employee.id);
      } else {
        form.resetFields();
        form.setFieldsValue({
          status: 1,
        });
      }
    }
  }, [visible, employee]);

  const loadEmployeeDetail = async (id: number) => {
    try {
      const employeeDetail = await getEmployeeById(id);
      form.setFieldsValue({
        id: employeeDetail.id,
        employeeNo: employeeDetail.employeeNo,
        name: employeeDetail.name,
        gender: employeeDetail.gender,
        age: employeeDetail.age,
        phone: employeeDetail.phone,
        email: employeeDetail.email,
        departmentId: employeeDetail.departmentId,
        position: employeeDetail.position,
        entryDate: employeeDetail.entryDate ? dayjs(employeeDetail.entryDate) : null,
        status: employeeDetail.status,
      });
    } catch (error: any) {
      message.error('加载员工详情失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const data: EmployeeSaveParams = {
        ...values,
        entryDate: values.entryDate ? values.entryDate.format('YYYY-MM-DD') : undefined,
      };

      if (employee) {
        await updateEmployee(employee.id, data);
        message.success('更新成功');
      } else {
        await createEmployee(data);
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

  return (
    <Modal
      title={employee ? '编辑员工' : '新增员工'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      confirmLoading={loading}
      width={700}
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
          name="employeeNo"
          label="工号"
          rules={[{ required: true, message: '请输入工号' }]}
        >
          <Input placeholder="请输入工号" disabled={!!employee} />
        </Form.Item>
        <Form.Item
          name="name"
          label="姓名"
          rules={[{ required: true, message: '请输入姓名' }]}
        >
          <Input placeholder="请输入姓名" />
        </Form.Item>
        <Form.Item name="gender" label="性别">
          <Select placeholder="请选择性别">
            <Select.Option value={1}>男</Select.Option>
            <Select.Option value={0}>女</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="age" label="年龄">
          <InputNumber min={1} max={150} placeholder="请输入年龄" style={{ width: '100%' }} />
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
          name="email"
          label="邮箱"
          rules={[{ type: 'email', message: '请输入正确的邮箱格式' }]}
        >
          <Input placeholder="请输入邮箱" />
        </Form.Item>
        <Form.Item
          name="departmentId"
          label="部门"
          rules={[{ required: true, message: '请选择部门' }]}
        >
          <Select placeholder="请选择部门" showSearch optionFilterProp="children">
            {flattenDepartments(departmentTree).map((dept) => (
              <Select.Option key={dept.id} value={dept.id}>
                {dept.deptName}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item name="position" label="职位">
          <Input placeholder="请输入职位" />
        </Form.Item>
        <Form.Item name="entryDate" label="入职日期">
          <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
        </Form.Item>
        <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
          <Select placeholder="请选择状态">
            <Select.Option value={1}>在职</Select.Option>
            <Select.Option value={0}>离职</Select.Option>
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default EmployeeModal;
