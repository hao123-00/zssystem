import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, InputNumber, DatePicker, Select, message } from 'antd';
import dayjs from 'dayjs';
import {
  ProductionPlanInfo,
  ProductionPlanSaveParams,
  createPlan,
  updatePlan,
  getPlanById,
} from '@/api/production';

interface PlanModalProps {
  visible: boolean;
  plan: ProductionPlanInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const PlanModal: React.FC<PlanModalProps> = ({ visible, plan, onCancel, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (visible) {
      if (plan) {
        loadPlanDetail(plan.id);
      } else {
        form.resetFields();
        form.setFieldsValue({
          status: 0,
        });
      }
    }
  }, [visible, plan]);

  const loadPlanDetail = async (id: number) => {
    try {
      const planDetail = await getPlanById(id);
      form.setFieldsValue({
        id: planDetail.id,
        planNo: planDetail.planNo,
        orderId: planDetail.orderId,
        equipmentId: planDetail.equipmentId,
        moldId: planDetail.moldId,
        operatorId: planDetail.operatorId,
        planStartTime: planDetail.planStartTime ? dayjs(planDetail.planStartTime) : null,
        planEndTime: planDetail.planEndTime ? dayjs(planDetail.planEndTime) : null,
        planQuantity: planDetail.planQuantity,
        status: planDetail.status,
        remark: planDetail.remark,
      });
    } catch (error: any) {
      message.error('加载计划详情失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const data: ProductionPlanSaveParams = {
        ...values,
        planStartTime: values.planStartTime ? values.planStartTime.format('YYYY-MM-DD HH:mm:ss') : undefined,
        planEndTime: values.planEndTime ? values.planEndTime.format('YYYY-MM-DD HH:mm:ss') : undefined,
      };

      if (plan) {
        await updatePlan(plan.id, data);
        message.success('更新成功');
      } else {
        await createPlan(data);
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
      title={plan ? '编辑计划' : '新增计划'}
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
          status: 0,
        }}
      >
        <Form.Item name="id" hidden>
          <Input />
        </Form.Item>
        {plan && (
          <Form.Item name="planNo" label="计划编号">
            <Input disabled />
          </Form.Item>
        )}
        <Form.Item
          name="orderId"
          label="订单ID"
          rules={[{ required: true, message: '请输入订单ID' }]}
        >
          <InputNumber min={1} placeholder="请输入订单ID" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="equipmentId" label="设备ID">
          <InputNumber min={1} placeholder="请输入设备ID" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="moldId" label="模具ID">
          <InputNumber min={1} placeholder="请输入模具ID" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="operatorId" label="操作员ID">
          <InputNumber min={1} placeholder="请输入操作员ID" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="planStartTime" label="计划开始时间">
          <DatePicker showTime style={{ width: '100%' }} format="YYYY-MM-DD HH:mm:ss" />
        </Form.Item>
        <Form.Item name="planEndTime" label="计划结束时间">
          <DatePicker showTime style={{ width: '100%' }} format="YYYY-MM-DD HH:mm:ss" />
        </Form.Item>
        <Form.Item
          name="planQuantity"
          label="计划数量"
          rules={[{ required: true, message: '请输入计划数量' }]}
        >
          <InputNumber min={1} placeholder="请输入计划数量" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
          <Select placeholder="请选择状态">
            <Select.Option value={0}>待执行</Select.Option>
            <Select.Option value={1}>执行中</Select.Option>
            <Select.Option value={2}>已完成</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={3} placeholder="请输入备注" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default PlanModal;
