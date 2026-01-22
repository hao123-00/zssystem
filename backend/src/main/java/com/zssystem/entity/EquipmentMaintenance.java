package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("equipment_maintenance")
public class EquipmentMaintenance {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("equipment_id")
    private Long equipmentId;
    
    @TableField("maintenance_date")
    private LocalDate maintenanceDate;
    
    @TableField("maintenance_type")
    private String maintenanceType;
    
    @TableField("maintenance_content")
    private String maintenanceContent;
    
    @TableField("maintainer_name")
    private String maintainerName;
    
    private BigDecimal cost;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
