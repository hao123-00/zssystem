import React, { useEffect } from 'react';
import { Modal, Form, Input, DatePicker, Select, message } from 'antd';
import {
  Site5sRectificationInfo,
  Site5sRectificationSaveParams,
  saveSite5sRectification,
} from '@/api/site5s';
import dayjs from 'dayjs';
import { useResponsive } from '@/hooks/useResponsive';

interface RectificationModalProps {
  visible: boolean;
  editingRectification: Site5sRectificationInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const RectificationModal: React.FC<RectificationModalProps> = ({
  visible,
  editingRectification,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm();
  const { isMobile } = useResponsive();

  useEffect(() => {
    if (visible) {
      if (editingRectification) {
        form.setFieldsValue({
          ...editingRectification,
          deadline: editingRectification.deadline ? dayjs(editingRectification.deadline) : undefined,
          rectificationDate: editingRectification.rectificationDate
            ? dayjs(editingRectification.rectificationDate)
            : undefined,
          verificationDate: editingRectification.verificationDate
            ? dayjs(editingRectification.verificationDate)
            : undefined,
        });
      } else {
        form.resetFields();
        form.setFieldsValue({ status: 0 });
      }
    }
  }, [visible, editingRectification, form]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const params: Site5sRectificationSaveParams = {
        ...values,
        id: editingRectification?.id,
        checkId: editingRectification?.checkId,
        deadline: values.deadline ? values.deadline.format('YYYY-MM-DD') : undefined,
        rectificationDate: values.rectificationDate
          ? values.rectificationDate.format('YYYY-MM-DD')
          : undefined,
        verificationDate: values.verificationDate
          ? values.verificationDate.format('YYYY-MM-DD')
          : undefined,
      };
      await saveSite5sRectification(params);
      message.success(editingRectification ? '更新成功' : '新增成功');
      onSuccess();
    } catch (error: any) {
      if (error.errorFields) {
        return;
      }
      message.error(error.message || (editingRectification ? '更新失败' : '新增失败'));
    }
  };

  return (
    <Modal
      title={editingRectification ? '编辑整改任务' : '新增整改任务'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      width={isMobile ? '100%' : 800}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="problemDescription"
          label="问题描述"
          rules={[{ required: true, message: '请输入问题描述' }]}
        >
          <Input.TextArea
            rows={4}
            placeholder="请输入问题描述"
            maxLength={1000}
            showCount
          />
        </Form.Item>

        <Form.Item
          name="area"
          label="区域"
          rules={[{ required: true, message: '请输入区域' }]}
        >
          <Input placeholder="请输入区域" />
        </Form.Item>

        <Form.Item name="department" label="责任部门">
          <Input placeholder="请输入责任部门" />
        </Form.Item>

        <Form.Item name="responsiblePerson" label="责任人">
          <Input placeholder="请输入责任人" />
        </Form.Item>

        <Form.Item name="deadline" label="整改期限">
          <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
        </Form.Item>

        <Form.Item name="status" label="状态">
          <Select>
            <Select.Option value={0}>待整改</Select.Option>
            <Select.Option value={1}>整改中</Select.Option>
            <Select.Option value={2}>待验证</Select.Option>
            <Select.Option value={3}>已完成</Select.Option>
          </Select>
        </Form.Item>

        <Form.Item name="rectificationContent" label="整改内容">
          <Input.TextArea
            rows={4}
            placeholder="请输入整改内容"
            maxLength={1000}
            showCount
          />
        </Form.Item>

        <Form.Item name="rectificationDate" label="整改日期">
          <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
        </Form.Item>

        <Form.Item name="verifierName" label="验证人员">
          <Input placeholder="请输入验证人员" />
        </Form.Item>

        <Form.Item name="verificationDate" label="验证日期">
          <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
        </Form.Item>

        <Form.Item name="verificationResult" label="验证结果">
          <Input.TextArea
            rows={3}
            placeholder="请输入验证结果"
            maxLength={500}
            showCount
          />
        </Form.Item>

        <Form.Item name="remark" label="备注">
          <Input.TextArea
            rows={3}
            placeholder="请输入备注"
            maxLength={500}
            showCount
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default RectificationModal;
