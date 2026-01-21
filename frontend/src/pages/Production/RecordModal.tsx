import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, InputNumber, DatePicker, message } from 'antd';
import dayjs from 'dayjs';
import {
  ProductionRecordInfo,
  ProductionRecordSaveParams,
  createRecord,
  updateRecord,
  getRecordById,
} from '@/api/production';

interface RecordModalProps {
  visible: boolean;
  record: ProductionRecordInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const RecordModal: React.FC<RecordModalProps> = ({ visible, record, onCancel, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (visible) {
      if (record) {
        loadRecordDetail(record.id);
      } else {
        form.resetFields();
        form.setFieldsValue({
          productionDate: dayjs(),
          defectQuantity: 0,
        });
      }
    }
  }, [visible, record]);

  const loadRecordDetail = async (id: number) => {
    try {
      const recordDetail = await getRecordById(id);
      form.setFieldsValue({
        id: recordDetail.id,
        recordNo: recordDetail.recordNo,
        orderId: recordDetail.orderId,
        planId: recordDetail.planId,
        equipmentId: recordDetail.equipmentId,
        moldId: recordDetail.moldId,
        operatorId: recordDetail.operatorId,
        productionDate: recordDetail.productionDate ? dayjs(recordDetail.productionDate) : null,
        startTime: recordDetail.startTime ? dayjs(recordDetail.startTime) : null,
        endTime: recordDetail.endTime ? dayjs(recordDetail.endTime) : null,
        quantity: recordDetail.quantity,
        defectQuantity: recordDetail.defectQuantity,
        remark: recordDetail.remark,
      });
    } catch (error: any) {
      message.error('加载记录详情失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const data: ProductionRecordSaveParams = {
        ...values,
        productionDate: values.productionDate ? values.productionDate.format('YYYY-MM-DD') : undefined,
        startTime: values.startTime ? values.startTime.format('YYYY-MM-DD HH:mm:ss') : undefined,
        endTime: values.endTime ? values.endTime.format('YYYY-MM-DD HH:mm:ss') : undefined,
      };

      if (record) {
        await updateRecord(record.id, data);
        message.success('更新成功');
      } else {
        await createRecord(data);
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
      title={record ? '编辑记录' : '新增记录'}
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
          defectQuantity: 0,
        }}
      >
        <Form.Item name="id" hidden>
          <Input />
        </Form.Item>
        {record && (
          <Form.Item name="recordNo" label="记录编号">
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
        <Form.Item name="planId" label="计划ID">
          <InputNumber min={1} placeholder="请输入计划ID" style={{ width: '100%' }} />
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
        <Form.Item
          name="productionDate"
          label="生产日期"
          rules={[{ required: true, message: '请选择生产日期' }]}
        >
          <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
        </Form.Item>
        <Form.Item name="startTime" label="开始时间">
          <DatePicker showTime style={{ width: '100%' }} format="YYYY-MM-DD HH:mm:ss" />
        </Form.Item>
        <Form.Item name="endTime" label="结束时间">
          <DatePicker showTime style={{ width: '100%' }} format="YYYY-MM-DD HH:mm:ss" />
        </Form.Item>
        <Form.Item
          name="quantity"
          label="产量"
          rules={[{ required: true, message: '请输入产量' }]}
        >
          <InputNumber min={0} placeholder="请输入产量" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="defectQuantity" label="不良品数量">
          <InputNumber min={0} placeholder="请输入不良品数量" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={3} placeholder="请输入备注" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default RecordModal;
