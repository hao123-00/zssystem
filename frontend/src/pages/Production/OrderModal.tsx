import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, InputNumber, Select, message, Card, Button, Space } from 'antd';
import { PlusOutlined, MinusCircleOutlined } from '@ant-design/icons';
import {
  ProductionOrderInfo,
  ProductionOrderSaveParams,
  ProductInfo,
  createOrder,
  updateOrder,
  getOrderById,
} from '@/api/production';
import { getEquipmentList } from '@/api/equipment';

interface OrderModalProps {
  visible: boolean;
  order: ProductionOrderInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const OrderModal: React.FC<OrderModalProps> = ({ visible, order, onCancel, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [equipmentList, setEquipmentList] = useState<any[]>([]);

  useEffect(() => {
    if (visible) {
      loadEquipmentList();
      if (order) {
        loadOrderDetail(order.id);
      } else {
        form.resetFields();
        // 初始化一个产品
        form.setFieldsValue({
          products: [{ productName: '', orderQuantity: undefined, dailyCapacity: undefined }],
        });
      }
    }
  }, [visible, order]);

  const loadEquipmentList = async () => {
    try {
      const response = await getEquipmentList({ pageNum: 1, pageSize: 1000 });
      setEquipmentList(response.list || []);
    } catch (error: any) {
      console.error('加载设备列表失败', error);
    }
  };

  const loadOrderDetail = async (id: number) => {
    try {
      const orderDetail = await getOrderById(id);
      form.setFieldsValue({
        id: orderDetail.id,
        orderNo: orderDetail.orderNo,
        machineNo: orderDetail.machineNo,
        products: orderDetail.products || [],
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

      // 确保 machineNo 是字符串，不是数组
      const machineNo = Array.isArray(values.machineNo) 
        ? values.machineNo[0] || '' 
        : values.machineNo || '';

      // 验证产品列表
      if (!values.products || values.products.length === 0) {
        message.error('请至少添加一个产品');
        return;
      }
      if (values.products.length > 3) {
        message.error('最多只能添加3个产品');
        return;
      }

      // 过滤掉空的产品
      const validProducts = values.products.filter(
        (p: ProductInfo) => p.productName && p.orderQuantity && p.dailyCapacity
      );

      if (validProducts.length === 0) {
        message.error('请至少添加一个有效的产品');
        return;
      }

      const data: ProductionOrderSaveParams = {
        ...values,
        machineNo: machineNo,
        products: validProducts,
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

  // 获取机台号选项（从设备列表中提取唯一的机台号）
  const machineNoOptions = Array.from(
    new Set(equipmentList.filter((eq) => eq.machineNo).map((eq) => eq.machineNo))
  ).map((machineNo) => ({
    value: machineNo,
    label: machineNo,
  }));

  return (
    <Modal
      title={order ? '编辑订单' : '新增订单'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      confirmLoading={loading}
      width={900}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
      >
        <Form.Item name="id" hidden>
          <Input />
        </Form.Item>
        {order && (
          <Form.Item name="orderNo" label="订单编号">
            <Input disabled />
          </Form.Item>
        )}
        <Form.Item
          name="machineNo"
          label="机台号"
          rules={[{ required: true, message: '请输入或选择机台号' }]}
        >
          <Select
            placeholder="请选择或输入机台号"
            showSearch
            allowClear
            options={machineNoOptions}
            filterOption={(input, option) =>
              (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
            }
            mode={undefined}
          />
        </Form.Item>

        <Form.Item label="产品信息">
          <Form.List name="products" initialValue={[{ productName: '', orderQuantity: undefined, dailyCapacity: undefined }]}>
            {(fields, { add, remove }) => (
              <>
                {fields.map(({ key, name, ...restField }, index) => (
                  <Card
                    key={key}
                    size="small"
                    title={`产品 ${index + 1}`}
                    style={{ marginBottom: 16 }}
                    extra={
                      fields.length > 1 ? (
                        <Button
                          type="link"
                          danger
                          icon={<MinusCircleOutlined />}
                          onClick={() => remove(name)}
                        >
                          删除
                        </Button>
                      ) : null
                    }
                  >
                    <Space direction="vertical" style={{ width: '100%' }} size="small">
                      <Form.Item
                        {...restField}
                        name={[name, 'productName']}
                        label="产品名称"
                        rules={[{ required: true, message: '请输入产品名称' }]}
                      >
                        <Input placeholder="请输入产品名称" />
                      </Form.Item>
                      <Form.Item
                        {...restField}
                        name={[name, 'productCode']}
                        label="产品编码"
                      >
                        <Input placeholder="请输入产品编码（可选）" />
                      </Form.Item>
                      <Space style={{ width: '100%' }}>
                        <Form.Item
                          {...restField}
                          name={[name, 'orderQuantity']}
                          label="订单数量"
                          rules={[{ required: true, message: '请输入订单数量' }]}
                          style={{ flex: 1 }}
                        >
                          <InputNumber min={1} placeholder="订单数量" style={{ width: '100%' }} />
                        </Form.Item>
                        <Form.Item
                          {...restField}
                          name={[name, 'dailyCapacity']}
                          label="产能"
                          rules={[{ required: true, message: '请输入产能' }]}
                          style={{ flex: 1 }}
                        >
                          <InputNumber min={1} placeholder="日产能" style={{ width: '100%' }} />
                        </Form.Item>
                      </Space>
                    </Space>
                  </Card>
                ))}
                {fields.length < 3 && (
                  <Button
                    type="dashed"
                    onClick={() => add()}
                    block
                    icon={<PlusOutlined />}
                  >
                    添加产品（最多3个）
                  </Button>
                )}
              </>
            )}
          </Form.List>
        </Form.Item>

        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={3} placeholder="请输入备注" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default OrderModal;
