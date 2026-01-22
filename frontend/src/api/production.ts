import request from '@/utils/request';

// ========== 生产订单 ==========
export interface ProductInfo {
  productName: string;
  productCode?: string;
  orderQuantity: number;
  dailyCapacity: number;
  sortOrder?: number;
}

export interface ProductionOrderInfo {
  id: number;
  orderNo: string;
  machineNo: string; // 机台号
  equipmentId?: number;
  equipmentNo?: string;
  products?: ProductInfo[]; // 产品列表（最多3个）
  status: number; // 0-待排程，1-排程中，2-已完成
  statusText?: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface ProductionOrderQueryParams {
  orderNo?: string;
  machineNo?: string; // 机台号
  productName?: string;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface ProductionOrderSaveParams {
  id?: number;
  orderNo?: string;
  machineNo: string; // 机台号
  products: ProductInfo[]; // 产品列表（1-3个）
  remark?: string;
}

// ========== 生产计划排程 ==========
export interface ProductionScheduleInfo {
  machineNo: string; // 机台号
  equipmentId?: number;
  equipmentNo?: string;
  equipmentName?: string;
  groupName?: string;
  scheduleStartDate: string; // 排程开始日期
  scheduleDays: ScheduleDayInfo[]; // 排程详情（排除星期天）
  canCompleteTarget: boolean; // 是否能在指定时间内完成生产目标
}

export interface ScheduleDayInfo {
  dayNumber: number; // 第几天（排除星期天后的天数）
  scheduleDate: string; // 排程日期
  productName: string; // 产品名称
  productionQuantity: number; // 排产数量（等于产能）
  dailyCapacity: number; // 产能
  remainingQuantity: number; // 剩余数量
}

export interface ProductionScheduleQueryParams {
  machineNo?: string; // 机台号
  startDate?: string; // 排程开始日期
}

// ========== 生产记录 ==========
export interface ProductionRecordProductInfo {
  productName: string;
  productCode?: string;
  orderQuantity: number;
  dailyCapacity: number;
  remainingQuantity?: number;
}

export interface ProductionRecordScheduleInfo {
  scheduleDate: string;
  dayNumber: number;
  productName: string;
  productionQuantity: number;
  dailyCapacity: number;
  remainingQuantity: number;
}

export interface ProductionRecordInfo {
  id: number;
  recordNo: string;
  orderId?: number;
  orderNo?: string;
  equipmentId?: number;
  equipmentNo?: string;
  equipmentName?: string;
  
  // 设备详细信息
  groupName?: string;
  machineNo?: string;
  equipmentModel?: string;
  robotModel?: string;
  enableDate?: string;
  serviceLife?: number;
  moldTempMachine?: string;
  chiller?: string;
  basicMold?: string;
  spareMold1?: string;
  spareMold2?: string;
  spareMold3?: string;
  
  // 产品信息
  products?: ProductionRecordProductInfo[];
  productName?: string;
  
  // 排程情况
  schedules?: ProductionRecordScheduleInfo[];
  
  scheduleId?: number;
  productCode?: string;
  planId?: number;
  planNo?: string;
  productionDate: string;
  startTime?: string;
  endTime?: string;
  quantity: number;
  defectQuantity?: number;
  operatorId?: number;
  operatorName?: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface ProductionRecordQueryParams {
  equipmentNo?: string;
  productName?: string;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface ProductionRecordSaveParams {
  id?: number;
  recordNo?: string;
  equipmentId: number;
  scheduleId?: number;
  productCode?: string;
  productName: string;
  productionDate: string;
  startTime?: string;
  endTime?: string;
  quantity: number;
  defectQuantity?: number;
  remark?: string;
}

// ========== 生产订单 API ==========
export const getOrderList = (params: ProductionOrderQueryParams) => {
  return request.get<{
    list: ProductionOrderInfo[];
    total: number;
  }>('/production/order/list', { params });
};

export const getOrderById = (id: number) => {
  return request.get<ProductionOrderInfo>(`/production/order/${id}`);
};

export const createOrder = (data: ProductionOrderSaveParams) => {
  return request.post<void>('/production/order', data);
};

export const updateOrder = (id: number, data: ProductionOrderSaveParams) => {
  return request.put<void>(`/production/order/${id}`, data);
};

export const deleteOrder = (id: number) => {
  return request.delete<void>(`/production/order/${id}`);
};

// ========== 生产计划排程 API ==========
export const generateSchedule = (machineNo: string, startDate?: string) => {
  const params: any = { machineNo };
  if (startDate) {
    params.startDate = startDate;
  }
  return request.post<ProductionScheduleInfo>('/production/schedule/generate', null, {
    params,
  });
};

export const getScheduleList = (params: ProductionScheduleQueryParams) => {
  return request.get<ProductionScheduleInfo[]>('/production/schedule/list', { params });
};

export const getScheduleByMachineNo = (machineNo: string, startDate?: string) => {
  return request.get<ProductionScheduleInfo>(`/production/schedule/machine/${machineNo}`, {
    params: { startDate },
  });
};

export const exportSchedule = (params: ProductionScheduleQueryParams) => {
  return request.get('/production/schedule/export', {
    params,
    responseType: 'blob',
  });
};

export const deleteScheduleByMachineNo = (machineNo: string) => {
  return request.delete<void>(`/production/schedule/machine/${machineNo}`);
};

export interface ProductionScheduleDetailInfo {
  id: number;
  machineNo: string;
  equipmentId?: number;
  equipmentNo?: string;
  equipmentName?: string;
  groupName?: string;
  scheduleDate: string;
  dayNumber: number;
  productCode?: string;
  productName: string;
  productionQuantity: number;
  dailyCapacity: number;
  remainingQuantity: number;
  orderId: number;
}

export const getScheduleDetailList = (params: ProductionScheduleQueryParams) => {
  return request.get<ProductionScheduleDetailInfo[]>('/production/schedule/detail/list', { params });
};

export const deleteScheduleById = (id: number) => {
  return request.delete<void>(`/production/schedule/${id}`);
};

// ========== 生产记录 API ==========
export const getRecordList = (params: ProductionRecordQueryParams) => {
  return request.get<{
    list: ProductionRecordInfo[];
    total: number;
  }>('/production/record/list', { params });
};

export const getRecordById = (id: number) => {
  return request.get<ProductionRecordInfo>(`/production/record/${id}`);
};

export const createRecord = (data: ProductionRecordSaveParams) => {
  return request.post<void>('/production/record', data);
};

export const updateRecord = (id: number, data: ProductionRecordSaveParams) => {
  return request.put<void>(`/production/record/${id}`, data);
};

export const deleteRecord = (id: number) => {
  return request.delete<void>(`/production/record/${id}`);
};

export const exportRecord = (params: ProductionRecordQueryParams) => {
  return request.get('/production/record/export', {
    params,
    responseType: 'blob',
  });
};
