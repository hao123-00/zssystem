package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductionOrderVO {
    private Long id;
    private String orderNo; // 订单编号
    private String machineNo; // 机台号
    private Long equipmentId; // 设备ID
    private String equipmentNo; // 设备编号
    private Integer status; // 0-待排程，1-排程中，2-已完成
    private String statusText; // 状态文本
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 产品列表
    private List<ProductInfo> products;
    
    @Data
    public static class ProductInfo {
        private String productName; // 产品名称
        private String productCode; // 产品编码
        private Integer orderQuantity; // 订单数量
        private Integer dailyCapacity; // 日产能
        private Integer sortOrder; // 排序（1-第一产品，2-第二产品，3-第三产品）
    }
}
