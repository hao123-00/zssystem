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
  return request.get('/site5s/check/list', { params });
};

export const getSite5sCheckById = (id: number) => {
  return request.get(`/site5s/check/${id}`);
};

export const saveSite5sCheck = (data: Site5sCheckSaveParams) => {
  return request.post('/site5s/check', data);
};

export const deleteSite5sCheck = (id: number) => {
  return request.delete(`/site5s/check/${id}`);
};

// 5S整改任务
export const getSite5sRectificationList = (params: Site5sRectificationQueryParams) => {
  return request.get('/site5s/rectification/list', { params });
};

export const getSite5sRectificationById = (id: number) => {
  return request.get(`/site5s/rectification/${id}`);
};

export const saveSite5sRectification = (data: Site5sRectificationSaveParams) => {
  return request.post('/site5s/rectification', data);
};

export const deleteSite5sRectification = (id: number) => {
  return request.delete(`/site5s/rectification/${id}`);
};

export const createRectificationFromCheck = (checkId: number) => {
  return request.post(`/site5s/rectification/create-from-check/${checkId}`);
};

// ========== 5S区域管理 ==========
export interface InjectionLeaderItem {
  id: number;
  name: string;
  username?: string;
}

export interface Site5sAreaInfo {
  id?: number;
  areaCode?: string;
  areaName?: string;
  checkItem?: string;
  responsibleUserId?: number;
  responsibleUserName?: string;
  responsibleUserId2?: number;
  responsibleUserName2?: string;
  morningPhotoTime?: string;
  eveningPhotoTime?: string;
  sortOrder?: number;
  status?: number;
  remark?: string;
}

export interface Site5sAreaQueryParams {
  areaName?: string;
  checkItem?: string;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface Site5sAreaSaveParams {
  id?: number;
  areaName: string;
  checkItem: string;
  responsibleUserId: number;
  responsibleUserId2: number;
  morningPhotoTime: string;  // "HH:mm"
  eveningPhotoTime: string;  // "HH:mm"
  sortOrder?: number;
  status?: number;
  remark?: string;
}

export interface AreaTaskSlot {
  slotIndex: number;
  scheduledTime: string;
  toleranceMinutes?: number;
  completed: boolean;
  onTime?: boolean;
  photoId?: number;
  uploaderName?: string;
  uploadTimeStr?: string;
}

export interface AreaTask {
  areaId: number;
  areaCode: string;
  areaName: string;
  checkItem: string;
  responsibleUserId?: number;
  responsibleUserName?: string;
  responsibleUserId2?: number;
  responsibleUserName2?: string;
  totalSlots: number;
  completedSlots: number;
  status: number;  // 0-异常，1-正常，2-放假
  dayOff?: boolean;
  slots: AreaTaskSlot[];
}

export interface AreaDailyStatus {
  statusDate: string;
  areas: AreaTask[];
}

// 5S区域
export const getSite5sAreaList = (params: Site5sAreaQueryParams) => {
  return request.get('/site5s/area/list', { params });
};

export const getSite5sAreaById = (id: number) => {
  return request.get(`/site5s/area/${id}`);
};

export const saveSite5sArea = (data: Site5sAreaSaveParams) => {
  return request.post('/site5s/area', data);
};

export const deleteSite5sArea = (id: number) => {
  return request.delete(`/site5s/area/${id}`);
};

/** 获取注塑组长列表（用于负责人选择） */
export const getInjectionLeaders = () => {
  return request.get<InjectionLeaderItem[]>('/site5s/area/injection-leaders');
};

/** 当前用户是否可管理区域（仅注塑部经理） */
export const getAreaCanManage = () => {
  return request.get<boolean>('/site5s/area/can-manage');
};

// 拍照任务
export const getSite5sAreaTasks = (photoDate: string) => {
  return request.get('/site5s/area/tasks', { params: { photoDate } });
};

/** 获取日期范围内的拍照任务（按日期每天一条记录） */
export const getSite5sAreaTasksRange = (startDate: string, endDate: string) => {
  return request.get<AreaDailyStatus[]>('/site5s/area/tasks-range', { params: { startDate, endDate } });
};

/** 设置/取消区域某日放假（仅注塑部经理） */
export const setSite5sAreaDayOff = (areaId: number, photoDate: string, dayOff: boolean) => {
  return request.post('/site5s/area/set-day-off', null, { params: { areaId, photoDate, dayOff } });
};

/** 灯光管理示意图：本月至今完成次数、拍照完成率 */
export interface LightingStats {
  completionCount: number;
  days: number;
  completionRate: number;
}
export const getLightingStats = () => request.get<LightingStats>('/site5s/area/lighting-stats');

// 上传照片
export const uploadSite5sAreaPhoto = (
  areaId: number,
  slotIndex: number,
  photoDate: string,
  file: File
) => {
  const formData = new FormData();
  formData.append('areaId', String(areaId));
  formData.append('slotIndex', String(slotIndex));
  formData.append('photoDate', photoDate);
  formData.append('file', file);
  return request.post<number>('/site5s/area/upload-photo', formData);
};

// 拍照记录
export const getSite5sAreaPhotoRecords = (params: {
  areaId?: number;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}) => {
  return request.get('/site5s/area/photo-records', { params });
};

/** 获取照片 Blob（需登录，用于 img 显示；blob 响应时拦截器返回完整 response） */
export const getSite5sAreaPhotoBlob = async (id: number): Promise<Blob> => {
  const res = await request.get(`/site5s/area/photo/${id}`, { responseType: 'blob' });
  return (res as { data?: Blob })?.data ?? (res as unknown as Blob);
};

/** 删除拍照记录（单条） */
export const deleteSite5sAreaPhoto = (id: number) => {
  return request.delete(`/site5s/area/photo/${id}`);
};

/** 删除区域某日全部拍照记录（早间+晚间），删除后可重新上传 */
export const deleteSite5sAreaPhotosByDay = (areaId: number, photoDate: string) => {
  return request.delete('/site5s/area/photo-by-day', { params: { areaId, photoDate } });
};
