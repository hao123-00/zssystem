import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Select, Space, message, Popconfirm, Tag, DatePicker, Modal } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, DownloadOutlined } from '@ant-design/icons';
import {
  getCheckList,
  deleteCheck,
  exportCheckExcel,
  EquipmentCheckInfo,
  EquipmentCheckQueryParams,
} from '@/api/equipment';
import CheckModal from './CheckModal';
import EquipmentCheckPreview from '@/components/EquipmentCheckPreview/EquipmentCheckPreview';
import { getEquipmentList } from '@/api/equipment';
import { useResponsive } from '@/hooks/useResponsive';
import { ResponsiveSearch } from '@/components/ResponsiveSearch';
import { MobileCardList, FieldConfig, ActionConfig } from '@/components/MobileCard';
import './CheckList.less';

const CheckList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState<EquipmentCheckInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 30, // 默认30条（一个月）
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingCheck, setEditingCheck] = useState<EquipmentCheckInfo | null>(null);
  const [equipmentList, setEquipmentList] = useState<any[]>([]);
  const [exportModalVisible, setExportModalVisible] = useState(false);
  const [exportEquipmentId, setExportEquipmentId] = useState<number | undefined>();
  const [exportMonth, setExportMonth] = useState<any>(null);
  const [exporting, setExporting] = useState(false);
  const [previewVisible, setPreviewVisible] = useState(false);
  const { isMobile } = useResponsive();

  useEffect(() => {
    loadEquipmentList();
    fetchList();
  }, [pagination.current, pagination.pageSize]);

  const loadEquipmentList = async () => {
    try {
      const response = await getEquipmentList({ pageNum: 1, pageSize: 1000 });
      setEquipmentList(response.list || []);
    } catch (error: any) {
      console.error('加载设备列表失败', error);
    }
  };

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params: EquipmentCheckQueryParams = {
        ...values,
        checkMonth: values.checkMonth ? values.checkMonth.format('YYYY-MM') : undefined,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getCheckList(params);
      setTableData(response.list || []);
      setPagination({ ...pagination, total: response.total || 0 });
    } catch (error: any) {
      message.error(error.message || '查询失败');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingCheck(null);
    setModalVisible(true);
  };

  const handleEdit = (check: EquipmentCheckInfo) => {
    setEditingCheck(check);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteCheck(id);
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

  const handleTableChange = (page: number, pageSize: number) => {
    setPagination({ ...pagination, current: page, pageSize });
  };

  const handleExportOpen = () => {
    setExportModalVisible(true);
    setExportEquipmentId(undefined);
    setExportMonth(null);
  };

  const handlePreview = () => {
    if (exportEquipmentId == null) {
      message.warning('请选择设备');
      return;
    }
    if (!exportMonth) {
      message.warning('请选择月份');
      return;
    }
    setPreviewVisible(true);
  };

  const handleExportOk = async () => {
    if (exportEquipmentId == null) {
      message.warning('请选择设备');
      return;
    }
    if (!exportMonth) {
      message.warning('请选择月份');
      return;
    }
    const checkMonth = exportMonth.format('YYYY-MM');
    setExporting(true);
    try {
      const res = await exportCheckExcel(exportEquipmentId, checkMonth);
      const blob = res instanceof Blob ? res : (res?.data instanceof Blob ? res.data : new Blob([res?.data ?? []]));
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `点检表_${checkMonth}.xlsx`;
      a.click();
      window.URL.revokeObjectURL(url);
      message.success('导出成功');
      setExportModalVisible(false);
    } catch (error: any) {
      message.error(error.message || '导出失败');
    } finally {
      setExporting(false);
    }
  };

  const renderCheckItem = (value: number | undefined) => {
    if (value === undefined || value === null) {
      return <Tag>-</Tag>;
    }
    return value === 1 ? (
      <Tag color="success">正常</Tag>
    ) : (
      <Tag color="error">异常</Tag>
    );
  };

  const columns = [
    {
      title: '检查日期',
      dataIndex: 'checkDate',
      key: 'checkDate',
      width: 120,
      fixed: 'left' as const,
    },
    {
      title: '设备编号',
      dataIndex: 'equipmentNo',
      key: 'equipmentNo',
      width: 120,
      fixed: 'left' as const,
    },
    {
      title: '设备名称',
      dataIndex: 'equipmentName',
      key: 'equipmentName',
      width: 150,
      fixed: 'left' as const,
    },
    {
      title: '检点人',
      dataIndex: 'checkerName',
      key: 'checkerName',
      width: 100,
    },
    {
      title: '电路1',
      dataIndex: 'circuitItem1',
      key: 'circuitItem1',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '电路2',
      dataIndex: 'circuitItem2',
      key: 'circuitItem2',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '电路3',
      dataIndex: 'circuitItem3',
      key: 'circuitItem3',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '机架1',
      dataIndex: 'frameItem1',
      key: 'frameItem1',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '机架2',
      dataIndex: 'frameItem2',
      key: 'frameItem2',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '机架3',
      dataIndex: 'frameItem3',
      key: 'frameItem3',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '油路1',
      dataIndex: 'oilItem1',
      key: 'oilItem1',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '油路2',
      dataIndex: 'oilItem2',
      key: 'oilItem2',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '油路3',
      dataIndex: 'oilItem3',
      key: 'oilItem3',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '油路4',
      dataIndex: 'oilItem4',
      key: 'oilItem4',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '油路5',
      dataIndex: 'oilItem5',
      key: 'oilItem5',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '周边1',
      dataIndex: 'peripheralItem1',
      key: 'peripheralItem1',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '周边2',
      dataIndex: 'peripheralItem2',
      key: 'peripheralItem2',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '周边3',
      dataIndex: 'peripheralItem3',
      key: 'peripheralItem3',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '周边4',
      dataIndex: 'peripheralItem4',
      key: 'peripheralItem4',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '周边5',
      dataIndex: 'peripheralItem5',
      key: 'peripheralItem5',
      width: 80,
      render: renderCheckItem,
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      width: 200,
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_: any, record: EquipmentCheckInfo) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm title="确定要删除吗？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // 计算异常项数量
  const countAbnormalItems = (record: EquipmentCheckInfo): number => {
    const checkItems = [
      record.circuitItem1, record.circuitItem2, record.circuitItem3,
      record.frameItem1, record.frameItem2, record.frameItem3,
      record.oilItem1, record.oilItem2, record.oilItem3, record.oilItem4, record.oilItem5,
      record.peripheralItem1, record.peripheralItem2, record.peripheralItem3, record.peripheralItem4, record.peripheralItem5,
    ];
    return checkItems.filter(item => item === 0).length;
  };

  // 移动端卡片字段配置
  const mobileFields: FieldConfig[] = [
    { key: 'equipmentNo', label: '设备编号' },
    { key: 'checkerName', label: '检点人' },
    {
      key: 'abnormalCount',
      label: '异常项',
      render: (_: any, record: EquipmentCheckInfo) => {
        const count = countAbnormalItems(record);
        return count > 0 ? (
          <Tag color="error">{count} 项异常</Tag>
        ) : (
          <Tag color="success">全部正常</Tag>
        );
      },
    },
    { key: 'remark', label: '备注' },
  ];

  // 移动端操作按钮配置
  const mobileActions: ActionConfig[] = [
    {
      key: 'edit',
      label: '编辑',
      icon: <EditOutlined />,
      onClick: (record) => handleEdit(record),
    },
    {
      key: 'delete',
      label: '删除',
      icon: <DeleteOutlined />,
      danger: true,
      onClick: (record) => handleDelete(record.id),
    },
  ];

  return (
    <div className={`check-list-container ${isMobile ? 'check-list-mobile' : ''}`}>
      <ResponsiveSearch
        form={form}
        onSearch={handleSearch}
        onReset={handleReset}
        extra={
          <Space>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              {isMobile ? '新增' : '新增点检'}
            </Button>
            <Button icon={<DownloadOutlined />} onClick={handleExportOpen}>
              {isMobile ? '导出' : '导出30天点检表'}
            </Button>
          </Space>
        }
      >
        <Form.Item name="equipmentNo" label={isMobile ? '设备' : undefined}>
          <Select
            placeholder="设备编号"
            allowClear
            showSearch
            filterOption={(input, option) =>
              (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
            }
            options={equipmentList.map((eq) => ({
              value: eq.equipmentNo,
              label: `${eq.equipmentNo} - ${eq.equipmentName}`,
            }))}
            style={{ width: isMobile ? '100%' : 200 }}
          />
        </Form.Item>
        <Form.Item name="checkMonth" label={isMobile ? '月份' : undefined}>
          <DatePicker picker="month" placeholder="检查月份" format="YYYY-MM" style={{ width: isMobile ? '100%' : undefined }} />
        </Form.Item>
        <Form.Item name="checkerName" label={isMobile ? '检点人' : undefined}>
          <Input placeholder="检点人" allowClear />
        </Form.Item>
      </ResponsiveSearch>

      {isMobile ? (
        <MobileCardList
          dataSource={tableData}
          loading={loading}
          rowKey="id"
          titleField={{ key: 'checkDate', label: '检查日期' }}
          subtitleField={{ key: 'equipmentName', label: '设备名称' }}
          fields={mobileFields}
          actions={mobileActions}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            onChange: handleTableChange,
          }}
        />
      ) : (
        <Table
          columns={columns}
          dataSource={tableData}
          rowKey="id"
          loading={loading}
          scroll={{ x: 2000 }}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showTotal: (total) => `共 ${total} 条`,
            onChange: handleTableChange,
          }}
        />
      )}

      <CheckModal
        visible={modalVisible}
        check={editingCheck}
        onCancel={() => setModalVisible(false)}
        onSuccess={() => {
          setModalVisible(false);
          fetchList();
        }}
      />

      <Modal
        title="导出30天点检表"
        open={exportModalVisible}
        onCancel={() => setExportModalVisible(false)}
        footer={[
          <Button key="cancel" onClick={() => setExportModalVisible(false)}>
            取消
          </Button>,
          <Button key="preview" onClick={handlePreview}>
            预览
          </Button>,
          <Button key="export" type="primary" loading={exporting} onClick={handleExportOk}>
            导出
          </Button>,
        ]}
      >
        <Form layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item label="选择设备" required>
            <Select
              placeholder="请选择设备"
              allowClear
              showSearch
              optionFilterProp="label"
              value={exportEquipmentId}
              onChange={setExportEquipmentId}
              options={equipmentList.map((eq) => ({
                value: eq.id,
                label: `${eq.equipmentNo} - ${eq.equipmentName}`,
              }))}
              style={{ width: '100%' }}
            />
          </Form.Item>
          <Form.Item label="选择月份" required>
            <DatePicker
              picker="month"
              format="YYYY-MM"
              value={exportMonth}
              onChange={setExportMonth}
              style={{ width: '100%' }}
            />
          </Form.Item>
        </Form>
      </Modal>

      <EquipmentCheckPreview
        visible={previewVisible}
        onClose={() => setPreviewVisible(false)}
        equipmentId={exportEquipmentId ?? 0}
        checkMonth={exportMonth ? exportMonth.format('YYYY-MM') : ''}
        title={`点检表预览 - ${exportMonth ? exportMonth.format('YYYY-MM') : ''}`}
      />
    </div>
  );
};

export default CheckList;
