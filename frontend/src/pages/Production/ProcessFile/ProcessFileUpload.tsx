import React, { useState, useEffect } from 'react';
import {
  Form,
  Upload,
  Select,
  Input,
  Button,
  message,
  Card,
  Space,
  Alert,
} from 'antd';
import { UploadOutlined, InboxOutlined } from '@ant-design/icons';
import type { UploadFile } from 'antd/es/upload/interface';
import { useNavigate, useParams } from 'react-router-dom';
import { getEquipmentList } from '@/api/equipment';
import { uploadProcessFile } from '@/api/processFile';

/**
 * 工艺文件上传页
 */
const ProcessFileUpload: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [equipmentList, setEquipmentList] = useState<any[]>([]);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const navigate = useNavigate();
  const { id } = useParams(); // 修改时传入文件ID

  useEffect(() => {
    fetchEquipmentList();
  }, []);

  const fetchEquipmentList = async () => {
    try {
      console.log('正在获取设备列表...');
      const response = await getEquipmentList({ pageNum: 1, pageSize: 1000 });
      console.log('设备列表响应:', response);
      
      // request拦截器已经返回了data，所以response直接就是 {list, total}
      if (response && response.list) {
        setEquipmentList(response.list);
        console.log('设备列表加载成功，共', response.list.length, '条数据');
      } else {
        setEquipmentList([]);
        console.warn('设备列表为空或格式不正确:', response);
        message.warning('暂无设备数据');
      }
    } catch (error: any) {
      console.error('获取设备列表失败:', error);
      message.error('获取设备列表失败: ' + (error.message || '未知错误'));
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      if (fileList.length === 0) {
        message.error('请选择要上传的文件');
        return;
      }

      setLoading(true);

      await uploadProcessFile({
        id: id ? Number(id) : undefined,
        equipmentId: values.equipmentId,
        file: fileList[0].originFileObj as File,
        changeReason: values.changeReason,
        remark: values.remark,
      });

      message.success(id ? '修改成功' : '上传成功');
      navigate('/production/process-file');
    } catch (error: any) {
      if (error.errorFields) {
        return;
      }
      message.error(error.message || '操作失败');
    } finally {
      setLoading(false);
    }
  };

  const uploadProps = {
    beforeUpload: (file: File) => {
      // 验证文件类型
      const isExcel = file.name.endsWith('.xls') || file.name.endsWith('.xlsx');
      if (!isExcel) {
        message.error('只支持Excel文件格式（.xls, .xlsx）');
        return false;
      }

      // 验证文件大小（10MB）
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isLt10M) {
        message.error('文件大小不能超过10MB');
        return false;
      }

      setFileList([{
        uid: file.uid,
        name: file.name,
        status: 'done',
        originFileObj: file,
      } as UploadFile]);
      
      return false; // 阻止自动上传
    },
    fileList,
    onRemove: () => {
      setFileList([]);
    },
    maxCount: 1,
  };

  return (
    <div className="process-file-upload">
      <Card title={id ? '修改工艺文件' : '上传工艺文件'}>
        <Alert
          message="温馨提示"
          description={
            <ul style={{ marginBottom: 0, paddingLeft: 20 }}>
              <li>每台设备至少需要配置4个工艺文件</li>
              <li>仅支持Excel文件格式（.xls, .xlsx）</li>
              <li>文件大小不超过10MB</li>
              <li>上传后需要提交审批流程</li>
              {id && <li>修改工艺文件必须填写变更原因</li>}
            </ul>
          }
          type="info"
          showIcon
          style={{ marginBottom: 24 }}
        />

        <Form
          form={form}
          layout="vertical"
          style={{ maxWidth: 800 }}
        >
          <Form.Item
            name="equipmentId"
            label="选择设备（机台号）"
            rules={[{ required: true, message: '请选择设备' }]}
            tooltip="请根据机台号选择对应的设备"
          >
            <Select
              showSearch
              placeholder="请输入机台号、设备编号或设备名称进行搜索"
              optionFilterProp="label"
              filterOption={(input, option) => {
                const label = option?.label ?? '';
                const searchText = option?.searchText ?? '';
                const searchValue = input.toLowerCase();
                // 支持通过显示标签和搜索文本进行匹配
                return label.toLowerCase().includes(searchValue) || 
                       searchText.toLowerCase().includes(searchValue);
              }}
              options={equipmentList
                .filter((eq: any) => eq.status === 1) // 只显示正常状态的设备
                .sort((a: any, b: any) => {
                  // 按机台号排序，如果机台号为空则排在后面
                  const machineNoA = a.machineNo || '';
                  const machineNoB = b.machineNo || '';
                  if (!machineNoA && machineNoB) return 1;
                  if (machineNoA && !machineNoB) return -1;
                  return machineNoA.localeCompare(machineNoB, 'zh-CN', { numeric: true });
                })
                .map((eq: any) => {
                  // 优先显示机台号，格式：机台号 - 设备名称 (设备编号)
                  const machineNo = eq.machineNo || eq.equipmentNo || '未设置';
                  const displayLabel = eq.machineNo 
                    ? `${machineNo} - ${eq.equipmentName} (${eq.equipmentNo})`
                    : `${eq.equipmentNo} - ${eq.equipmentName} [无机台号]`;
                  
                  // 构建搜索文本，包含机台号、设备编号、设备名称
                  const searchText = `${eq.machineNo || ''} ${eq.equipmentNo || ''} ${eq.equipmentName || ''}`.trim();
                  
                  return {
                    label: displayLabel,
                    value: eq.id,
                    searchText: searchText, // 用于搜索的关键词
                  };
                })}
            />
          </Form.Item>

          {id && (
            <Form.Item
              name="changeReason"
              label="变更原因"
              rules={[{ required: true, message: '请填写变更原因' }]}
            >
              <Input.TextArea
                rows={4}
                placeholder="请详细说明本次变更的原因和内容"
                maxLength={500}
                showCount
              />
            </Form.Item>
          )}

          <Form.Item
            label="上传工艺文件"
            required
            tooltip="支持.xls和.xlsx格式，文件大小不超过10MB"
          >
            <Upload.Dragger {...uploadProps}>
              <p className="ant-upload-drag-icon">
                <InboxOutlined />
              </p>
              <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
              <p className="ant-upload-hint">
                支持Excel文件格式（.xls, .xlsx），单个文件不超过10MB
              </p>
            </Upload.Dragger>
          </Form.Item>

          <Form.Item name="remark" label="备注">
            <Input.TextArea
              rows={3}
              placeholder="请输入备注信息（可选）"
              maxLength={1000}
              showCount
            />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" onClick={handleSubmit} loading={loading}>
                {id ? '保存修改' : '上传'}
              </Button>
              <Button onClick={() => navigate('/production/process-file')}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default ProcessFileUpload;
