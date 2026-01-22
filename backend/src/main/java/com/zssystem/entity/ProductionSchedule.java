package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("production_schedule")
public class ProductionSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("machine_no")
    private String machineNo; // 机台号
    
    @TableField("equipment_id")
    private Long equipmentId; // 设备ID（关联equipment表）
    
    @TableField("equipment_no")
    private String equipmentNo; // 设备编号
    
    @TableField("schedule_date")
    private LocalDate scheduleDate; // 排程日期（避开星期天）
    
    @TableField("day_number")
    private Integer dayNumber; // 第几天（从开始日期起，排除星期天后的天数）
    
    @TableField("product_code")
    private String productCode; // 产品编码
    
    @TableField("product_name")
    private String productName; // 产品名称
    
    @TableField("production_quantity")
    private Integer productionQuantity; // 排产数量（等于产能）
    
    @TableField("daily_capacity")
    private Integer dailyCapacity; // 当天产能
    
    @TableField("remaining_quantity")
    private Integer remainingQuantity; // 剩余数量（订单数量 - 产能 × 已生产天数）
    
    @TableField("order_id")
    private Long orderId; // 关联的生产订单ID
    
    @TableField("is_sunday")
    private Integer isSunday; // 是否为星期天：0-否，1-是（用于标识跳过的日期）
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
