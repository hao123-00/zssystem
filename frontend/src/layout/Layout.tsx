import React from 'react';
import { Layout as AntLayout, Menu, Button } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  DashboardOutlined,
  UserOutlined,
  TeamOutlined,
  SettingOutlined,
  UsergroupAddOutlined,
  ApartmentOutlined,
  ShopOutlined,
  ToolOutlined,
  SafetyOutlined,
} from '@ant-design/icons';
import { logout } from '@/utils/auth';
import './Layout.less';

const { Header, Sider, Content } = AntLayout;

const Layout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: '首页',
    },
    {
      key: '/user',
      icon: <UserOutlined />,
      label: '用户管理',
    },
    {
      key: '/role',
      icon: <TeamOutlined />,
      label: '角色管理',
    },
    {
      key: '/permission',
      icon: <SettingOutlined />,
      label: '权限管理',
    },
    {
      key: '/employee',
      icon: <UsergroupAddOutlined />,
      label: '员工管理',
    },
    {
      key: '/department',
      icon: <ApartmentOutlined />,
      label: '部门管理',
    },
    {
      key: '/production',
      icon: <ShopOutlined />,
      label: '生产管理',
      children: [
        {
          key: '/production',
          label: '生产订单',
        },
        {
          key: '/production/process-file',
          label: '工艺文件管理',
        },
        {
          key: '/production/process-file/pending-approval',
          label: '待我审批',
        },
      ],
    },
    {
      key: '/equipment',
      icon: <ToolOutlined />,
      label: '设备管理',
    },
    {
      key: '/site5s',
      icon: <SafetyOutlined />,
      label: '现场5S管理',
    },
  ];

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key);
  };

  const handleLogout = () => {
    logout();
  };

  return (
    <AntLayout className="layout-container">
      <Sider width={200} className="layout-sider">
        <div className="logo">注塑部管理系统</div>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <AntLayout>
        <Header className="layout-header">
          <div className="header-right">
            <span>欢迎，管理员</span>
            <Button type="link" onClick={handleLogout}>
              退出
            </Button>
          </div>
        </Header>
        <Content className="layout-content">
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  );
};

export default Layout;
