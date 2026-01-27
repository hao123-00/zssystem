import request from '@/utils/request';

// ========== 5S检查记录 ==========
export interface Site5sCheckInfo {
  id?: number;
  checkNo?: string;
  checkDate: string;
  checkArea: string;
  checkerName?: string;
  sortScore?: number; // 整理得分（0-20）
  setScore?: number; // 整顿得分（0-20）
  shineScore?: number; // 清扫得分（0-20）
  standardizeScore?: number; // 清洁得分（0-20）
  sustainScore?: number; // 素养得分（0-20）
  totalScore?: number; // 总分（0-100）
  problemDescription?: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface Site5sCheckQueryParams {
  checkDate?: string;
  checkArea?: string;
  checkerName?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface Site5sCheckSaveParams {
  id?: number;
  checkDate: string;
  checkArea: string;
  checkerName?: string;
  sortScore?: number;
  setScore?: number;
  shineScore?: number;
  standardizeScore?: number;
  sustainScore?: number;
  problemDescription?: string;
  remark?: string;
}

// ========== 5S整改任务 ==========
export interface Site5sRectificationInfo {
  id?: number;
  taskNo?: string;
  checkId?: number;
  checkNo?: string;
  problemDescription: string;
  area: string;
  department?: string;
  responsiblePerson?: string;
  deadline?: string;
  rectificationContent?: string;
  rectificationDate?: string;
  verifierName?: string;
  verificationDate?: string;
  verificationResult?: string;
  status?: number; // 0-待整改，1-整改中，2-待验证，3-已完成
  statusText?: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface Site5sRectificationQueryParams {
  taskNo?: string;
  area?: string;
  department?: string;
  responsiblePerson?: string;
  status?: number;
  deadline?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface Site5sRectificationSaveParams {
  id?: number;
  checkId?: number;
  problemDescription: string;
  area: string;
  department?: string;
  responsiblePerson?: string;
  deadline?: string;
  rectificationContent?: string;
  rectificationDate?: string;
  verifierName?: string;
  verificationDate?: string;
  verificationResult?: string;
  status?: number;
  remark?: string;
}

// ========== API 接口 ==========

// 5S检查记录
export const getSite5sCheckList = (params: Site5sCheckQueryParams) => {
  return request.get('/api/site5s/check/list', { params });
};

export const getSite5sCheckById = (id: number) => {
  return request.get(`/api/site5s/check/${id}`);
};

export const saveSite5sCheck = (data: Site5sCheckSaveParams) => {
  return request.post('/api/site5s/check', data);
};

export const deleteSite5sCheck = (id: number) => {
  return request.delete(`/api/site5s/check/${id}`);
};

// 5S整改任务
export const getSite5sRectificationList = (params: Site5sRectificationQueryParams) => {
  return request.get('/api/site5s/rectification/list', { params });
};

export const getSite5sRectificationById = (id: number) => {
  return request.get(`/api/site5s/rectification/${id}`);
};

export const saveSite5sRectification = (data: Site5sRectificationSaveParams) => {
  return request.post('/api/site5s/rectification', data);
};

export const deleteSite5sRectification = (id: number) => {
  return request.delete(`/api/site5s/rectification/${id}`);
};

export const createRectificationFromCheck = (checkId: number) => {
  return request.post(`/api/site5s/rectification/create-from-check/${checkId}`);
};
