import React, { useEffect } from 'react';
import { Modal, Form, Input, InputNumber, DatePicker, message } from 'antd';
import { Site5sCheckInfo, Site5sCheckSaveParams, saveSite5sCheck } from '@/api/site5s';
import dayjs from 'dayjs';

interface CheckModalProps {
  visible: boolean;
  editingCheck: Site5sCheckInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const CheckModal: React.FC<CheckModalProps> = ({
  visible,
  editingCheck,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (visible) {
      if (editingCheck) {
        form.setFieldsValue({
          ...editingCheck,
          checkDate: editingCheck.checkDate ? dayjs(editingCheck.checkDate) : undefined,
        });
      } else {
        form.resetFields();
      }
    }
  }, [visible, editingCheck, form]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const params: Site5sCheckSaveParams = {
        ...values,
        id: editingCheck?.id,
        checkDate: values.checkDate ? values.checkDate.format('YYYY-MM-DD') : undefined,
      };
      await saveSite5sCheck(params);
      message.success(editingCheck ? '更新成功' : '新增成功');
      onSuccess();
    } catch (error: any) {
      if (error.errorFields) {
        return;
      }
      message.error(error.message || (editingCheck ? '更新失败' : '新增失败'));
    }
  };

  // 计算总分
  const calculateTotal = () => {
    const sortScore = form.getFieldValue('sortScore') || 0;
    const setScore = form.getFieldValue('setScore') || 0;
    const shineScore = form.getFieldValue('shineScore') || 0;
    const standardizeScore = form.getFieldValue('standardizeScore') || 0;
    const sustainScore = form.getFieldValue('sustainScore') || 0;
    return sortScore + setScore + shineScore + standardizeScore + sustainScore;
  };

  return (
    <Modal
      title={editingCheck ? '编辑5S检查记录' : '新增5S检查记录'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      width={800}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          sortScore: 0,
          setScore: 0,
          shineScore: 0,
          standardizeScore: 0,
          sustainScore: 0,
        }}
      >
        <Form.Item
          name="checkDate"
          label="检查日期"
          rules={[{ required: true, message: '请选择检查日期' }]}
        >
          <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
        </Form.Item>

        <Form.Item
          name="checkArea"
          label="检查区域"
          rules={[{ required: true, message: '请输入检查区域' }]}
        >
          <Input placeholder="请输入检查区域" />
        </Form.Item>

        <Form.Item
          name="checkerName"
          label="检查人员"
        >
          <Input placeholder="请输入检查人员" />
        </Form.Item>

        <div style={{ marginBottom: 16 }}>
          <h4>5S评分（每项满分20分）</h4>
        </div>

        <Form.Item
          name="sortScore"
          label="整理得分"
          rules={[
            { type: 'number', min: 0, max: 20, message: '得分必须在0-20之间' },
          ]}
        >
          <InputNumber
            style={{ width: '100%' }}
            min={0}
            max={20}
            placeholder="请输入整理得分（0-20）"
            onChange={() => {
              const total = calculateTotal();
              form.setFieldValue('totalScore', total);
            }}
          />
        </Form.Item>

        <Form.Item
          name="setScore"
          label="整顿得分"
          rules={[
            { type: 'number', min: 0, max: 20, message: '得分必须在0-20之间' },
          ]}
        >
          <InputNumber
            style={{ width: '100%' }}
            min={0}
            max={20}
            placeholder="请输入整顿得分（0-20）"
            onChange={() => {
              const total = calculateTotal();
              form.setFieldValue('totalScore', total);
            }}
          />
        </Form.Item>

        <Form.Item
          name="shineScore"
          label="清扫得分"
          rules={[
            { type: 'number', min: 0, max: 20, message: '得分必须在0-20之间' },
          ]}
        >
          <InputNumber
            style={{ width: '100%' }}
            min={0}
            max={20}
            placeholder="请输入清扫得分（0-20）"
            onChange={() => {
              const total = calculateTotal();
              form.setFieldValue('totalScore', total);
            }}
          />
        </Form.Item>

        <Form.Item
          name="standardizeScore"
          label="清洁得分"
          rules={[
            { type: 'number', min: 0, max: 20, message: '得分必须在0-20之间' },
          ]}
        >
          <InputNumber
            style={{ width: '100%' }}
            min={0}
            max={20}
            placeholder="请输入清洁得分（0-20）"
            onChange={() => {
              const total = calculateTotal();
              form.setFieldValue('totalScore', total);
            }}
          />
        </Form.Item>

        <Form.Item
          name="sustainScore"
          label="素养得分"
          rules={[
            { type: 'number', min: 0, max: 20, message: '得分必须在0-20之间' },
          ]}
        >
          <InputNumber
            style={{ width: '100%' }}
            min={0}
            max={20}
            placeholder="请输入素养得分（0-20）"
            onChange={() => {
              const total = calculateTotal();
              form.setFieldValue('totalScore', total);
            }}
          />
        </Form.Item>

        <Form.Item
          name="totalScore"
          label="总分（自动计算）"
        >
          <InputNumber
            style={{ width: '100%' }}
            disabled
            value={calculateTotal()}
          />
        </Form.Item>

        <Form.Item
          name="problemDescription"
          label="问题描述"
        >
          <Input.TextArea
            rows={4}
            placeholder="请输入问题描述"
            maxLength={1000}
            showCount
          />
        </Form.Item>

        <Form.Item
          name="remark"
          label="备注"
        >
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

export default CheckModal;
