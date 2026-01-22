package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("production_order")
public class ProductionOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo; // 订单编号（唯一）

    @TableField("machine_no")
    private String machineNo; // 机台号

    @TableField("equipment_id")
    private Long equipmentId; // 设备ID（关联equipment表）

    private Integer status; // 0-待排程，1-排程中，2-已完成

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
