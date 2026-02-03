import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, InputNumber, Select, Button, Space, message } from 'antd';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import {
  Site5sAreaInfo,
  Site5sAreaSaveParams,
  Site5sAreaScheduleItem,
  saveSite5sArea,
  getSite5sAreaById,
} from '@/api/site5s';

interface AreaModalProps {
  visible: boolean;
  area: Site5sAreaInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const AreaModal: React.FC<AreaModalProps> = ({ visible, area, onCancel, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (visible) {
      if (area?.id) {
        loadDetail(area.id);
      } else {
        form.resetFields();
        form.setFieldsValue({
          sortOrder: 0,
          status: 1,
          schedules: [
            { slotIndex: 1, scheduledTime: '08:00', toleranceMinutes: 30 },
            { slotIndex: 2, scheduledTime: '16:00', toleranceMinutes: 30 },
          ],
        });
      }
    }
  }, [visible, area?.id]);

  const loadDetail = async (id: number) => {
    try {
      const detail = await getSite5sAreaById(id);
      const schedules = (detail.schedules || []).map((s: any) => ({
        ...s,
        scheduledTime: typeof s.scheduledTime === 'string' ? s.scheduledTime : (s.scheduledTime || '08:00').toString().slice(0, 5),
      }));
      form.setFieldsValue({
        ...detail,
        schedules: schedules.length > 0 ? schedules : [
          { slotIndex: 1, scheduledTime: '08:00', toleranceMinutes: 30 },
          { slotIndex: 2, scheduledTime: '16:00', toleranceMinutes: 30 },
        ],
      });
    } catch {
      message.error('加载区域详情失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const schedules: Site5sAreaScheduleItem[] = (values.schedules || []).map((s: any, idx: number) => ({
        slotIndex: idx + 1,
        scheduledTime: typeof s.scheduledTime === 'string' ? s.scheduledTime : (s.scheduledTime || '08:00'),
        toleranceMinutes: s.toleranceMinutes ?? 30,
        remark: s.remark,
      }));
      const data: Site5sAreaSaveParams = {
        id: area?.id,
        areaCode: values.areaCode,
        areaName: values.areaName,
        dutyName: values.dutyName,
        sortOrder: values.sortOrder ?? 0,
        status: values.status ?? 1,
        remark: values.remark,
        schedules,
      };
      setLoading(true);
      await saveSite5sArea(data);
      message.success(area ? '保存成功' : '新增成功');
      onSuccess();
    } catch (error: any) {
      if (error.errorFields) return;
      message.error(error.message || '操作失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={area ? '编辑区域' : '新增区域'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      confirmLoading={loading}
      width={600}
      destroyOnClose
    >
      <Form form={form} layout="vertical" preserve={false}>
        <Form.Item name="areaCode" label="区域编码" rules={[{ required: true, message: '请输入区域编码' }]}>
          <Input placeholder="如 AREA001" />
        </Form.Item>
        <Form.Item name="areaName" label="区域名称" rules={[{ required: true, message: '请输入区域名称' }]}>
          <Input placeholder="如 车间A区" />
        </Form.Item>
        <Form.Item name="dutyName" label="职能名称" rules={[{ required: true, message: '请输入职能名称' }]}>
          <Input placeholder="如 灯光管理、地面清洁" />
        </Form.Item>
        <Form.Item name="sortOrder" label="排序号">
          <InputNumber min={0} style={{ width: '100%' }} placeholder="越小越靠前" />
        </Form.Item>
        <Form.Item name="status" label="状态" rules={[{ required: true }]}>
          <Select options={[
            { value: 1, label: '启用' },
            { value: 0, label: '停用' },
          ]} />
        </Form.Item>

        <Form.Item label="拍照时段配置" required>
          <Form.List name="schedules">
            {(fields, { add, remove }) => (
              <>
                {fields.map(({ key, name, ...rest }) => (
                  <Space key={key} align="baseline" style={{ display: 'flex', marginBottom: 8 }}>
                    <Form.Item {...rest} name={[name, 'scheduledTime']} rules={[{ required: true }]}>
                      <Input placeholder="08:00" style={{ width: 80 }} />
                    </Form.Item>
                    <span>±</span>
                    <Form.Item {...rest} name={[name, 'toleranceMinutes']}>
                      <InputNumber min={0} max={120} placeholder="30" style={{ width: 70 }} />
                    </Form.Item>
                    <span>分钟</span>
                    <Form.Item {...rest} name={[name, 'remark']}>
                      <Input placeholder="时段说明" style={{ width: 100 }} />
                    </Form.Item>
                    <Button type="text" danger icon={<DeleteOutlined />} onClick={() => remove(name)} />
                  </Space>
                ))}
                <Form.Item>
                  <Button
                    type="dashed"
                    onClick={() => add({ scheduledTime: '08:00', toleranceMinutes: 30 })}
                    block
                    icon={<PlusOutlined />}
                  >
                    添加时段
                  </Button>
                </Form.Item>
              </>
            )}
          </Form.List>
        </Form.Item>

        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={2} placeholder="备注信息" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default AreaModal;
