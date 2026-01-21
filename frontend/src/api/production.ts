import request from '@/utils/request';

// ========== 生产订单 ==========
export interface ProductionOrderInfo {
  id: number;
  orderNo: string;
  customerName?: string;
  productName: string;
  productCode?: string;
  quantity: number;
  completedQuantity: number;
  deliveryDate?: string;
  status: number; // 0-待生产，1-生产中，2-已完成，3-已取消
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface ProductionOrderQueryParams {
  orderNo?: string;
  customerName?: string;
  productName?: string;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface ProductionOrderSaveParams {
  id?: number;
  orderNo?: string;
  customerName?: string;
  productName: string;
  productCode?: string;
  quantity: number;
  deliveryDate?: string;
  status: number;
  remark?: string;
}

// ========== 生产计划 ==========
export interface ProductionPlanInfo {
  id: number;
  planNo: string;
  orderId: number;
  orderNo?: string;
  productName?: string;
  equipmentId?: number;
  equipmentName?: string;
  moldId?: number;
  moldName?: string;
  operatorId?: number;
  operatorName?: string;
  planStartTime?: string;
  planEndTime?: string;
  planQuantity: number;
  completedQuantity?: number;
  status: number; // 0-待执行，1-执行中，2-已完成
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface ProductionPlanQueryParams {
  planNo?: string;
  orderId?: number;
  equipmentId?: number;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface ProductionPlanSaveParams {
  id?: number;
  planNo?: string;
  orderId: number;
  equipmentId?: number;
  moldId?: number;
  operatorId?: number;
  planStartTime?: string;
  planEndTime?: string;
  planQuantity: number;
  status: number;
  remark?: string;
}

// ========== 生产记录 ==========
export interface ProductionRecordInfo {
  id: number;
  recordNo: string;
  orderId: number;
  orderNo?: string;
  productName?: string;
  planId?: number;
  planNo?: string;
  equipmentId?: number;
  equipmentName?: string;
  moldId?: number;
  moldName?: string;
  operatorId?: number;
  operatorName?: string;
  productionDate: string;
  startTime?: string;
  endTime?: string;
  quantity: number;
  defectQuantity: number;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface ProductionRecordQueryParams {
  recordNo?: string;
  orderId?: number;
  planId?: number;
  equipmentId?: number;
  productionDate?: string;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface ProductionRecordSaveParams {
  id?: number;
  recordNo?: string;
  orderId: number;
  planId?: number;
  equipmentId?: number;
  moldId?: number;
  operatorId?: number;
  productionDate: string;
  startTime?: string;
  endTime?: string;
  quantity: number;
  defectQuantity?: number;
  remark?: string;
}

// ========== 订单 API ==========
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

// ========== 计划 API ==========
export const getPlanList = (params: ProductionPlanQueryParams) => {
  return request.get<{
    list: ProductionPlanInfo[];
    total: number;
  }>('/production/plan/list', { params });
};

export const getPlanById = (id: number) => {
  return request.get<ProductionPlanInfo>(`/production/plan/${id}`);
};

export const createPlan = (data: ProductionPlanSaveParams) => {
  return request.post<void>('/production/plan', data);
};

export const updatePlan = (id: number, data: ProductionPlanSaveParams) => {
  return request.put<void>(`/production/plan/${id}`, data);
};

export const deletePlan = (id: number) => {
  return request.delete<void>(`/production/plan/${id}`);
};

// ========== 记录 API ==========
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
