import React, { useEffect, useState, useRef } from 'react';
import { Modal, Form, Input, Select, Button, message } from 'antd';
import { CameraOutlined, UploadOutlined } from '@ant-design/icons';
import StatusSelect, { ABNORMAL_EMPTY } from '@/components/StatusSelect';

const STATUS_FIELDS = ['equipmentCleaning', 'floorCleaning', 'leakage', 'itemPlacement', 'injectionMachine', 'robot', 'assemblyLine', 'mold', 'process'];
import {
  HandoverRecordInfo,
  saveHandoverRecord,
  getHandoverRecordById,
  getHandoverProductNames,
  uploadHandoverPhoto,
} from '@/api/handover';
import { getEquipmentList } from '@/api/equipment';

interface HandoverModalProps {
  visible: boolean;
  record: HandoverRecordInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const isMobile = () =>
  /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ||
  (typeof window !== 'undefined' && window.innerWidth < 768);

const HandoverModal: React.FC<HandoverModalProps> = ({ visible, record, onCancel, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [photoUploading, setPhotoUploading] = useState(false);
  const [photoUploaded, setPhotoUploaded] = useState(false);
  const [equipmentList, setEquipmentList] = useState<{ id: number; equipmentNo: string; equipmentName: string }[]>([]);
  const [productOptions, setProductOptions] = useState<string[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (visible) {
      if (record) {
        loadRecordDetail(record.id);
      } else {
        form.resetFields();
        form.setFieldsValue({
          photoPath: undefined,
        });
        setPhotoUploaded(false);
      }
    }
  }, [visible, record]);

  useEffect(() => {
    fetchEquipmentList();
  }, []);

  const fetchEquipmentList = async () => {
    try {
      const res = await getEquipmentList({
        status: 1,
        pageNum: 1,
        pageSize: 500,
      });
      setEquipmentList(res.list || []);
    } catch {
      setEquipmentList([]);
    }
  };

  const loadRecordDetail = async (id: number) => {
    try {
      const detail = await getHandoverRecordById(id);
      form.setFieldsValue({
        ...detail,
      });
      if (detail.equipmentId) {
        loadProductNames(detail.equipmentId);
      }
    } catch {
      message.error('加载记录详情失败');
    }
  };

  const loadProductNames = async (equipmentId: number) => {
    try {
      const list = await getHandoverProductNames(equipmentId);
      setProductOptions(list || []);
    } catch {
      setProductOptions([]);
    }
  };

  const handleEquipmentChange = (equipmentId: number) => {
    form.setFieldsValue({ productName: undefined });
    if (equipmentId) {
      loadProductNames(equipmentId);
    } else {
      setProductOptions([]);
    }
  };

  const handlePhotoChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      message.error('请选择图片格式');
      return;
    }
    setPhotoUploading(true);
    try {
      const path = await uploadHandoverPhoto(file);
      form.setFieldValue('photoPath', path);
      setPhotoUploaded(true);
      message.success('照片已上传');
    } catch (err: any) {
      message.error(err.message || '上传失败');
    } finally {
      setPhotoUploading(false);
      e.target.value = '';
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (!record && (!values.photoPath || !values.photoPath.trim())) {
        message.error(isMobile() ? '请使用摄像头拍照' : '请上传一张照片');
        return;
      }
      setLoading(true);

      const data = { ...values };
      if (record) delete data.photoPath;
      STATUS_FIELDS.forEach((f) => {
        if (data[f] === ABNORMAL_EMPTY) data[f] = '';
      });

      await saveHandoverRecord(data);
      message.success(record ? '更新成功' : '创建成功');
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
      title={record ? '编辑交接班记录' : '新增交接班记录'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onCancel}
      confirmLoading={loading}
      width={720}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <Form.Item name="id" hidden>
          <Input />
        </Form.Item>
        <Form.Item
          name="equipmentId"
          label="设备"
          rules={[{ required: true, message: '请选择设备' }]}
        >
          <Select
            placeholder="请选择设备"
            options={equipmentList.map((e) => ({
              value: e.id,
              label: `${e.equipmentNo} ${e.equipmentName || ''}`,
            }))}
            onChange={handleEquipmentChange}
          />
        </Form.Item>
        {!record && (
          <Form.Item
            name="photoPath"
            label={isMobile() ? '现场拍照' : '现场照片'}
            rules={[{ required: true, message: isMobile() ? '请使用摄像头拍照' : '请上传一张照片' }]}
          >
            <div>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                capture={isMobile() ? 'environment' : undefined}
                onChange={handlePhotoChange}
                style={{ display: 'none' }}
              />
              <Button
                type="default"
                icon={isMobile() ? <CameraOutlined /> : <UploadOutlined />}
                loading={photoUploading}
                onClick={() => fileInputRef.current?.click()}
              >
                {photoUploaded ? '已拍照/已上传' : isMobile() ? '点击拍照' : '点击上传照片'}
              </Button>
            </div>
          </Form.Item>
        )}
        <Form.Item name="shift" label="班次" rules={[{ required: true, message: '请选择班次' }]}>
          <Select placeholder="请选择班次" options={[{ value: 'A', label: 'A' }, { value: 'B', label: 'B' }, { value: 'C', label: 'C' }, { value: 'D', label: 'D' }]} />
        </Form.Item>
        <Form.Item name="productName" label="产品名称" rules={[{ required: true, message: '请选择产品名称' }]}>
          <Select
            placeholder="请选择产品名称（来自设备基本排模）"
            allowClear
            showSearch
            optionFilterProp="label"
            options={productOptions.map((p) => ({ value: p, label: p }))}
          />
        </Form.Item>
        <Form.Item name="material" label="材质" rules={[{ required: true, message: '请选择材质' }]}>
          <Select
            placeholder="请选择材质"
            options={[
              { value: 'ABS', label: 'ABS' },
              { value: 'POM', label: 'POM' },
              { value: 'PC', label: 'PC' },
              { value: '透明ABS', label: '透明ABS' },
              { value: 'ABS+TA50E', label: 'ABS+TA50E' },
              { value: 'ABS+TA60E', label: 'ABS+TA60E' },
              { value: 'PC+TA50E', label: 'PC+TA50E' },
            ]}
          />
        </Form.Item>
        <Form.Item
          name="equipmentCleaning"
          label="设备清洁"
          rules={[
            { required: true, message: '请选择状态' },
            { validator: (_, v) => (v === ABNORMAL_EMPTY ? Promise.reject(new Error('请输入异常原因')) : Promise.resolve()) },
          ]}
        >
          <StatusSelect placeholder="请选择状态" />
        </Form.Item>
        <Form.Item
          name="floorCleaning"
          label="地面清洁"
          rules={[
            { required: true, message: '请选择状态' },
            { validator: (_, v) => (v === ABNORMAL_EMPTY ? Promise.reject(new Error('请输入异常原因')) : Promise.resolve()) },
          ]}
        >
          <StatusSelect placeholder="请选择状态" />
        </Form.Item>
        <Form.Item
          name="leakage"
          label="有无漏油"
          rules={[
            { required: true, message: '请选择状态' },
            { validator: (_, v) => (v === ABNORMAL_EMPTY ? Promise.reject(new Error('请输入异常原因')) : Promise.resolve()) },
          ]}
        >
          <StatusSelect placeholder="请选择状态" />
        </Form.Item>
        <Form.Item
          name="itemPlacement"
          label="物品摆放"
          rules={[
            { required: true, message: '请选择状态' },
            { validator: (_, v) => (v === ABNORMAL_EMPTY ? Promise.reject(new Error('请输入异常原因')) : Promise.resolve()) },
          ]}
        >
          <StatusSelect placeholder="请选择状态" />
        </Form.Item>
        <Form.Item
          name="injectionMachine"
          label="注塑机"
          rules={[
            { required: true, message: '请选择状态' },
            { validator: (_, v) => (v === ABNORMAL_EMPTY ? Promise.reject(new Error('请输入异常原因')) : Promise.resolve()) },
          ]}
        >
          <StatusSelect placeholder="请选择状态" />
        </Form.Item>
        <Form.Item
          name="robot"
          label="机械手"
          rules={[
            { required: true, message: '请选择状态' },
            { validator: (_, v) => (v === ABNORMAL_EMPTY ? Promise.reject(new Error('请输入异常原因')) : Promise.resolve()) },
          ]}
        >
          <StatusSelect placeholder="请选择状态" />
        </Form.Item>
        <Form.Item
          name="assemblyLine"
          label="流水线"
          rules={[
            { required: true, message: '请选择状态' },
            { validator: (_, v) => (v === ABNORMAL_EMPTY ? Promise.reject(new Error('请输入异常原因')) : Promise.resolve()) },
          ]}
        >
          <StatusSelect placeholder="请选择状态" />
        </Form.Item>
        <Form.Item
          name="mold"
          label="模具"
          rules={[
            { required: true, message: '请选择状态' },
            { validator: (_, v) => (v === ABNORMAL_EMPTY ? Promise.reject(new Error('请输入异常原因')) : Promise.resolve()) },
          ]}
        >
          <StatusSelect placeholder="请选择状态" />
        </Form.Item>
        <Form.Item
          name="process"
          label="工艺"
          rules={[
            { required: true, message: '请选择状态' },
            { validator: (_, v) => (v === ABNORMAL_EMPTY ? Promise.reject(new Error('请输入异常原因')) : Promise.resolve()) },
          ]}
        >
          <StatusSelect placeholder="请选择状态" />
        </Form.Item>
        <Form.Item name="handoverLeader" label="交接组长" rules={[{ required: true, message: '请输入交接组长' }]}>
          <Input placeholder="请输入交接组长" />
        </Form.Item>
        <Form.Item name="receivingLeader" label="接班组长" rules={[{ required: true, message: '请输入接班组长' }]}>
          <Input placeholder="请输入接班组长" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default HandoverModal;
