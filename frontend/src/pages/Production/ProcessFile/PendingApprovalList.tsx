import React, { useState, useEffect } from 'react';
import { Table, Button, Tag, Space, message, Card } from 'antd';
import { EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getPendingApprovalFiles } from '@/api/processFile';
import type { ProcessFileInfo } from '@/api/processFile';

/**
 * 待审批列表页
 */
const PendingApprovalList: React.FC = () => {
  const navigate = useNavigate();
  const [tableData, setTableData] = useState<ProcessFileInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  useEffect(() => {
    fetchList();
  }, [pagination.current, pagination.pageSize]);

  const fetchList = async () => {
    setLoading(true);
    try {
      console.log('正在查询待审批文件列表，页码:', pagination.current, '每页:', pagination.pageSize);
      const response = await getPendingApprovalFiles(
        pagination.current,
        pagination.pageSize
      );
      console.log('待审批文件列表响应:', response);
      
      // request拦截器已经返回了data，所以response直接就是 {list, total}
      if (response && response.list) {
        setTableData(response.list);
        setPagination({ ...pagination, total: response.total || 0 });
        console.log('待审批文件列表加载成功，共', response.list.length, '条数据');
      } else {
        setTableData([]);
        setPagination({ ...pagination, total: 0 });
        console.warn('待审批文件列表为空或格式不正确:', response);
      }
    } catch (error: any) {
      console.error('查询待审批文件列表失败:', error);
      message.error('查询失败: ' + (error.message || '未知错误'));
      setTableData([]);
      setPagination({ ...pagination, total: 0 });
    } finally {
      setLoading(false);
    }
  };

  // 审批流程：车间主任审核(1) → 生产技术部经理批准(2) → 注塑部经理会签(3)
  const getStatusTag = (status: number) => {
    const statusConfig: { [key: number]: { color: string; text: string } } = {
      1: { color: 'processing', text: '待车间主任审核' },
      2: { color: 'processing', text: '待生产技术部经理批准' },
      3: { color: 'processing', text: '待注塑部经理会签' },
    };
    const config = statusConfig[status] || { color: 'default', text: '未知' };
    return <Tag color={config.color}>{config.text}</Tag>;
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
      title: '当前状态',
      dataIndex: 'status',
      key: 'status',
      width: 180,
      render: (status: number) => getStatusTag(status),
    },
    {
      title: '创建人',
      dataIndex: 'creatorName',
      key: 'creatorName',
      width: 100,
    },
    {
      title: '提交时间',
      dataIndex: 'submitTime',
      key: 'submitTime',
      width: 160,
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right' as const,
      width: 120,
      render: (_: any, record: ProcessFileInfo) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/production/process-file/detail/${record.id}`)}
          >
            审批
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className="pending-approval-list">
      <Card title="待我审批">
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
          scroll={{ x: 1200 }}
        />
      </Card>
    </div>
  );
};

export default PendingApprovalList;
