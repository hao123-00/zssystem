import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, DatePicker, Space, message, Tag, Card, Select, Popconfirm } from 'antd';
import { ReloadOutlined, PlayCircleOutlined, DownloadOutlined, DeleteOutlined } from '@ant-design/icons';
import dayjs, { Dayjs } from 'dayjs';
import {
  generateSchedule,
  getScheduleList,
  ProductionScheduleInfo,
  ProductionScheduleQueryParams,
  ScheduleDayInfo,
  exportSchedule,
  deleteScheduleByMachineNo,
} from '@/api/production';
import { getOrderList } from '@/api/production';
import { exportExcel } from '@/utils/excel';
import './ProductionList.less';

const PlanList: React.FC = () => {
  const [form] = Form.useForm();
  const [scheduleList, setScheduleList] = useState<ProductionScheduleInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [machineNoList, setMachineNoList] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadMachineNoList();
    // 不自动查询，等用户选择机台号或点击查询
  }, []);

  const loadMachineNoList = async () => {
    try {
      setError(null);
      const response = await getOrderList({ pageNum: 1, pageSize: 1000 });
      const machineNos = Array.from(
        new Set((response.list || []).map((order) => order.machineNo).filter(Boolean))
      );
      setMachineNoList(machineNos);
    } catch (error: any) {
      console.error('加载机台号列表失败', error);
      setError('加载机台号列表失败: ' + (error.message || '未知错误'));
    }
  };

  const fetchList = async () => {
    setLoading(true);
    setError(null);
    try {
      const values = form.getFieldsValue();
      const params: ProductionScheduleQueryParams = {
        machineNo: values.machineNo,
        startDate: values.startDate ? values.startDate.format('YYYY-MM-DD') : undefined,
      };
      // 获取按机台号分组的排程列表
      const response = await getScheduleList(params);
      
      // 确保 response 是数组
      if (Array.isArray(response)) {
        setScheduleList(response);
      } else {
        setScheduleList([]);
      }
    } catch (error: any) {
      console.error('查询排程列表失败', error);
      // 如果是404或空数据，不显示错误，只显示空列表
      if (error.response?.status === 404) {
        setScheduleList([]);
      } else {
        setError(error.message || '查询排程列表失败');
        setScheduleList([]);
        message.error(error.message || '查询失败');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleGenerate = async (machineNo?: string) => {
    try {
      const values = form.getFieldsValue();
      const targetMachineNo = machineNo || values.machineNo;
      
      if (!targetMachineNo) {
        message.warning('请先选择机台号');
        return;
      }
      
      if (!values.startDate) {
        message.warning('请先选择排程开始日期');
        return;
      }
      
      const startDate = values.startDate.format('YYYY-MM-DD');
      
      setLoading(true);
      await generateSchedule(targetMachineNo, startDate);
      message.success('生成排程成功');
      fetchList();
    } catch (error: any) {
      message.error(error.message || '生成排程失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    fetchList();
  };

  const handleReset = () => {
    form.resetFields();
    fetchList();
  };

  const handleExport = async () => {
    try {
      const values = form.getFieldsValue();
      
      if (!values.startDate) {
        message.warning('请先选择排程开始日期');
        return;
      }
      
      // 根据排程开始日期生成本月内的日期列表（排除星期天），用于Excel列标题
      const dateList: string[] = [];
      let currentDate = values.startDate.clone();
      // 计算本月最后一天（排程开始日期所在月份的最后一天）
      const monthEndDate = currentDate.endOf('month');
      
      while (currentDate.isBefore(monthEndDate) || currentDate.isSame(monthEndDate, 'day')) {
        // dayjs中day()返回0-6，0是星期天
        if (currentDate.day() !== 0) {
          dateList.push(currentDate.format('YYYY-MM-DD'));
        }
        currentDate = currentDate.add(1, 'day');
      }
      
      // 根据排程开始日期导出所有符合该日期的机台号排程
      const params: ProductionScheduleQueryParams = {
        // 不传机台号，导出所有机台号
        machineNo: undefined,
        startDate: values.startDate.format('YYYY-MM-DD'),
        dateList: dateList, // 传递日期列表用于Excel列标题
      };
      
      const date = new Date().toISOString().split('T')[0].replace(/-/g, '');
      const filename = `生产管理_生产计划排程_${date}`;
      await exportExcel('/api/production/schedule/export', params, filename);
      message.success('导出成功');
    } catch (error: any) {
      message.error('导出失败：' + (error.message || '未知错误'));
    }
  };

  const handleDelete = async (machineNo: string) => {
    try {
      await deleteScheduleByMachineNo(machineNo);
      message.success('删除排程成功');
      fetchList();
    } catch (error: any) {
      message.error(error.message || '删除排程失败');
    }
  };

  // 构建表格列（动态日期列）
  const buildColumns = (schedule: ProductionScheduleInfo) => {
    const baseColumns = [
      {
        title: '机台号',
        dataIndex: 'machineNo',
        key: 'machineNo',
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
        title: '组别',
        dataIndex: 'groupName',
        key: 'groupName',
        width: 100,
        fixed: 'left' as const,
      },
    ];

    // 动态日期列
    const dateColumns = (schedule.scheduleDays || []).map((day: ScheduleDayInfo) => ({
      title: day.scheduleDate || '',
      key: `day_${day.dayNumber}`,
      width: 150,
      render: () => (
        <div>
          <div>{day.productName || '-'}</div>
          <div style={{ color: '#666', fontSize: '12px' }}>
            排产: {day.productionQuantity || 0} | 剩余: {day.remainingQuantity || 0}
          </div>
        </div>
      ),
    }));

    return [...baseColumns, ...dateColumns];
  };

  // 如果有错误，显示错误信息
  if (error) {
    return (
      <div className="production-list-container">
        <Card>
          <div style={{ textAlign: 'center', padding: '40px', color: '#ff4d4f' }}>
            <p>加载失败</p>
            <p style={{ marginTop: 16 }}>{error}</p>
            <Button type="primary" onClick={loadMachineNoList} style={{ marginTop: 16 }}>
              重试
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="production-list-container">
      <div className="search-form">
        <Form form={form} layout="inline">
          <Form.Item name="machineNo" label="机台号">
            <Select
              placeholder="请选择机台号"
              allowClear
              showSearch
              style={{ width: 200 }}
              options={machineNoList.map((no) => ({ value: no, label: no }))}
              onChange={(value) => {
                // 选择机台号后自动查询该机台号的排程
                if (value) {
                  fetchList();
                } else {
                  setScheduleList([]);
                }
              }}
            />
          </Form.Item>
          <Form.Item name="startDate" label="排程开始日期" rules={[{ required: true, message: '请选择排程开始日期' }]}>
            <DatePicker format="YYYY-MM-DD" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" onClick={handleSearch}>
                查询
              </Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Form.Item>
        </Form>
      </div>

      <div className="toolbar" style={{ marginBottom: 16 }}>
        <Space>
          <Button
            type="primary"
            icon={<PlayCircleOutlined />}
            onClick={() => handleGenerate()}
            loading={loading}
          >
            生成排程
          </Button>
          <Button
            type="primary"
            icon={<DownloadOutlined />}
            onClick={handleExport}
          >
            导出Excel
          </Button>
          <Button icon={<ReloadOutlined />} onClick={fetchList} loading={loading}>
            刷新
          </Button>
          <span style={{ color: '#999', marginLeft: 16 }}>
            提示：请先选择机台号，然后点击"生成排程"按钮
          </span>
        </Space>
      </div>

      <div className="schedule-list">
        {scheduleList.length === 0 ? (
          <Card>
            <div style={{ textAlign: 'center', padding: '40px' }}>
              <p>暂无排程数据</p>
              <p style={{ marginTop: 16, color: '#999' }}>
                请先选择机台号，然后点击"生成排程"按钮
              </p>
            </div>
          </Card>
        ) : (
          scheduleList.map((schedule) => (
            <Card
              key={schedule.machineNo}
              title={
                <Space>
                  <span>机台号: {schedule.machineNo}</span>
                  {schedule.equipmentName && <span>设备: {schedule.equipmentName}</span>}
                  {schedule.groupName && <span>组别: {schedule.groupName}</span>}
                  <Tag color={schedule.canCompleteTarget ? 'success' : 'error'}>
                    {schedule.canCompleteTarget ? '可完成目标' : '无法完成目标'}
                  </Tag>
                  <Button
                    type="primary"
                    size="small"
                    icon={<PlayCircleOutlined />}
                    onClick={() => handleGenerate(schedule.machineNo)}
                  >
                    重新生成
                  </Button>
                  <Popconfirm
                    title="确定要删除该机台号的所有排程计划吗？"
                    onConfirm={() => handleDelete(schedule.machineNo)}
                    okText="确定"
                    cancelText="取消"
                  >
                    <Button
                      type="primary"
                      danger
                      size="small"
                      icon={<DeleteOutlined />}
                    >
                      删除
                    </Button>
                  </Popconfirm>
                </Space>
              }
              style={{ marginBottom: 16 }}
            >
              <Table
                columns={buildColumns(schedule)}
                dataSource={[{ ...schedule }]}
                rowKey="machineNo"
                loading={loading}
                pagination={false}
                scroll={{ x: 'max-content' }}
              />
            </Card>
          ))
        )}
      </div>
    </div>
  );
};

export default PlanList;
