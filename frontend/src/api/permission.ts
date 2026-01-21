import request from '@/utils/request';

export interface PermissionTreeVO {
  id: number;
  parentId: number;
  permissionCode: string;
  permissionName: string;
  permissionType: number; // 1-菜单，2-按钮
  path?: string;
  component?: string;
  icon?: string;
  sortOrder: number;
  status: number;
  children?: PermissionTreeVO[];
}

export interface PermissionSaveParams {
  id?: number;
  parentId?: number;
  permissionCode: string;
  permissionName: string;
  permissionType: number;
  path?: string;
  component?: string;
  icon?: string;
  sortOrder?: number;
  status?: number;
}

// 获取权限树
export const getPermissionTree = (type?: number) => {
  return request.get<PermissionTreeVO[]>('/permission/tree', {
    params: type ? { type } : {},
  });
};

// 获取权限详情
export const getPermissionById = (id: number) => {
  return request.get<PermissionTreeVO>(`/permission/${id}`);
};

// 创建权限
export const createPermission = (data: PermissionSaveParams) => {
  return request.post<void>('/permission', data);
};

// 更新权限
export const updatePermission = (id: number, data: PermissionSaveParams) => {
  return request.put<void>(`/permission/${id}`, data);
};

// 删除权限
export const deletePermission = (id: number) => {
  return request.delete<void>(`/permission/${id}`);
};

// 根据角色ID获取权限
export const getPermissionsByRoleId = (roleId: number) => {
  return request.get<PermissionTreeVO[]>(`/permission/role/${roleId}`);
};
