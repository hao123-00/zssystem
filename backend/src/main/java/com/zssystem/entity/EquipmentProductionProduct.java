package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("equipment_production_product")
public class EquipmentProductionProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("equipment_id")
    private Long equipmentId;
    
    @TableField("equipment_no")
    private String equipmentNo;
    
    @TableField("product_code")
    private String productCode;
    
    @TableField("product_name")
    private String productName;
    
    @TableField("order_quantity")
    private Integer orderQuantity; // 订单数量
    
    @TableField("daily_capacity")
    private Integer dailyCapacity; // 日产能
    
    @TableField("sort_order")
    private Integer sortOrder; // 排序（生产优先级）
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
