import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Form,
  Input,
  Select,
  Space,
  Tag,
  Modal,
  message,
  Card,
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  DownloadOutlined,
  EyeOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import type { ProcessFileInfo, ProcessFileQueryParams } from '@/api/processFile';
import {
  getProcessFileList,
  submitProcessFile,
  deleteProcessFile,
  downloadProcessFile,
  uploadSignature,
} from '@/api/processFile';
import SignaturePad from '@/components/SignaturePad/SignaturePad';

/**
 * 工艺文件列表页
 */
const ProcessFileList: React.FC = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<ProcessFileInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [submitModalVisible, setSubmitModalVisible] = useState(false);
  const [submittingFileId, setSubmittingFileId] = useState<number | null>(null);

  const statusOptions = [
    { label: '草稿', value: 0 },
    { label: '待车间主任审核', value: 1 },
    { label: '待注塑部经理会签', value: 2 },
    { label: '待生产技术部经理批准', value: 3 },
    { label: '已批准（生效中）', value: 5 },
    { label: '已驳回', value: -1 },
    { label: '已作废', value: -2 },
  ];

  useEffect(() => {
    fetchList();
  }, [pagination.current, pagination.pageSize]);

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: ProcessFileQueryParams = {
        ...values,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      console.log('正在查询工艺文件列表，参数:', params);
      const response = await getProcessFileList(params);
      console.log('工艺文件列表响应:', response);
      
      // request拦截器已经返回了data，所以response直接就是 {list, total}
      if (response && response.list) {
        setTableData(response.list);
        setPagination({ ...pagination, total: response.total || 0 });
        console.log('工艺文件列表加载成功，共', response.list.length, '条数据');
      } else {
        setTableData([]);
        setPagination({ ...pagination, total: 0 });
        console.warn('工艺文件列表为空或格式不正确:', response);
      }
    } catch (error: any) {
      console.error('查询工艺文件列表失败:', error);
      message.error('查询失败: ' + (error.message || '未知错误'));
      setTableData([]);
      setPagination({ ...pagination, total: 0 });
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setPagination({ ...pagination, current: 1 });
    fetchList();
  };

  const handleReset = () => {
    form.resetFields();
    setPagination({ ...pagination, current: 1 });
    fetchList();
  };

  const handleSubmit = async (id: number) => {
    setSubmittingFileId(id);
    setSubmitModalVisible(true);
  };

  const handleSignatureConfirm = async (signatureData: string) => {
    if (!submittingFileId) return;

    try {
      // 提交审批（包含电子签名）
      await submitProcessFile(submittingFileId, signatureData);
      
      message.success('提交成功');
      setSubmitModalVisible(false);
      setSubmittingFileId(null);
      fetchList();
    } catch (error: any) {
      message.error(error.message || '提交失败');
    }
  };

  const handleDelete = async (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除此工艺文件吗？',
      onOk: async () => {
        try {
          await deleteProcessFile(id);
          message.success('删除成功');
          fetchList();
        } catch (error: any) {
          message.error(error.message || '删除失败');
        }
      },
    });
  };

  const handleDownload = async (record: ProcessFileInfo) => {
    try {
      console.log('开始下载工艺文件，ID:', record.id, '文件名:', record.fileName);
      const response = await downloadProcessFile(record.id);
      console.log('下载响应:', response);
      
      // 检查响应状态
      if (!response || !response.data) {
        throw new Error('下载响应为空');
      }
      
      // 检查是否是错误响应（可能是文本错误信息）
      if (response.data instanceof Blob) {
        // 检查blob的type，如果是text/plain或application/json，可能是错误信息
        if (response.data.type === 'text/plain' || response.data.type === 'application/json' || response.data.size < 100) {
          const text = await response.data.text();
          throw new Error(text || '下载失败');
        }
      }
      
      // 对于blob响应，response本身就是axios响应对象，response.data是blob
      const blob = response.data instanceof Blob 
        ? response.data 
        : new Blob([response.data]);
      
      // 从响应头获取文件名（如果后端设置了）
      const contentDisposition = response.headers['content-disposition'];
      let fileName = record.fileName || `工艺文件_${record.id}.xlsx`;
      if (contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        if (fileNameMatch && fileNameMatch[1]) {
          fileName = decodeURIComponent(fileNameMatch[1].replace(/['"]/g, ''));
        }
      }
      
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      message.success('下载成功');
    } catch (error: any) {
      console.error('下载工艺文件失败:', error);
      const errorMessage = error.message || '未知错误';
      message.error('下载失败: ' + errorMessage);
    }
  };

  const getStatusTag = (status: number) => {
    const colorMap: { [key: number]: string } = {
      0: 'default',
      1: 'processing',
      2: 'processing',
      3: 'processing',
      5: 'success',
      '-1': 'error',
      '-2': 'default',
    };
    return colorMap[status] || 'default';
  };

  const columns = [
    {
      title: '工艺文件编号',
      dataIndex: 'fileNo',
      key: 'fileNo',
      width: 150,
    },
    {
      title: '设备编号',
      dataIndex: 'equipmentNo',
      key: 'equipmentNo',
      width: 120,
    },
    {
      title: '机台号',
      dataIndex: 'machineNo',
      key: 'machineNo',
      width: 100,
    },
    {
      title: '文件名称',
      dataIndex: 'fileName',
      key: 'fileName',
      width: 200,
      ellipsis: true,
    },
    {
      title: '版本',
      dataIndex: 'versionText',
      key: 'versionText',
      width: 80,
    },
    {
      title: '文件大小',
      dataIndex: 'fileSizeText',
      key: 'fileSizeText',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'statusText',
      key: 'statusText',
      width: 150,
      render: (text: string, record: ProcessFileInfo) => (
        <Tag color={getStatusTag(record.status)}>{text}</Tag>
      ),
    },
    {
      title: '创建人',
      dataIndex: 'creatorName',
      key: 'creatorName',
      width: 100,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160,
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right' as const,
      width: 220,
      render: (_: any, record: ProcessFileInfo) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/production/process-file/detail/${record.id}`)}
          >
            查看
          </Button>
          <Button
            type="link"
            size="small"
            icon={<DownloadOutlined />}
            onClick={() => handleDownload(record)}
          >
            下载
          </Button>
          {record.status === 0 && (
            <>
              <Button
                type="link"
                size="small"
                icon={<CheckCircleOutlined />}
                onClick={() => handleSubmit(record.id)}
              >
                提交
              </Button>
              <Button
                type="link"
                size="small"
                danger
                icon={<DeleteOutlined />}
                onClick={() => handleDelete(record.id)}
              >
                删除
              </Button>
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="process-file-list">
      <Card title="工艺文件管理">
        <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="fileNo" label="文件编号">
            <Input placeholder="请输入文件编号" allowClear />
          </Form.Item>
          <Form.Item name="equipmentNo" label="设备编号">
            <Input placeholder="请输入设备编号" allowClear />
          </Form.Item>
          <Form.Item name="machineNo" label="机台号">
            <Input placeholder="请输入机台号" allowClear />
          </Form.Item>
          <Form.Item name="fileName" label="文件名称">
            <Input placeholder="请输入文件名称" allowClear />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              placeholder="请选择状态"
              allowClear
              style={{ width: 180 }}
              options={statusOptions}
            />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button
                type="primary"
                icon={<SearchOutlined />}
                onClick={handleSearch}
              >
                查询
              </Button>
              <Button icon={<ReloadOutlined />} onClick={handleReset}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>

        <div style={{ marginBottom: 16 }}>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/production/process-file/form')}
          >
            新建工艺文件
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={tableData}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={(newPagination) => {
            setPagination({
              current: newPagination.current || 1,
              pageSize: newPagination.pageSize || 10,
              total: pagination.total,
            });
          }}
          scroll={{ x: 1400 }}
        />
      </Card>

      {/* 提交审批签名弹窗 */}
      <Modal
        title="电子签名 - 提交审批"
        open={submitModalVisible}
        onCancel={() => {
          setSubmitModalVisible(false);
          setSubmittingFileId(null);
        }}
        footer={null}
        width={700}
        destroyOnClose
      >
        <SignaturePad
          onConfirm={handleSignatureConfirm}
          onCancel={() => {
            setSubmitModalVisible(false);
            setSubmittingFileId(null);
          }}
        />
      </Modal>
    </div>
  );
};

export default ProcessFileList;
