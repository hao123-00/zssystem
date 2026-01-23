import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, InputNumber, DatePicker, Select, message } from 'antd';
import dayjs from 'dayjs';
import {
  EquipmentInfo,
  EquipmentSaveParams,
  createEquipment,
  updateEquipment,
  getEquipmentById,
} from '@/api/equipment';

interface EquipmentModalProps {
  visible: boolean;
  equipment: EquipmentInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const EquipmentModal: React.FC<EquipmentModalProps> = ({
  visible,
  equipment,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (visible) {
      if (equipment) {
        loadEquipmentDetail(equipment.id);
      } else {
        form.resetFields();
        form.setFieldsValue({
          status: 1,
        });
      }
    }
  }, [visible, equipment]);

  const loadEquipmentDetail = async (id: number) => {
    try {
      const detail = await getEquipmentById(id);
      const enableDateValue = detail.enableDate ? dayjs(detail.enableDate) : null;
      form.setFieldsValue({
        ...detail,
        purchaseDate: detail.purchaseDate ? dayjs(detail.purchaseDate) : null,
        enableDate: enableDateValue,
      });
      
      // 如果启用日期存在，自动计算使用年限
      if (enableDateValue) {
        const currentYear = new Date().getFullYear();
        const enableYear = enableDateValue.year();
        const serviceLife = currentYear - enableYear;
        form.setFieldsValue({ serviceLife: serviceLife >= 0 ? serviceLife : 0 });
      }
    } catch (error: any) {
      message.error('加载设备详情失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const data: EquipmentSaveParams = {
        ...values,
        purchaseDate: values.purchaseDate ? values.purchaseDate.format('YYYY-MM-DD') : undefined,
        enableDate: values.enableDate ? values.enableDate.format('YYYY-MM-DD') : undefined,
      };

      if (equipment) {
        await updateEquipment(equipment.id, data);
        message.success('更新成功');
      } else {
        await createEquipment(data);
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
      title={equipment ? '编辑设备' : '新增设备'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      confirmLoading={loading}
      width={800}
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
          name="equipmentNo"
          label="设备编号"
          rules={[{ required: true, message: '请输入设备编号' }]}
        >
          <Input placeholder="请输入设备编号" />
        </Form.Item>
        <Form.Item
          name="equipmentName"
          label="设备名称"
          rules={[{ required: true, message: '请输入设备名称' }]}
        >
          <Input placeholder="请输入设备名称" />
        </Form.Item>
        <Form.Item name="groupName" label="组别">
          <Input placeholder="请输入组别" />
        </Form.Item>
        <Form.Item name="machineNo" label="机台号">
          <Input placeholder="请输入机台号" />
        </Form.Item>
        <Form.Item name="equipmentModel" label="设备型号">
          <Input placeholder="请输入设备型号" />
        </Form.Item>
        <Form.Item name="manufacturer" label="制造商">
          <Input placeholder="请输入制造商" />
        </Form.Item>
        <Form.Item name="purchaseDate" label="购买日期">
          <DatePicker format="YYYY-MM-DD" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="robotModel" label="机械手型号">
          <Input placeholder="请输入机械手型号" />
        </Form.Item>
        <Form.Item name="enableDate" label="启用日期">
          <DatePicker
            format="YYYY-MM-DD"
            style={{ width: '100%' }}
            onChange={(date) => {
              if (date) {
                // 计算使用年限：今年 - 启用年
                const currentYear = new Date().getFullYear();
                const enableYear = date.year();
                const serviceLife = currentYear - enableYear;
                form.setFieldsValue({ serviceLife: serviceLife >= 0 ? serviceLife : 0 });
              } else {
                form.setFieldsValue({ serviceLife: undefined });
              }
            }}
          />
        </Form.Item>
        <Form.Item name="serviceLife" label="使用年限（年）">
          <InputNumber
            min={0}
            placeholder="自动计算（根据启用日期）"
            style={{ width: '100%' }}
            readOnly
          />
        </Form.Item>
        <Form.Item name="moldTempMachine" label="模温机">
          <Select placeholder="请选择模温机">
            <Select.Option value="是">是</Select.Option>
            <Select.Option value="否">否</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="chiller" label="冻水机">
          <Select placeholder="请选择冻水机">
            <Select.Option value="是">是</Select.Option>
            <Select.Option value="否">否</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="basicMold" label="基本排模">
          <Input placeholder="请输入基本排模" />
        </Form.Item>
        <Form.Item name="spareMold1" label="备用排模1">
          <Input placeholder="请输入备用排模1" />
        </Form.Item>
        <Form.Item name="spareMold2" label="备用排模2">
          <Input placeholder="请输入备用排模2" />
        </Form.Item>
        <Form.Item name="spareMold3" label="备用排模3">
          <Input placeholder="请输入备用排模3" />
        </Form.Item>
        <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
          <Select placeholder="请选择状态">
            <Select.Option value={0}>停用</Select.Option>
            <Select.Option value={1}>正常</Select.Option>
            <Select.Option value={2}>维修中</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={3} placeholder="请输入备注" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default EquipmentModal;
