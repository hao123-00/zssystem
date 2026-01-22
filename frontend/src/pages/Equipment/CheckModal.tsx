import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, DatePicker, Select, message, Row, Col, Divider } from 'antd';
import dayjs from 'dayjs';
import {
  EquipmentCheckInfo,
  EquipmentCheckSaveParams,
  saveCheck,
  getCheckById,
} from '@/api/equipment';
import { getEquipmentList } from '@/api/equipment';
import './CheckModal.less';

interface CheckModalProps {
  visible: boolean;
  check: EquipmentCheckInfo | null;
  onCancel: () => void;
  onSuccess: () => void;
}

const CheckModal: React.FC<CheckModalProps> = ({ visible, check, onCancel, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [equipmentList, setEquipmentList] = useState<any[]>([]);

  useEffect(() => {
    if (visible) {
      loadEquipmentList();
      if (check && check.id) {
        loadCheckDetail(check.id);
      } else {
        form.resetFields();
        form.setFieldsValue({
          checkDate: dayjs(),
        });
      }
    }
  }, [visible, check]);

  const loadEquipmentList = async () => {
    try {
      const response = await getEquipmentList({ pageNum: 1, pageSize: 1000 });
      setEquipmentList(response.list || []);
    } catch (error: any) {
      console.error('加载设备列表失败', error);
    }
  };

  const loadCheckDetail = async (id: number) => {
    try {
      const detail = await getCheckById(id);
      const formValues: any = {
        ...detail,
        checkDate: detail.checkDate ? dayjs(detail.checkDate) : null,
      };
      // 将0转换为null，因为Select组件需要null表示未选择
      Object.keys(formValues).forEach((key) => {
        if (key.includes('Item') && formValues[key] === 0) {
          formValues[key] = null;
        }
      });
      form.setFieldsValue(formValues);
    } catch (error: any) {
      message.error('加载点检详情失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const data: EquipmentCheckSaveParams = {
        ...values,
        checkDate: values.checkDate ? values.checkDate.format('YYYY-MM-DD') : undefined,
      };
      // 将null转换为0（异常），未选择的状态设为null
      Object.keys(data).forEach((key) => {
        if (key.includes('Item') && data[key as keyof EquipmentCheckSaveParams] === null) {
          // 保持null，表示未检查
        } else if (key.includes('Item') && data[key as keyof EquipmentCheckSaveParams] === undefined) {
          data[key as keyof EquipmentCheckSaveParams] = null as any;
        }
      });

      await saveCheck(data);
      message.success(check ? '更新成功' : '创建成功');
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
      title={check ? '编辑点检记录' : '新增点检记录'}
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
        initialValues={{
          checkDate: dayjs(),
        }}
      >
        <Form.Item name="id" hidden>
          <Input />
        </Form.Item>
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              name="equipmentId"
              label="设备"
              rules={[{ required: true, message: '请选择设备' }]}
            >
              <Select
                placeholder="请选择设备"
                showSearch
                filterOption={(input, option) =>
                  (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                }
                options={equipmentList.map((eq) => ({
                  value: eq.id,
                  label: `${eq.equipmentNo} - ${eq.equipmentName}`,
                }))}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              name="checkDate"
              label="检查日期"
              rules={[{ required: true, message: '请选择检查日期' }]}
            >
              <DatePicker format="YYYY-MM-DD" style={{ width: '100%' }} />
            </Form.Item>
          </Col>
        </Row>
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              name="checkerName"
              label="检点人"
              rules={[{ required: true, message: '请输入检点人姓名' }]}
            >
              <Input placeholder="请输入检点人姓名" />
            </Form.Item>
          </Col>
        </Row>

        <Divider orientation="left">电路部分</Divider>
        <Row gutter={16}>
          <Col span={8}>
            <Form.Item name="circuitItem1" label="发热圈/感温线/交流接触器温度控制器">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="circuitItem2" label="电箱排风扇/安全门开关/烘料斗温度">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="circuitItem3" label="形成开关">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
        </Row>

        <Divider orientation="left">机架部分</Divider>
        <Row gutter={16}>
          <Col span={8}>
            <Form.Item name="frameItem1" label="哥林柱、机架螺母">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="frameItem2" label="安全挡板/射咀/低压保护">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="frameItem3" label="调模牙盘变形及余音">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
        </Row>

        <Divider orientation="left">油路部分</Divider>
        <Row gutter={16}>
          <Col span={8}>
            <Form.Item name="oilItem1" label="油泵压力/动作">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="oilItem2" label="油泵/溶胶/马达杂音">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="oilItem3" label="油温/冷却器">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="oilItem4" label="自动加油润滑油管">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="oilItem5" label="机台油管漏油">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
        </Row>

        <Divider orientation="left">周边设备</Divider>
        <Row gutter={16}>
          <Col span={8}>
            <Form.Item name="peripheralItem1" label="模温机、冻水机异响">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="peripheralItem2" label="模温机冷却水、过滤网">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="peripheralItem3" label="油温机缺油、温度">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="peripheralItem4" label="冻水机过滤网、运水">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="peripheralItem5" label="冻水机制冷系统、交流触感器">
              <Select placeholder="请选择状态" style={{ width: '100%' }}>
                <Select.Option value={1}>正常</Select.Option>
                <Select.Option value={0}>异常</Select.Option>
              </Select>
            </Form.Item>
          </Col>
        </Row>

        <Form.Item name="remark" label="异常项备注说明">
          <Input.TextArea rows={4} placeholder="请输入异常项备注说明" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default CheckModal;
