import React from 'react';
import { Card, Space, Button, Dropdown, Spin, Empty, Pagination } from 'antd';
import { MoreOutlined } from '@ant-design/icons';
import type { MenuProps } from 'antd';
import './MobileCard.less';

// 字段配置类型
export interface FieldConfig {
  key: string;
  label: string;
  render?: (value: any, record: any) => React.ReactNode;
}

// 操作按钮配置类型
export interface ActionConfig {
  key: string;
  label: string;
  icon?: React.ReactNode;
  danger?: boolean;
  onClick: (record: any) => void;
  show?: (record: any) => boolean; // 控制是否显示
}

interface MobileCardListProps<T = any> {
  dataSource: T[];
  loading?: boolean;
  rowKey: string | ((record: T) => string);
  // 标题字段（显示在卡片顶部）
  titleField?: FieldConfig;
  // 副标题字段
  subtitleField?: FieldConfig;
  // 主要字段列表
  fields: FieldConfig[];
  // 操作按钮
  actions?: ActionConfig[];
  // 分页配置
  pagination?: {
    current: number;
    pageSize: number;
    total: number;
    onChange: (page: number, pageSize: number) => void;
  };
  // 空状态描述
  emptyText?: string;
}

function MobileCardList<T extends Record<string, any>>({
  dataSource,
  loading = false,
  rowKey,
  titleField,
  subtitleField,
  fields,
  actions = [],
  pagination,
  emptyText = '暂无数据',
}: MobileCardListProps<T>) {
  const getRowKey = (record: T, index: number): string => {
    if (typeof rowKey === 'function') {
      return rowKey(record);
    }
    return record[rowKey]?.toString() || index.toString();
  };

  const renderFieldValue = (field: FieldConfig, record: T) => {
    const value = record[field.key];
    if (field.render) {
      return field.render(value, record);
    }
    return value ?? '-';
  };

  const getActionMenuItems = (record: T): MenuProps['items'] => {
    return actions
      .filter((action) => !action.show || action.show(record))
      .map((action) => ({
        key: action.key,
        label: (
          <span style={{ color: action.danger ? '#ff4d4f' : undefined }}>
            {action.icon} {action.label}
          </span>
        ),
        onClick: () => action.onClick(record),
      }));
  };

  if (loading) {
    return (
      <div className="mobile-card-loading">
        <Spin size="large" />
      </div>
    );
  }

  if (!dataSource || dataSource.length === 0) {
    return <Empty description={emptyText} className="mobile-card-empty" />;
  }

  return (
    <div className="mobile-card-list">
      {dataSource.map((record, index) => {
        const key = getRowKey(record, index);
        const menuItems = getActionMenuItems(record);

        return (
          <Card key={key} className="mobile-card-item" size="small">
            {/* 卡片头部 */}
            <div className="mobile-card-header">
              <div className="mobile-card-title-area">
                {titleField && (
                  <div className="mobile-card-title">
                    {renderFieldValue(titleField, record)}
                  </div>
                )}
                {subtitleField && (
                  <div className="mobile-card-subtitle">
                    {renderFieldValue(subtitleField, record)}
                  </div>
                )}
              </div>
              {actions.length > 0 && menuItems && menuItems.length > 0 && (
                <Dropdown menu={{ items: menuItems }} trigger={['click']}>
                  <Button type="text" icon={<MoreOutlined />} className="mobile-card-more" />
                </Dropdown>
              )}
            </div>

            {/* 卡片内容 */}
            <div className="mobile-card-body">
              {fields.map((field) => (
                <div key={field.key} className="mobile-card-field">
                  <span className="mobile-card-field-label">{field.label}：</span>
                  <span className="mobile-card-field-value">
                    {renderFieldValue(field, record)}
                  </span>
                </div>
              ))}
            </div>
          </Card>
        );
      })}

      {/* 分页 */}
      {pagination && pagination.total > 0 && (
        <div className="mobile-card-pagination">
          <Pagination
            current={pagination.current}
            pageSize={pagination.pageSize}
            total={pagination.total}
            onChange={pagination.onChange}
            size="small"
            simple
            showTotal={(total) => `共 ${total} 条`}
          />
        </div>
      )}
    </div>
  );
}

export default MobileCardList;
