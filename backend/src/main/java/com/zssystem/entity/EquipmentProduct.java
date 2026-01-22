package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("equipment_product")
public class EquipmentProduct {
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
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
