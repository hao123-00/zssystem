package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 生产排程详情VO（单条记录）
 */
@Data
public class ProductionScheduleDetailVO {
    private Long id; // 排程记录ID
    private String machineNo; // 机台号
    private Long equipmentId; // 设备ID
    private String equipmentNo; // 设备编号
    private String equipmentName; // 设备名称
    private String groupName; // 组别
    private LocalDate scheduleDate; // 排程日期
    private Integer dayNumber; // 第几天（排除星期天后的天数）
    private String productCode; // 产品编码
    private String productName; // 产品名称
    private Integer productionQuantity; // 排产数量
    private Integer dailyCapacity; // 产能
    private Integer remainingQuantity; // 剩余数量
    private Long orderId; // 订单ID
}
