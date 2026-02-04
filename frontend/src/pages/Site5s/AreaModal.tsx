import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, InputNumber, Select, message, TimePicker } from 'antd';
import {
  Site5sAreaInfo,
  Site5sAreaSaveParams,
  saveSite5sArea,
  getSite5sAreaById,
  getInjectionLeaders,
  InjectionLeaderItem,
} from '@/api/site5s';
import dayjs, { Dayjs } from 'dayjs';

interface AreaModalProps {
  visible: boolean;
  area: Site5sAreaInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const AreaModal: React.FC<AreaModalProps> = ({ visible, area, onCancel, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [leaderOptions, setLeaderOptions] = useState<InjectionLeaderItem[]>([]);

  const defaultMorning = dayjs('08:00', 'HH:mm');
  const defaultEvening = dayjs('16:00', 'HH:mm');

  useEffect(() => {
    if (visible) {
      loadLeaders();
      if (area?.id) {
        loadDetail(area.id);
      } else {
        form.resetFields();
        form.setFieldsValue({
          sortOrder: 0,
          status: 1,
          morningPhotoTime: defaultMorning,
          eveningPhotoTime: defaultEvening,
        });
      }
    }
  }, [visible, area?.id]);

  const loadLeaders = async () => {
    try {
      const list = await getInjectionLeaders();
      setLeaderOptions(list || []);
    } catch {
      setLeaderOptions([]);
    }
  };

  const loadDetail = async (id: number) => {
    try {
      const detail = await getSite5sAreaById(id);
      const fmt = (t: string | undefined) => {
        if (!t) return defaultMorning;
        const d = dayjs(t, ['HH:mm', 'H:mm', 'HH:mm:ss']);
        return d.isValid() ? d : defaultMorning;
      };
      const fmtEvening = (t: string | undefined) => {
        if (!t) return defaultEvening;
        const d = dayjs(t, ['HH:mm', 'H:mm', 'HH:mm:ss']);
        return d.isValid() ? d : defaultEvening;
      };
      form.setFieldsValue({
        ...detail,
        morningPhotoTime: fmt(detail.morningPhotoTime),
        eveningPhotoTime: fmtEvening(detail.eveningPhotoTime),
      });
    } catch {
      form.setFieldsValue({ morningPhotoTime: defaultMorning, eveningPhotoTime: defaultEvening });
    }
  };

  const formatTimeValue = (value: string | Dayjs | undefined, fallback: Dayjs) => {
    if (!value) return fallback.format('HH:mm');
    if (dayjs.isDayjs(value)) {
      return value.format('HH:mm');
    }
    const parsed = dayjs(value, ['HH:mm', 'H:mm', 'HH:mm:ss']);
    if (parsed.isValid()) {
      return parsed.format('HH:mm');
    }
    const [h = '00', m = '00'] = String(value).split(':');
    return `${h.padStart(2, '0')}:${m.padStart(2, '0')}`;
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const data: Site5sAreaSaveParams = {
        id: area?.id,
        areaName: values.areaName,
        checkItem: values.checkItem,
        responsibleUserId: values.responsibleUserId,
        responsibleUserId2: values.responsibleUserId2,
        morningPhotoTime: formatTimeValue(values.morningPhotoTime, defaultMorning),
        eveningPhotoTime: formatTimeValue(values.eveningPhotoTime, defaultEvening),
        sortOrder: values.sortOrder ?? 0,
        status: values.status ?? 1,
        remark: values.remark,
      };
      setLoading(true);
      await saveSite5sArea(data);
      message.success(area ? '保存成功' : '新增成功');
      onSuccess();
    } catch (error: any) {
      if (error.errorFields) return;
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
      width={520}
      destroyOnClose
    >
      <Form form={form} layout="vertical" preserve={false}>
        <Form.Item name="areaName" label="区域名称" rules={[{ required: true, message: '请输入区域名称' }]}>
          <Input placeholder="如 车间A区" />
        </Form.Item>
        <Form.Item name="checkItem" label="检查项目" rules={[{ required: true, message: '请输入检查项目' }]}>
          <Input placeholder="如 灯光管理、地面清洁" />
        </Form.Item>
        <Form.Item name="responsibleUserId" label="负责人1（注塑组长）" rules={[{ required: true, message: '请选择负责人1' }]}>
          <Select
            placeholder="请选择负责人1"
            options={leaderOptions.map((u) => ({ value: u.id, label: `${u.name || u.username}（${u.username}）` }))}
            allowClear
          />
        </Form.Item>
        <Form.Item
          name="responsibleUserId2"
          label="负责人2（注塑组长）"
          rules={[
            { required: true, message: '请选择负责人2' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('responsibleUserId') !== value) return Promise.resolve();
                return Promise.reject(new Error('负责人1与负责人2不能为同一人'));
              },
            }),
          ]}
        >
          <Select
            placeholder="请选择负责人2"
            options={leaderOptions.map((u) => ({ value: u.id, label: `${u.name || u.username}（${u.username}）` }))}
            allowClear
          />
        </Form.Item>
        <Form.Item name="morningPhotoTime" label="早间拍照时间" rules={[{ required: true }]}>
          <TimePicker format="HH:mm" style={{ width: '100%' }} minuteStep={5} />
        </Form.Item>
        <Form.Item name="eveningPhotoTime" label="晚间拍照时间" rules={[{ required: true }]}>
          <TimePicker format="HH:mm" style={{ width: '100%' }} minuteStep={5} />
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
        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={2} placeholder="备注信息" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default AreaModal;
