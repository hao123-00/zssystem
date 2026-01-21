import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, InputNumber, DatePicker, Select, message } from 'antd';
import dayjs from 'dayjs';
import {
  ProductionOrderInfo,
  ProductionOrderSaveParams,
  createOrder,
  updateOrder,
  getOrderById,
} from '@/api/production';

interface OrderModalProps {
  visible: boolean;
  order: ProductionOrderInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const OrderModal: React.FC<OrderModalProps> = ({ visible, order, onCancel, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (visible) {
      if (order) {
        loadOrderDetail(order.id);
      } else {
        form.resetFields();
        form.setFieldsValue({
          status: 0,
        });
      }
    }
  }, [visible, order]);

  const loadOrderDetail = async (id: number) => {
    try {
      const orderDetail = await getOrderById(id);
      form.setFieldsValue({
        id: orderDetail.id,
        orderNo: orderDetail.orderNo,
        customerName: orderDetail.customerName,
        productName: orderDetail.productName,
        productCode: orderDetail.productCode,
        quantity: orderDetail.quantity,
        deliveryDate: orderDetail.deliveryDate ? dayjs(orderDetail.deliveryDate) : null,
        status: orderDetail.status,
        remark: orderDetail.remark,
      });
    } catch (error: any) {
      message.error('加载订单详情失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const data: ProductionOrderSaveParams = {
        ...values,
        deliveryDate: values.deliveryDate ? values.deliveryDate.format('YYYY-MM-DD') : undefined,
      };

      if (order) {
        await updateOrder(order.id, data);
        message.success('更新成功');
      } else {
        await createOrder(data);
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
      title={order ? '编辑订单' : '新增订单'}
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
        {order && (
          <Form.Item name="orderNo" label="订单编号">
            <Input disabled />
          </Form.Item>
        )}
        <Form.Item name="customerName" label="客户名称">
          <Input placeholder="请输入客户名称" />
        </Form.Item>
        <Form.Item
          name="productName"
          label="产品名称"
          rules={[{ required: true, message: '请输入产品名称' }]}
        >
          <Input placeholder="请输入产品名称" />
        </Form.Item>
        <Form.Item name="productCode" label="产品编码">
          <Input placeholder="请输入产品编码" />
        </Form.Item>
        <Form.Item
          name="quantity"
          label="订单数量"
          rules={[{ required: true, message: '请输入订单数量' }]}
        >
          <InputNumber min={1} placeholder="请输入订单数量" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="deliveryDate" label="交期">
          <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
        </Form.Item>
        <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
          <Select placeholder="请选择状态">
            <Select.Option value={0}>待生产</Select.Option>
            <Select.Option value={1}>生产中</Select.Option>
            <Select.Option value={2}>已完成</Select.Option>
            <Select.Option value={3}>已取消</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={3} placeholder="请输入备注" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default OrderModal;
