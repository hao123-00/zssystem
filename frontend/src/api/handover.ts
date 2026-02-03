import request from '@/utils/request';

export interface HandoverRecordInfo {
  id: number;
  equipmentId: number;
  equipmentNo: string;
  recordDate: string;
  shift?: string;
  productName?: string;
  material?: string;
  equipmentCleaning?: string;
  floorCleaning?: string;
  leakage?: string;
  itemPlacement?: string;
  injectionMachine?: string;
  robot?: string;
  assemblyLine?: string;
  mold?: string;
  process?: string;
  handoverLeader?: string;
  receivingLeader?: string;
  createTime?: string;
  updateTime?: string;
  hasPhoto?: boolean;
}

export interface HandoverRecordQueryParams {
  equipmentId?: number;
  equipmentNo?: string;
  recordMonth?: string;  // YYYY-MM
  productName?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface HandoverRecordSaveParams {
  id?: number;  // 编辑时传
  equipmentId: number;
  /** 记录时间由服务端在提交时自动设置为当前时间 */
  recordDate?: string;
  shift?: string;
  productName?: string;
  material?: string;
  equipmentCleaning?: string;
  floorCleaning?: string;
  leakage?: string;
  itemPlacement?: string;
  injectionMachine?: string;
  robot?: string;
  assemblyLine?: string;
  mold?: string;
  process?: string;
  handoverLeader?: string;
  receivingLeader?: string;
  photoPath?: string;  // 新增时必填，由上传接口返回
}

export const getHandoverRecordList = (params: HandoverRecordQueryParams) => {
  return request.get<{ list: HandoverRecordInfo[]; total: number }>('/handover/list', { params });
};

export const getHandoverRecordById = (id: number) => {
  return request.get<HandoverRecordInfo>(`/handover/${id}`);
};

export const saveHandoverRecord = (data: HandoverRecordSaveParams) => {
  return request.post<void>('/handover', data);
};

/** 上传交接班拍照照片，返回存储路径 */
export const uploadHandoverPhoto = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<string>('/handover/upload-photo', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const deleteHandoverRecord = (id: number) => {
  return request.delete<void>(`/handover/${id}`);
};

export const getHandoverProductNames = (equipmentId: number) => {
  return request.get<string[]>('/handover/products', { params: { equipmentId } });
};

/** 获取导出文件数量（每28条一张Excel） */
export const getHandoverExportFileCount = (equipmentId: number, recordMonth: string) => {
  return request.get<number>('/handover/export-count', { params: { equipmentId, recordMonth } });
};

/** 导出单张 Excel（page 从 1 开始） */
export const exportHandoverExcelPage = (
  equipmentId: number,
  recordMonth: string,
  page: number
) => {
  return request.get(`/handover/export-page`, {
    params: { equipmentId, recordMonth, page },
    responseType: 'blob',
  });
};

/** 导出交接班记录（≤28条单文件，>28条分多文件下载） */
export const exportHandoverExcel = (equipmentId: number, recordMonth: string) => {
  return request.get(`/handover/export`, {
    params: { equipmentId, recordMonth },
    responseType: 'blob',
  });
};

/** 获取交接班记录表 HTML 预览（与下载 Excel 效果一致） */
export const getHandoverPreviewHtml = (equipmentId: number, recordMonth: string) => {
  return request.get<string>('/handover/preview', { params: { equipmentId, recordMonth } });
};

/** 获取交接班记录照片（新增时提交的拍照） */
export const getHandoverRecordPhoto = (id: number) => {
  return request.get(`/handover/${id}/photo`, { responseType: 'blob' });
};
