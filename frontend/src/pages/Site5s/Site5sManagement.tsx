import React, { useState } from 'react';
import { Tabs } from 'antd';
import AreaList from './AreaList';
import AreaPhotoTask from './AreaPhotoTask';

const Site5sManagement: React.FC = () => {
  const [activeKey, setActiveKey] = useState('area');

  const tabItems = [
    {
      key: 'area',
      label: '区域管理',
      children: <AreaList />,
    },
    {
      key: 'area-photo',
      label: '区域拍照',
      children: <AreaPhotoTask />,
    },
  ];

  return (
    <div style={{ padding: '20px' }}>
      <Tabs activeKey={activeKey} onChange={setActiveKey} items={tabItems} />
    </div>
  );
};

export default Site5sManagement;
