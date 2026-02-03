import React, { useState } from 'react';
import { Layout as AntLayout, Menu, Button, Drawer } from 'antd';
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
  MenuOutlined,
  CloseOutlined,
} from '@ant-design/icons';
import { logout } from '@/utils/auth';
import { useResponsive } from '@/hooks/useResponsive';
import './Layout.less';

const { Header, Sider, Content } = AntLayout;

const Layout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isMobile } = useResponsive();
  const [drawerVisible, setDrawerVisible] = useState(false);

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
        {
          key: '/production/handover',
          label: '交接班记录',
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
    // 移动端点击菜单后关闭抽屉
    if (isMobile) {
      setDrawerVisible(false);
    }
  };

  const handleLogout = () => {
    logout();
  };

  // PC端菜单组件（深色主题）
  const pcMenuContent = (
    <Menu
      mode="inline"
      selectedKeys={[location.pathname]}
      defaultOpenKeys={['/production']}
      items={menuItems}
      onClick={handleMenuClick}
      className="layout-menu"
    />
  );

  // 移动端菜单组件（浅色主题）
  const mobileMenuContent = (
    <Menu
      mode="inline"
      theme="light"
      selectedKeys={[location.pathname]}
      defaultOpenKeys={[]}
      items={menuItems}
      onClick={handleMenuClick}
      className="layout-menu"
    />
  );

  // 移动端布局
  if (isMobile) {
    return (
      <AntLayout className="layout-container layout-mobile">
        <Header className="layout-header layout-header-mobile">
          <Button
            type="text"
            icon={<MenuOutlined />}
            onClick={() => setDrawerVisible(true)}
            className="menu-trigger"
          />
          <div className="logo-mobile">注塑部管理系统</div>
          <Button type="link" onClick={handleLogout} className="logout-btn-mobile">
            退出
          </Button>
        </Header>
        <Content className="layout-content layout-content-mobile">
          <Outlet />
        </Content>
        <Drawer
          title={
            <div className="drawer-header">
              <span>导航菜单</span>
              <Button
                type="text"
                icon={<CloseOutlined />}
                onClick={() => setDrawerVisible(false)}
              />
            </div>
          }
          placement="left"
          onClose={() => setDrawerVisible(false)}
          open={drawerVisible}
          width={280}
          className="mobile-drawer"
          closeIcon={null}
          styles={{ body: { padding: 0 } }}
        >
          {mobileMenuContent}
        </Drawer>
      </AntLayout>
    );
  }

  // PC 端布局
  return (
    <AntLayout className="layout-container">
      <Sider width={200} className="layout-sider">
        <div className="logo">注塑部管理系统</div>
        {pcMenuContent}
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
