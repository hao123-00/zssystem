package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("production_plan")
public class ProductionPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("plan_no")
    private String planNo;

    @TableField("order_id")
    private Long orderId;

    @TableField("equipment_id")
    private Long equipmentId;

    @TableField("mold_id")
    private Long moldId;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("plan_start_time")
    private LocalDateTime planStartTime;

    @TableField("plan_end_time")
    private LocalDateTime planEndTime;

    @TableField("plan_quantity")
    private Integer planQuantity;

    private Integer status; // 0-待执行，1-执行中，2-已完成

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
