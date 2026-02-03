import React, { useState } from 'react';
import { Tabs } from 'antd';
import CheckList from './CheckList';
import RectificationList from './RectificationList';
import AreaList from './AreaList';
import AreaPhotoTask from './AreaPhotoTask';

const Site5sManagement: React.FC = () => {
  const [activeKey, setActiveKey] = useState('check');

  const tabItems = [
    {
      key: 'check',
      label: '5S检查记录',
      children: <CheckList />,
    },
    {
      key: 'rectification',
      label: '整改任务管理',
      children: <RectificationList />,
    },
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
      <Tabs
        activeKey={activeKey}
        onChange={setActiveKey}
        items={tabItems}
      />
    </div>
  );
};

export default Site5sManagement;
