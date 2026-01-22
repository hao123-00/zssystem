import React from 'react';
import { Tabs } from 'antd';
import EquipmentList from './EquipmentList';
import CheckList from './CheckList';

const EquipmentManagement: React.FC = () => {
  const tabItems = [
    {
      key: 'equipment',
      label: '设备列表',
      children: <EquipmentList />,
    },
    {
      key: 'check',
      label: '设备点检',
      children: <CheckList />,
    },
  ];

  return (
    <div style={{ padding: '24px', background: '#fff' }}>
      <Tabs items={tabItems} />
    </div>
  );
};

export default EquipmentManagement;
