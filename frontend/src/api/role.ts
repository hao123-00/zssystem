import request from '@/utils/request';

export interface RoleInfo {
  id: number;
  roleCode: string;
  roleName: string;
  description?: string;
  status: number;
  createTime?: string;
  updateTime?: string;
  permissionIds?: number[];
}

export interface RoleQueryParams {
  roleName?: string;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface RoleSaveParams {
  id?: number;
  roleCode: string;
  roleName: string;
  description?: string;
  status: number;
  permissionIds?: number[];
}

// 获取角色列表（分页）
export const getRoleList = (params: RoleQueryParams) => {
  return request.get<{
    list: RoleInfo[];
    total: number;
  }>('/role/list', { params });
};

// 获取所有角色
export const getAllRoles = () => {
  return request.get<RoleInfo[]>('/role/all');
};

// 获取角色详情
export const getRoleById = (id: number) => {
  return request.get<RoleInfo>(`/role/${id}`);
};

// 创建角色
export const createRole = (data: RoleSaveParams) => {
  return request.post<void>('/role', data);
};

// 更新角色
export const updateRole = (id: number, data: RoleSaveParams) => {
  return request.put<void>(`/role/${id}`, data);
};

// 删除角色
export const deleteRole = (id: number) => {
  return request.delete<void>(`/role/${id}`);
};

// 分配权限
export const assignPermissions = (roleId: number, permissionIds: number[]) => {
  return request.post<void>(`/role/${roleId}/permissions`, permissionIds);
};

// 获取角色权限
export const getRolePermissions = (roleId: number) => {
  return request.get<number[]>(`/role/${roleId}/permissions`);
};

