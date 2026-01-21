import request from '@/utils/request';

export interface DepartmentTreeVO {
  id: number;
  parentId: number;
  deptName: string;
  deptCode?: string;
  leader?: string;
  phone?: string;
  email?: string;
  sortOrder: number;
  status: number;
  createTime?: string;
  updateTime?: string;
  children?: DepartmentTreeVO[];
}

export interface DepartmentSaveParams {
  id?: number;
  parentId?: number;
  deptName: string;
  deptCode?: string;
  leader?: string;
  phone?: string;
  email?: string;
  sortOrder?: number;
  status?: number;
}

// 获取部门树
export const getDepartmentTree = () => {
  return request.get<DepartmentTreeVO[]>('/department/tree');
};

// 获取部门详情
export const getDepartmentById = (id: number) => {
  return request.get<DepartmentTreeVO>(`/department/${id}`);
};

// 创建部门
export const createDepartment = (data: DepartmentSaveParams) => {
  return request.post<void>('/department', data);
};

// 更新部门
export const updateDepartment = (id: number, data: DepartmentSaveParams) => {
  return request.put<void>(`/department/${id}`, data);
};

// 删除部门
export const deleteDepartment = (id: number) => {
  return request.delete<void>(`/department/${id}`);
};
