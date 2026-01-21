import request from '@/utils/request';

export interface EmployeeInfo {
  id: number;
  employeeNo: string;
  name: string;
  gender?: number; // 0-女，1-男
  age?: number;
  phone?: string;
  email?: string;
  departmentId: number;
  departmentName?: string;
  position?: string;
  entryDate?: string;
  status: number; // 0-离职，1-在职
  createTime?: string;
  updateTime?: string;
}

export interface EmployeeQueryParams {
  name?: string;
  employeeNo?: string;
  departmentId?: number;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface EmployeeSaveParams {
  id?: number;
  employeeNo: string;
  name: string;
  gender?: number;
  age?: number;
  phone?: string;
  email?: string;
  departmentId: number;
  position?: string;
  entryDate?: string;
  status: number;
}

// 获取员工列表（分页）
export const getEmployeeList = (params: EmployeeQueryParams) => {
  return request.get<{
    list: EmployeeInfo[];
    total: number;
  }>('/employee/list', { params });
};

// 获取员工详情
export const getEmployeeById = (id: number) => {
  return request.get<EmployeeInfo>(`/employee/${id}`);
};

// 创建员工
export const createEmployee = (data: EmployeeSaveParams) => {
  return request.post<void>('/employee', data);
};

// 更新员工
export const updateEmployee = (id: number, data: EmployeeSaveParams) => {
  return request.put<void>(`/employee/${id}`, data);
};

// 删除员工
export const deleteEmployee = (id: number) => {
  return request.delete<void>(`/employee/${id}`);
};
