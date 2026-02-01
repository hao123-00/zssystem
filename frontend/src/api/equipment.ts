import request from '@/utils/request';

// ========== 设备基本信息 ==========
export interface EquipmentInfo {
  id: number;
  equipmentNo: string;
  equipmentName: string;
  groupName?: string;
  machineNo?: string;
  equipmentModel?: string;
  manufacturer?: string;
  purchaseDate?: string;
  robotModel?: string;
  enableDate?: string;
  serviceLife?: number;
  moldTempMachine?: string;
  chiller?: string;
  basicMold?: string;
  spareMold1?: string;
  spareMold2?: string;
  spareMold3?: string;
  status: number; // 0-停用，1-正常，2-维修中
  statusText?: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface EquipmentQueryParams {
  equipmentNo?: string;
  equipmentName?: string;
  groupName?: string;
  machineNo?: string;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface EquipmentSaveParams {
  id?: number;
  equipmentNo: string;
  equipmentName: string;
  groupName?: string;
  machineNo?: string;
  equipmentModel?: string;
  manufacturer?: string;
  purchaseDate?: string;
  robotModel?: string;
  enableDate?: string;
  serviceLife?: number;
  moldTempMachine?: string;
  chiller?: string;
  basicMold?: string;
  spareMold1?: string;
  spareMold2?: string;
  spareMold3?: string;
  status?: number;
  remark?: string;
}

// ========== 设备可生产产品 ==========
export interface EquipmentProductInfo {
  id: number;
  equipmentId: number;
  equipmentNo: string;
  equipmentName?: string;
  productCode: string;
  productName: string;
}

export interface EquipmentProductSaveParams {
  id?: number;
  equipmentId: number;
  productCode: string;
  productName: string;
}

// ========== 设备点检 ==========
export interface EquipmentCheckInfo {
  id: number;
  equipmentId: number;
  equipmentNo: string;
  equipmentName: string;
  checkMonth: string;
  checkDate: string;
  checkerName: string;
  circuitItem1?: number;
  circuitItem2?: number;
  circuitItem3?: number;
  frameItem1?: number;
  frameItem2?: number;
  frameItem3?: number;
  oilItem1?: number;
  oilItem2?: number;
  oilItem3?: number;
  oilItem4?: number;
  oilItem5?: number;
  peripheralItem1?: number;
  peripheralItem2?: number;
  peripheralItem3?: number;
  peripheralItem4?: number;
  peripheralItem5?: number;
  remark?: string;
}

export interface EquipmentCheckQueryParams {
  equipmentNo?: string;
  equipmentName?: string;
  checkMonth?: string;
  checkerName?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface EquipmentCheckSaveParams {
  id?: number;
  equipmentId: number;
  checkDate: string;
  checkerName: string;
  circuitItem1?: number;
  circuitItem2?: number;
  circuitItem3?: number;
  frameItem1?: number;
  frameItem2?: number;
  frameItem3?: number;
  oilItem1?: number;
  oilItem2?: number;
  oilItem3?: number;
  oilItem4?: number;
  oilItem5?: number;
  peripheralItem1?: number;
  peripheralItem2?: number;
  peripheralItem3?: number;
  peripheralItem4?: number;
  peripheralItem5?: number;
  remark?: string;
}

// ========== 设备维护记录 ==========
export interface EquipmentMaintenanceInfo {
  id: number;
  equipmentId: number;
  equipmentNo?: string;
  equipmentName?: string;
  maintenanceDate: string;
  maintenanceType?: string;
  maintenanceContent?: string;
  maintainerName?: string;
  cost?: number;
  remark?: string;
}

export interface EquipmentMaintenanceQueryParams {
  equipmentId?: number;
  maintenanceType?: string;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface EquipmentMaintenanceSaveParams {
  id?: number;
  equipmentId: number;
  maintenanceDate: string;
  maintenanceType?: string;
  maintenanceContent?: string;
  maintainerName?: string;
  cost?: number;
  remark?: string;
}

// ========== 设备故障记录 ==========
export interface EquipmentFaultInfo {
  id: number;
  equipmentId: number;
  equipmentNo?: string;
  equipmentName?: string;
  faultDate: string;
  faultDescription: string;
  handleMethod?: string;
  handlerName?: string;
  handleDate?: string;
  status: number; // 0-待处理，1-处理中，2-已处理
  statusText?: string;
  remark?: string;
}

export interface EquipmentFaultQueryParams {
  equipmentId?: number;
  status?: number;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface EquipmentFaultSaveParams {
  id?: number;
  equipmentId: number;
  faultDate: string;
  faultDescription: string;
  handleMethod?: string;
  handlerName?: string;
  handleDate?: string;
  status?: number;
  remark?: string;
}

// ========== 设备 API ==========
export const getEquipmentList = (params: EquipmentQueryParams) => {
  return request.get<{
    list: EquipmentInfo[];
    total: number;
  }>('/equipment/list', { params });
};

export const getEquipmentById = (id: number) => {
  return request.get<EquipmentInfo>(`/equipment/${id}`);
};

export const getEquipmentByNo = (equipmentNo: string) => {
  return request.get<EquipmentInfo>(`/equipment/no/${equipmentNo}`);
};

export const createEquipment = (data: EquipmentSaveParams) => {
  return request.post<void>('/equipment', data);
};

export const updateEquipment = (id: number, data: EquipmentSaveParams) => {
  return request.put<void>(`/equipment/${id}`, data);
};

export const deleteEquipment = (id: number) => {
  return request.delete<void>(`/equipment/${id}`);
};

// ========== 设备可生产产品 API ==========
export const getProductListByEquipmentId = (equipmentId: number) => {
  return request.get<EquipmentProductInfo[]>(`/equipment/product/equipment/${equipmentId}`);
};

export const getEquipmentListByProductCode = (productCode: string) => {
  return request.get<EquipmentProductInfo[]>(`/equipment/product/product/${productCode}`);
};

export const bindProduct = (data: EquipmentProductSaveParams) => {
  return request.post<void>('/equipment/product', data);
};

export const unbindProduct = (id: number) => {
  return request.delete<void>(`/equipment/product/${id}`);
};

// ========== 设备点检 API ==========
export const getCheckList = (params: EquipmentCheckQueryParams) => {
  return request.get<{
    list: EquipmentCheckInfo[];
    total: number;
  }>('/equipment/check/list', { params });
};

export const getCheckById = (id: number) => {
  return request.get<EquipmentCheckInfo>(`/equipment/check/${id}`);
};

export const saveCheck = (data: EquipmentCheckSaveParams) => {
  return request.post<void>('/equipment/check', data);
};

export const deleteCheck = (id: number) => {
  return request.delete<void>(`/equipment/check/${id}`);
};

/**
 * 导出某设备某月30天点检记录为 Excel
 */
export const exportCheckExcel = (equipmentId: number, checkMonth: string) => {
  return request.get(`/equipment/check/export`, {
    params: { equipmentId, checkMonth },
    responseType: 'blob',
  });
};

/**
 * 获取某设备某月30天点检表 HTML 预览（效果与下载 Excel 一致）
 */
export const getEquipmentCheckPreviewHtml = (equipmentId: number, checkMonth: string) => {
  return request.get<string>('/equipment/check/export/preview', {
    params: { equipmentId, checkMonth },
  });
};

// ========== 设备扫码查看（公开接口，无需登录） ==========
export interface EquipmentQrViewData {
  equipment: { id: number; equipmentNo: string; equipmentName: string; machineNo: string };
  checkRecords: EquipmentCheckInfo[];
  enabledProcessFile: { id: number; fileNo: string; fileName: string; versionText: string } | null;
  checkMonth: string;
}

export const getEquipmentQrViewData = (equipmentId: number) => {
  return request.get<EquipmentQrViewData>(`/qr/equipment/${equipmentId}/view`);
};

export const getEquipmentQrCheckPreviewHtml = (equipmentId: number) => {
  return request.get<string>(`/qr/equipment/${equipmentId}/check/preview`);
};

export const getEquipmentQrProcessFilePreviewHtml = (equipmentId: number) => {
  return request.get<string>(`/qr/equipment/${equipmentId}/process-file/preview`);
};

export const getEquipmentQrCodeImage = (equipmentId: number) => {
  return request.get(`/equipment/${equipmentId}/qrcode`, { responseType: 'blob' });
};

// ========== 设备维护 API ==========
export const getMaintenanceList = (params: EquipmentMaintenanceQueryParams) => {
  return request.get<{
    list: EquipmentMaintenanceInfo[];
    total: number;
  }>('/equipment/maintenance/list', { params });
};

export const getMaintenanceById = (id: number) => {
  return request.get<EquipmentMaintenanceInfo>(`/equipment/maintenance/${id}`);
};

export const createMaintenance = (data: EquipmentMaintenanceSaveParams) => {
  return request.post<void>('/equipment/maintenance', data);
};

export const updateMaintenance = (id: number, data: EquipmentMaintenanceSaveParams) => {
  return request.put<void>(`/equipment/maintenance/${id}`, data);
};

export const deleteMaintenance = (id: number) => {
  return request.delete<void>(`/equipment/maintenance/${id}`);
};

// ========== 设备故障 API ==========
export const getFaultList = (params: EquipmentFaultQueryParams) => {
  return request.get<{
    list: EquipmentFaultInfo[];
    total: number;
  }>('/equipment/fault/list', { params });
};

export const getFaultById = (id: number) => {
  return request.get<EquipmentFaultInfo>(`/equipment/fault/${id}`);
};

export const createFault = (data: EquipmentFaultSaveParams) => {
  return request.post<void>('/equipment/fault', data);
};

export const updateFault = (id: number, data: EquipmentFaultSaveParams) => {
  return request.put<void>(`/equipment/fault/${id}`, data);
};

export const deleteFault = (id: number) => {
  return request.delete<void>(`/equipment/fault/${id}`);
};
