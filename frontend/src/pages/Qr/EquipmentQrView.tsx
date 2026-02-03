import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Card, Spin, Tabs, Table, Tag, Button, Empty, message } from 'antd';
import { FilePdfOutlined } from '@ant-design/icons';
import { getEquipmentQrViewData, EquipmentQrViewData } from '@/api/equipment';
import request from '@/utils/request';
import './EquipmentQrView.less';

/**
 * 设备扫码查看页 - 微信扫码后展示点检记录和启用工艺卡（无需登录）
 */
const EquipmentQrView: React.FC = () => {
  const { equipmentId } = useParams<{ equipmentId: string }>();
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<EquipmentQrViewData | null>(null);
  const [activeTab, setActiveTab] = useState<string>('check');

  useEffect(() => {
    if (equipmentId) {
      fetchData();
    }
  }, [equipmentId]);

  useEffect(() => {
    if (activeTab === 'process' && equipmentId && data?.enabledProcessFile) {
      const baseURL = request.defaults.baseURL || '/api';
      window.open(`${baseURL}/qr/equipment/${equipmentId}/process-file/svg`, '_blank');
      message.success('正在打开工艺卡');
    }
  }, [activeTab, equipmentId, data?.enabledProcessFile]);

  const fetchData = async () => {
    if (!equipmentId) return;
    setLoading(true);
    try {
      const res = await getEquipmentQrViewData(Number(equipmentId));
      setData(res);
    } catch (e: any) {
      message.error(e.message || '加载失败');
      setData(null);
    } finally {
      setLoading(false);
    }
  };

  const handleViewCheckSvg = () => {
    if (!equipmentId) return;
    const baseURL = request.defaults.baseURL || '/api';
    window.open(`${baseURL}/qr/equipment/${equipmentId}/check/svg`, '_blank');
    message.success('正在打开点检表');
  };

  const handleViewHandoverSvg = () => {
    if (!equipmentId) return;
    const baseURL = request.defaults.baseURL || '/api';
    window.open(`${baseURL}/qr/equipment/${equipmentId}/handover/svg`, '_blank');
    message.success('正在打开交班记录表');
  };

  const renderCheckItem = (val: number | undefined) => {
    if (val === undefined || val === null) return <Tag>-</Tag>;
    return val === 1 ? <Tag color="success">√</Tag> : <Tag color="error">×</Tag>;
  };

  const checkColumns = [
    { title: '日期', dataIndex: 'checkDate', key: 'checkDate', width: 100 },
    { title: '检点人', dataIndex: 'checkerName', key: 'checkerName', width: 80 },
    { title: '电路1', dataIndex: 'circuitItem1', key: 'c1', width: 60, render: renderCheckItem },
    { title: '电路2', dataIndex: 'circuitItem2', key: 'c2', width: 60, render: renderCheckItem },
    { title: '电路3', dataIndex: 'circuitItem3', key: 'c3', width: 60, render: renderCheckItem },
    { title: '机架1', dataIndex: 'frameItem1', key: 'f1', width: 60, render: renderCheckItem },
    { title: '机架2', dataIndex: 'frameItem2', key: 'f2', width: 60, render: renderCheckItem },
    { title: '机架3', dataIndex: 'frameItem3', key: 'f3', width: 60, render: renderCheckItem },
    { title: '油路1', dataIndex: 'oilItem1', key: 'o1', width: 60, render: renderCheckItem },
    { title: '油路2', dataIndex: 'oilItem2', key: 'o2', width: 60, render: renderCheckItem },
    { title: '油路3', dataIndex: 'oilItem3', key: 'o3', width: 60, render: renderCheckItem },
    { title: '油路4', dataIndex: 'oilItem4', key: 'o4', width: 60, render: renderCheckItem },
    { title: '油路5', dataIndex: 'oilItem5', key: 'o5', width: 60, render: renderCheckItem },
    { title: '周边1', dataIndex: 'peripheralItem1', key: 'p1', width: 60, render: renderCheckItem },
    { title: '周边2', dataIndex: 'peripheralItem2', key: 'p2', width: 60, render: renderCheckItem },
    { title: '周边3', dataIndex: 'peripheralItem3', key: 'p3', width: 60, render: renderCheckItem },
    { title: '周边4', dataIndex: 'peripheralItem4', key: 'p4', width: 60, render: renderCheckItem },
    { title: '周边5', dataIndex: 'peripheralItem5', key: 'p5', width: 60, render: renderCheckItem },
    { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true },
  ];

  if (loading) {
    return (
      <div className="qr-equipment-view">
        <div className="qr-loading">
          <Spin size="large" tip="加载中..." />
        </div>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="qr-equipment-view">
        <Card><Empty description="设备不存在或加载失败" /></Card>
      </div>
    );
  }

  return (
    <div className="qr-equipment-view">
      <Card size="small" className="qr-header">
        <div className="qr-equipment-title">
          {data.equipment.equipmentName || data.equipment.equipmentNo}
        </div>
        <div className="qr-equipment-sub">
          机台号：{data.equipment.machineNo} | 设备编号：{data.equipment.equipmentNo}
        </div>
      </Card>

      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={[
          {
            key: 'check',
            label: `点检记录 (${data.checkMonth})`,
            children: (
              <Card size="small">
                <div style={{ marginBottom: 12 }}>
                  <Button
                    type="primary"
                    icon={<FilePdfOutlined />}
                    onClick={handleViewCheckSvg}
                  >
                    查看点检表
                  </Button>
                </div>
                {data.checkRecords.length > 0 ? (
                  <Table
                    dataSource={data.checkRecords}
                    columns={checkColumns}
                    rowKey="id"
                    size="small"
                    pagination={false}
                    scroll={{ x: 1200 }}
                  />
                ) : (
                  <Empty description={`本月暂无点检记录`} />
                )}
              </Card>
            ),
          },
          {
            key: 'process',
            label: '启用工艺卡',
            children: (
              <Card size="small">
                {data.enabledProcessFile ? (
                  <div className="qr-process-file-info">
                    <span>{data.enabledProcessFile.fileName}</span>
                    <Tag>{data.enabledProcessFile.versionText}</Tag>
                    <div style={{ marginTop: 12, color: '#666', fontSize: 14 }}>
                      工艺卡已在新窗口打开
                    </div>
                  </div>
                ) : (
                  <Empty description="该机台暂无启用的工艺卡" />
                )}
              </Card>
            ),
          },
          {
            key: 'handover',
            label: '交班记录表',
            children: (
              <Card size="small">
                <div style={{ marginBottom: 12 }}>
                  <Button
                    type="primary"
                    icon={<FilePdfOutlined />}
                    onClick={handleViewHandoverSvg}
                  >
                    查看交班记录表
                  </Button>
                </div>
                <div style={{ color: '#666', fontSize: 14 }}>
                  查看当月所有交接班记录，格式与导出 Excel 一致
                </div>
              </Card>
            ),
          },
        ]}
      />
    </div>
  );
};

export default EquipmentQrView;
