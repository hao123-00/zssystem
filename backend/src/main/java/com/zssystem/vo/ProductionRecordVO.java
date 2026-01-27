package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductionRecordVO {
    private Long id;
    private String recordNo;
    private Long orderId;
    private String orderNo;
    private String productName;
    private Long planId;
    private String planNo;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentNo;
    
    // 设备详细信息
    private String groupName; // 组别
    private String machineNo; // 机台号
    private String equipmentModel; // 设备型号
    private String robotModel; // 机械手型号
    private LocalDate enableDate; // 启用日期
    private String serviceLife; // 使用年限（格式：X年X个月）
    private String moldTempMachine; // 模温机
    private String chiller; // 冻水机
    private String basicMold; // 基本排模
    private String spareMold1; // 备用排模1
    private String spareMold2; // 备用排模2
    private String spareMold3; // 备用排模3
    
    // 产品信息（从订单产品表获取）
    private List<ProductInfo> products; // 产品列表
    
    // 排程情况
    private List<ScheduleInfo> schedules; // 排程列表
    
    private Long moldId;
    private String moldName;
    private Long operatorId;
    private String operatorName;
    private LocalDate productionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quantity;
    private Integer defectQuantity;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @Data
    public static class ProductInfo {
        private String productName; // 产品名称
        private String productCode; // 产品编码
        private Integer orderQuantity; // 订单数量
        private Integer dailyCapacity; // 产能
        private Integer remainingQuantity; // 剩余数量（从排程中获取）
    }
    
    @Data
    public static class ScheduleInfo {
        private LocalDate scheduleDate; // 排程日期
        private Integer dayNumber; // 第几天
        private String productName; // 产品名称
        private Integer productionQuantity; // 排产数量
        private Integer dailyCapacity; // 产能
        private Integer remainingQuantity; // 剩余数量
    }
}
