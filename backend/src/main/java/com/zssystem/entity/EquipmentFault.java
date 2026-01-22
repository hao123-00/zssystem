package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("equipment_fault")
public class EquipmentFault {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("equipment_id")
    private Long equipmentId;
    
    @TableField("fault_date")
    private LocalDateTime faultDate;
    
    @TableField("fault_description")
    private String faultDescription;
    
    @TableField("handle_method")
    private String handleMethod;
    
    @TableField("handler_name")
    private String handlerName;
    
    @TableField("handle_date")
    private LocalDateTime handleDate;
    
    private Integer status; // 0-待处理，1-处理中，2-已处理
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
