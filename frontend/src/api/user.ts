import request from '@/utils/request';

export interface UserQueryParams {
  username?: string;
  realName?: string;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface UserSaveParams {
  id?: number;
  username: string;
  password?: string;
  realName: string;
  email?: string;
  phone?: string;
  employeeNo?: string;
  team?: string;
  position?: string;
  category?: string;
  hireDate?: string;
  status: number;
  roleIds?: number[];
}

export interface UserInfo {
  id: number;
  username: string;
  name: string;
  realName?: string;
  email?: string;
  phone?: string;
  employeeNo?: string;
  team?: string;
  position?: string;
  category?: string;
  hireDate?: string;
  status: number;
  createTime: string;
  updateTime?: string;
  roles?: RoleInfo[];
}

export interface RoleInfo {
  id: number;
  roleCode: string;
  roleName: string;
  description?: string;
  status: number;
}

export const getUserList = (params: UserQueryParams) => {
  return request.get<{ list: UserInfo[]; total: number }>('/user/list', { params });
};

export const getUserById = (id: number) => {
  return request.get<UserInfo>(`/user/${id}`);
};

export const createUser = (data: UserSaveParams) => {
  return request.post('/user', data);
};

export const updateUser = (id: number, data: UserSaveParams) => {
  return request.put(`/user/${id}`, data);
};

export const deleteUser = (id: number) => {
  return request.delete(`/user/${id}`);
};

export const resetPassword = (id: number) => {
  return request.post(`/user/${id}/reset-password`);
};

export const enableUser = (id: number) => {
  return request.post(`/user/${id}/enable`);
};

export const disableUser = (id: number) => {
  return request.post(`/user/${id}/disable`);
};
