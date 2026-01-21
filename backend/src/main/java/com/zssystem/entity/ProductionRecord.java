package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("production_record")
public class ProductionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("record_no")
    private String recordNo;

    @TableField("order_id")
    private Long orderId;

    @TableField("plan_id")
    private Long planId;

    @TableField("equipment_id")
    private Long equipmentId;

    @TableField("mold_id")
    private Long moldId;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("production_date")
    private LocalDate productionDate;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    private Integer quantity;

    @TableField("defect_quantity")
    private Integer defectQuantity;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
