import React from 'react';
import { Tree } from 'antd';
import { PermissionTreeVO } from '@/api/permission';

interface PermissionTreeProps {
  value?: number[];
  onChange?: (checkedKeys: number[]) => void;
  treeData: PermissionTreeVO[];
  checkable?: boolean;
}

const PermissionTree: React.FC<PermissionTreeProps> = ({
  value = [],
  onChange,
  treeData,
  checkable = true,
}) => {
  const convertToTreeData = (nodes: PermissionTreeVO[]): any[] => {
    return nodes.map((node) => ({
      title: `${node.permissionName} (${node.permissionCode})`,
      key: node.id,
      children: node.children && node.children.length > 0 ? convertToTreeData(node.children) : undefined,
    }));
  };

  const onCheck = (checkedKeys: any) => {
    const keys = Array.isArray(checkedKeys) ? checkedKeys : checkedKeys.checked || [];
    onChange?.(keys.map((k: any) => (typeof k === 'number' ? k : parseInt(k))));
  };

  return (
    <Tree
      checkable={checkable}
      checkedKeys={value}
      onCheck={onCheck}
      treeData={convertToTreeData(treeData)}
      defaultExpandAll
    />
  );
};

export default PermissionTree;
