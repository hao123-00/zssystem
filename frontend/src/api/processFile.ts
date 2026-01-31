import request from '@/utils/request';

/**
 * 工艺文件查询参数
 */
export interface ProcessFileQueryParams {
  fileNo?: string;
  equipmentNo?: string;
  machineNo?: string;
  fileName?: string;
  status?: number;
  creatorName?: string;
  version?: number;
  isCurrent?: number;
  pageNum?: number;
  pageSize?: number;
}

/**
 * 工艺文件上传参数
 */
export interface ProcessFileUploadParams {
  id?: number;
  equipmentId: number;
  file: File;
  changeReason?: string;
  remark?: string;
}

/**
 * 工艺文件审批参数
 */
export interface ProcessFileApprovalParams {
  fileId: number;
  approvalResult: number; // 1-通过，0-驳回
  approvalOpinion?: string;
}

/**
 * 工艺文件信息
 */
export interface ProcessFileInfo {
  id: number;
  fileNo: string;
  equipmentId: number;
  equipmentNo: string;
  machineNo: string;
  fileName: string;
  filePath: string;
  fileSize: number;
  fileSizeText: string;
  fileType: string;
  version: number;
  versionText: string;
  status: number;
  statusText: string;
  creatorId: number;
  creatorName: string;
  submitTime: string;
  approvalTime: string;
  effectiveTime: string;
  invalidTime: string;
  sealImagePath?: string;
  isCurrent: number;
  parentFileId?: number;
  changeReason?: string;
  remark?: string;
  createTime: string;
  updateTime: string;
  approvalHistory?: ProcessFileApprovalInfo[];
  sealInfo?: ProcessFileSealInfo;
  currentApprovalLevel?: number;
  nextApproverRole?: string;
}

/**
 * 工艺文件审批记录
 */
export interface ProcessFileApprovalInfo {
  id: number;
  fileId: number;
  fileNo: string;
  approvalLevel: number;
  approvalLevelText: string;
  approverId: number;
  approverName: string;
  approverRole: string;
  approvalResult: number;
  approvalResultText: string;
  approvalOpinion: string;
  approvalTime: string;
  signatureId?: number;
  signatureInfo?: ProcessFileSignatureInfo;
  createTime: string;
}

/**
 * 工艺文件电子受控章
 */
export interface ProcessFileSealInfo {
  id: number;
  fileId: number;
  fileNo: string;
  sealNo: string;
  sealType: string;
  sealContent: string;
  sealImagePath: string;
  sealTime: string;
  sealById: number;
  sealByName: string;
  createTime: string;
}

/**
 * 查询工艺文件列表
 */
export const getProcessFileList = (params: ProcessFileQueryParams) => {
  return request.get('/production/process-file/list', { params });
};

/**
 * 查询工艺文件详情
 */
export const getProcessFileById = (id: number) => {
  return request.get(`/production/process-file/${id}`);
};

/**
 * 上传工艺文件（Excel方式）
 */
export const uploadProcessFile = (params: ProcessFileUploadParams) => {
  const formData = new FormData();
  if (params.id) formData.append('id', params.id.toString());
  formData.append('equipmentId', params.equipmentId.toString());
  formData.append('file', params.file);
  if (params.changeReason) formData.append('changeReason', params.changeReason);
  if (params.remark) formData.append('remark', params.remark);
  
  return request.post('/production/process-file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/**
 * 保存工艺文件表单（支持图片上传）
 */
export const saveProcessFileForm = (params: any) => {
  // 判断是否为 FormData
  if (params instanceof FormData) {
    return request.post('/production/process-file/save-form', params, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  }
  return request.post('/production/process-file/save-form', params);
};

/**
 * 提交审批（需要电子签名）
 */
export const submitProcessFile = (id: number, signatureData: string) => {
  const formData = new FormData();
  
  // 将Base64转换为Blob
  const base64Data = signatureData.split(',')[1] || signatureData;
  const byteCharacters = atob(base64Data);
  const byteNumbers = new Array(byteCharacters.length);
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  const byteArray = new Uint8Array(byteNumbers);
  const blob = new Blob([byteArray], { type: 'image/png' });
  formData.append('signatureImage', blob, 'signature.png');
  formData.append('signatureType', 'SUBMIT');
  
  return request.post(`/production/process-file/${id}/submit`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/**
 * 审批工艺文件（需要电子签名）
 */
export const approveProcessFile = (params: ProcessFileApprovalParams & { signatureData?: string }) => {
  const formData = new FormData();
  formData.append('fileId', params.fileId.toString());
  formData.append('approvalResult', params.approvalResult.toString());
  if (params.approvalOpinion) {
    formData.append('approvalOpinion', params.approvalOpinion);
  }
  
  // 如果有签名数据，添加到FormData
  if (params.signatureData) {
    const base64Data = params.signatureData.split(',')[1] || params.signatureData;
    const byteCharacters = atob(base64Data);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: 'image/png' });
    formData.append('signatureImage', blob, 'signature.png');
  }
  
  return request.post('/production/process-file/approve', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/**
 * 作废工艺文件
 */
export const invalidateProcessFile = (id: number) => {
  return request.post(`/production/process-file/${id}/invalidate`);
};

/**
 * 下载工艺文件
 */
export const downloadProcessFile = (id: number) => {
  return request.get(`/production/process-file/${id}/download`, {
    responseType: 'blob',
  });
};

/**
 * 获取工艺文件 HTML 预览（与下载 Excel 效果一致）
 */
export const getProcessFilePreviewHtml = (id: number) => {
  return request.get<string>(`/production/process-file/${id}/preview`);
};

/**
 * 查询设备的工艺文件列表
 */
export const getProcessFilesByEquipment = (equipmentId: number, pageNum: number, pageSize: number) => {
  return request.get(`/production/process-file/equipment/${equipmentId}`, {
    params: { pageNum, pageSize },
  });
};

/**
 * 获取待审批的工艺文件列表
 */
export const getPendingApprovalFiles = (pageNum: number, pageSize: number) => {
  return request.get('/production/process-file/pending-approval', {
    params: { pageNum, pageSize },
  });
};

/**
 * 删除工艺文件（作废）
 */
export const deleteProcessFile = (id: number) => {
  return request.delete(`/production/process-file/${id}`);
};

/**
 * 按机台号（设备）批量删除工艺文件（作废该设备下所有工艺文件）
 * @returns 作废的数量
 */
export const batchDeleteProcessFileByEquipment = (equipmentId: number) => {
  return request.post<number>('/production/process-file/batch-delete-by-equipment', null, {
    params: { equipmentId },
  });
};

/**
 * 电子签名信息
 */
export interface ProcessFileSignatureInfo {
  id: number;
  fileId: number;
  fileNo: string;
  signatureType: string;
  signatureTypeText: string;
  signerId: number;
  signerName: string;
  signerRole: string;
  signatureImagePath: string;
  signatureImageUrl: string;
  signatureTime: string;
  ipAddress?: string;
  deviceInfo?: string;
  createTime: string;
}

/**
 * 上传电子签名
 */
export const uploadSignature = (params: {
  fileId: number;
  signatureType: string;
  signatureImage: string; // Base64图片数据
  ipAddress?: string;
  deviceInfo?: string;
}) => {
  const formData = new FormData();
  formData.append('fileId', params.fileId.toString());
  formData.append('signatureType', params.signatureType);
  
  // 将Base64转换为Blob
  const base64Data = params.signatureImage.split(',')[1] || params.signatureImage;
  const byteCharacters = atob(base64Data);
  const byteNumbers = new Array(byteCharacters.length);
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  const byteArray = new Uint8Array(byteNumbers);
  const blob = new Blob([byteArray], { type: 'image/png' });
  formData.append('signatureImage', blob, 'signature.png');
  
  if (params.ipAddress) {
    formData.append('ipAddress', params.ipAddress);
  }
  if (params.deviceInfo) {
    formData.append('deviceInfo', params.deviceInfo);
  }
  
  return request.post('/production/process-file/signature', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/**
 * 查询文件的电子签名列表
 */
export const getSignaturesByFileId = (fileId: number) => {
  return request.get<ProcessFileSignatureInfo[]>(`/production/process-file/${fileId}/signatures`);
};

/**
 * 查看签名图片
 */
export const getSignatureImage = (signatureId: number) => {
  return request.get(`/production/process-file/signature/image/${signatureId}`, {
    responseType: 'blob',
  });
};
