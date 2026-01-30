import React, { useState } from 'react';
import { Form, Button, Space, Collapse } from 'antd';
import { SearchOutlined, ReloadOutlined, DownOutlined, UpOutlined } from '@ant-design/icons';
import { useResponsive } from '@/hooks/useResponsive';
import type { FormInstance } from 'antd';
import './ResponsiveSearch.less';

interface ResponsiveSearchProps {
  form: FormInstance;
  onSearch: () => void;
  onReset: () => void;
  children: React.ReactNode;
  // 额外的操作按钮（如新增按钮）
  extra?: React.ReactNode;
  // 移动端默认是否展开搜索
  defaultExpanded?: boolean;
}

const ResponsiveSearch: React.FC<ResponsiveSearchProps> = ({
  form,
  onSearch,
  onReset,
  children,
  extra,
  defaultExpanded = false,
}) => {
  const { isMobile } = useResponsive();
  const [expanded, setExpanded] = useState(defaultExpanded);

  const handleReset = () => {
    form.resetFields();
    onReset();
  };

  // 移动端布局
  if (isMobile) {
    return (
      <div className="responsive-search responsive-search-mobile">
        {/* 顶部操作栏 */}
        <div className="search-header">
          <Button
            type="text"
            onClick={() => setExpanded(!expanded)}
            className="search-toggle"
          >
            <SearchOutlined />
            <span>筛选</span>
            {expanded ? <UpOutlined /> : <DownOutlined />}
          </Button>
          {extra && <div className="search-extra">{extra}</div>}
        </div>

        {/* 可折叠的搜索表单 */}
        <Collapse
          activeKey={expanded ? ['search'] : []}
          ghost
          className="search-collapse"
        >
          <Collapse.Panel header="" key="search" showArrow={false}>
            <Form
              form={form}
              layout="vertical"
              className="search-form search-form-mobile"
            >
              {children}
              <div className="search-actions">
                <Space style={{ width: '100%' }}>
                  <Button
                    type="primary"
                    icon={<SearchOutlined />}
                    onClick={onSearch}
                    block
                  >
                    查询
                  </Button>
                  <Button
                    icon={<ReloadOutlined />}
                    onClick={handleReset}
                    block
                  >
                    重置
                  </Button>
                </Space>
              </div>
            </Form>
          </Collapse.Panel>
        </Collapse>
      </div>
    );
  }

  // PC 端布局
  return (
    <div className="responsive-search responsive-search-desktop">
      <Form form={form} layout="inline" className="search-form">
        {children}
        <Form.Item>
          <Space>
            <Button type="primary" icon={<SearchOutlined />} onClick={onSearch}>
              查询
            </Button>
            <Button icon={<ReloadOutlined />} onClick={handleReset}>
              重置
            </Button>
            {extra}
          </Space>
        </Form.Item>
      </Form>
    </div>
  );
};

export default ResponsiveSearch;
