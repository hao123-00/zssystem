import React, { useEffect, useState } from 'react';
import {
  Table,
  Button,
  Form,
  Select,
  DatePicker,
  Space,
  message,
  Popconfirm,
  Card,
  Modal,
  Spin,
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, DownloadOutlined, EyeOutlined, PictureOutlined } from '@ant-design/icons';
import {
  getHandoverRecordList,
  deleteHandoverRecord,
  exportHandoverExcel,
  getHandoverExportFileCount,
  exportHandoverExcelPage,
  getHandoverProductNames,
  getHandoverRecordPhoto,
  HandoverRecordInfo,
  HandoverRecordQueryParams,
} from '@/api/handover';
import { getEquipmentList } from '@/api/equipment';
import HandoverModal from './HandoverModal';
import HandoverRecordPreview from '@/components/HandoverRecordPreview/HandoverRecordPreview';
import './ProductionList.less';
import dayjs from 'dayjs';

const HandoverList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<HandoverRecordInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [equipmentList, setEquipmentList] = useState<{ id: number; equipmentNo: string; equipmentName: string }[]>([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<HandoverRecordInfo | null>(null);
  const [exportModalVisible, setExportModalVisible] = useState(false);
  const [exportForm] = Form.useForm();
  const [exporting, setExporting] = useState(false);
  const [previewVisible, setPreviewVisible] = useState(false);
  const [photoModalVisible, setPhotoModalVisible] = useState(false);
  const [photoUrl, setPhotoUrl] = useState<string | null>(null);
  const [photoLoading, setPhotoLoading] = useState(false);

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

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: HandoverRecordQueryParams = {
        ...values,
        recordMonth: values.recordMonth ? values.recordMonth.format('YYYY-MM') : undefined,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getHandoverRecordList(params);
      setTableData(response.list || []);
      setPagination({ ...pagination, total: response.total || 0 });
    } catch (error: any) {
      message.error(error.message || '查询失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEquipmentList();
  }, []);

  useEffect(() => {
    fetchList();
  }, [pagination.current, pagination.pageSize]);

  const handleAdd = () => {
    setEditingRecord(null);
    setModalVisible(true);
  };

  const handleEdit = (record: HandoverRecordInfo) => {
    setEditingRecord(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteHandoverRecord(id);
      message.success('删除成功');
      fetchList();
    } catch (error: any) {
      message.error(error.message || '删除失败');
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

  const [previewParams, setPreviewParams] = useState<{ equipmentId: number; recordMonth: string } | null>(null);

  const handlePreview = async () => {
    try {
      const values = await exportForm.validateFields();
      setPreviewParams({ equipmentId: values.equipmentId, recordMonth: values.recordMonth.format('YYYY-MM') });
      setExportModalVisible(false);
      setPreviewVisible(true);
    } catch {
      // 校验失败不处理
    }
  };

  const handleViewPhoto = async (record: HandoverRecordInfo) => {
    if (!record.hasPhoto) {
      message.info('该记录无照片');
      return;
    }
    setPhotoLoading(true);
    setPhotoModalVisible(true);
    setPhotoUrl(null);
    try {
      const res = await getHandoverRecordPhoto(record.id);
      const blob = res?.data instanceof Blob ? res.data : new Blob([res?.data ?? []]);
      const url = URL.createObjectURL(blob);
      setPhotoUrl(url);
    } catch (err: any) {
      message.error(err.message || '加载照片失败');
      setPhotoModalVisible(false);
    } finally {
      setPhotoLoading(false);
    }
  };

  const handleClosePhotoModal = () => {
    if (photoUrl) URL.revokeObjectURL(photoUrl);
    setPhotoUrl(null);
    setPhotoModalVisible(false);
  };

  const downloadBlob = (blob: Blob, filename: string) => {
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
  };

  const handleExport = async () => {
    try {
      const values = await exportForm.validateFields();
      setExporting(true);
      const recordMonth = values.recordMonth.format('YYYY-MM');
      const equipmentId = values.equipmentId;
      const fileCount = await getHandoverExportFileCount(equipmentId, recordMonth);
      if (fileCount <= 1) {
        const response = await exportHandoverExcel(equipmentId, recordMonth);
        const contentDisposition = response.headers?.['content-disposition'];
        let filename = `交接班记录_${recordMonth}.xlsx`;
        if (contentDisposition) {
          const match = contentDisposition.match(/filename\*=UTF-8''(.+?)(?:;|$)/);
          if (match) filename = decodeURIComponent(match[1].trim());
        }
        const blob = response.data instanceof Blob ? response.data : new Blob([response.data]);
        downloadBlob(blob, filename);
      } else {
        for (let page = 1; page <= fileCount; page++) {
          const response = await exportHandoverExcelPage(equipmentId, recordMonth, page);
          const contentDisposition = response.headers?.['content-disposition'];
          const filename = contentDisposition
            ? (() => {
                const match = contentDisposition.match(/filename\*=UTF-8''(.+?)(?:;|$)/);
                return match ? decodeURIComponent(match[1].trim()) : `交接班记录_${recordMonth}_${page}.xlsx`;
              })()
            : `交接班记录_${recordMonth}_${page}.xlsx`;
          const blob = response.data instanceof Blob ? response.data : new Blob([response.data]);
          downloadBlob(blob, filename);
        }
      }
      message.success(fileCount > 1 ? `已导出${fileCount}个Excel文件` : '导出成功');
      setExportModalVisible(false);
    } catch (error: any) {
      if (error.errorFields) return;
      message.error('导出失败：' + (error.message || '未知错误'));
    } finally {
      setExporting(false);
    }
  };

  const columns = [
    {
      title: '记录时间',
      dataIndex: 'recordDate',
      key: 'recordDate',
      width: 175,
      render: (val: string) => (val ? dayjs(val).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    { title: '设备编号', dataIndex: 'equipmentNo', key: 'equipmentNo', width: 120 },
    { title: '班次', dataIndex: 'shift', key: 'shift', width: 80 },
    { title: '产品名称', dataIndex: 'productName', key: 'productName', width: 150 },
    { title: '材质', dataIndex: 'material', key: 'material', width: 100 },
    { title: '交接组长', dataIndex: 'handoverLeader', key: 'handoverLeader', width: 90 },
    { title: '接班组长', dataIndex: 'receivingLeader', key: 'receivingLeader', width: 90 },
  ];

  return (
    <div className="production-list">
      <Card>
        <Form form={form} layout="inline" onFinish={handleSearch} style={{ marginBottom: 16 }}>
          <Form.Item name="equipmentId" label="设备">
            <Select
              placeholder="请选择设备"
              allowClear
              style={{ width: 180 }}
              options={equipmentList.map((e) => ({
                value: e.id,
                label: `${e.equipmentNo} ${e.equipmentName || ''}`,
              }))}
            />
          </Form.Item>
          <Form.Item name="recordMonth" label="月份">
            <DatePicker picker="month" format="YYYY-MM" allowClear />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<ReloadOutlined />}>
                查询
              </Button>
              <Button onClick={handleReset}>重置</Button>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                新增
              </Button>
              <Button icon={<DownloadOutlined />} onClick={() => setExportModalVisible(true)}>
                导出Excel
              </Button>
            </Space>
          </Form.Item>
        </Form>

        <Table
          dataSource={tableData}
          columns={[
            ...columns,
            {
              title: '操作',
              key: 'action',
              width: 180,
              fixed: 'right' as const,
              render: (_: any, record: HandoverRecordInfo) => (
                <Space>
                  <Button
                    type="link"
                    size="small"
                    icon={<PictureOutlined />}
                    onClick={() => handleViewPhoto(record)}
                    disabled={!record.hasPhoto}
                  >
                    查看图片
                  </Button>
                  <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
                    编辑
                  </Button>
                  <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)}>
                    <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                      删除
                    </Button>
                  </Popconfirm>
                </Space>
              ),
            },
          ]}
          rowKey="id"
          loading={loading}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            onChange: (page, pageSize) => setPagination({ ...pagination, current: page, pageSize: pageSize || 20 }),
          }}
          scroll={{ x: 900 }}
        />
      </Card>

      <HandoverModal
        visible={modalVisible}
        record={editingRecord}
        onCancel={() => setModalVisible(false)}
        onSuccess={() => {
          setModalVisible(false);
          fetchList();
        }}
      />

      <Modal
        title="导出/预览交接班记录"
        open={exportModalVisible}
        onCancel={() => setExportModalVisible(false)}
        footer={[
          <Button key="preview" icon={<EyeOutlined />} onClick={handlePreview}>
            预览
          </Button>,
          <Button key="export" type="primary" icon={<DownloadOutlined />} loading={exporting} onClick={handleExport}>
            导出Excel
          </Button>,
        ]}
      >
        <Form form={exportForm} layout="vertical" initialValues={{ recordMonth: dayjs() }}>
          <Form.Item
            name="equipmentId"
            label="设备编号"
            rules={[{ required: true, message: '请选择设备' }]}
          >
            <Select
              placeholder="请选择设备"
              options={equipmentList.map((e) => ({
                value: e.id,
                label: `${e.equipmentNo} ${e.equipmentName || ''}`,
              }))}
            />
          </Form.Item>
          <Form.Item
            name="recordMonth"
            label="月份"
            rules={[{ required: true, message: '请选择月份' }]}
          >
            <DatePicker picker="month" format="YYYY-MM" style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      <HandoverRecordPreview
        visible={previewVisible}
        onClose={() => setPreviewVisible(false)}
        equipmentId={previewParams?.equipmentId ?? null}
        recordMonth={previewParams?.recordMonth ?? ''}
        title={`交接班记录表预览 - ${previewParams?.recordMonth ?? ''}`}
      />

      <Modal
        title="查看图片"
        open={photoModalVisible}
        onCancel={handleClosePhotoModal}
        footer={null}
        width={600}
        destroyOnClose
      >
        <Spin spinning={photoLoading} tip="加载中...">
          <div style={{ textAlign: 'center', minHeight: 200 }}>
            {photoUrl && !photoLoading && (
              <img src={photoUrl} alt="交接班记录照片" style={{ maxWidth: '100%', maxHeight: '70vh' }} />
            )}
          </div>
        </Spin>
      </Modal>
    </div>
  );
};

export default HandoverList;
