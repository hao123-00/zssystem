import React from 'react';
import { Tabs } from 'antd';
import OrderList from './OrderList';
import PlanList from './PlanList';
import RecordList from './RecordList';

const ProductionManagement: React.FC = () => {
  const tabItems = [
    {
      key: 'order',
      label: '生产订单',
      children: <OrderList />,
    },
    {
      key: 'plan',
      label: '生产计划',
      children: <PlanList />,
    },
    {
      key: 'record',
      label: '生产记录',
      children: <RecordList />,
    },
  ];

  return (
    <div style={{ padding: '24px', background: '#fff' }}>
      <Tabs items={tabItems} />
    </div>
  );
};

export default ProductionManagement;
