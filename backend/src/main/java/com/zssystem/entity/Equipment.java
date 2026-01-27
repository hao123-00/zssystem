package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("equipment")
public class Equipment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("equipment_no")
    private String equipmentNo;
    
    @TableField("equipment_name")
    private String equipmentName;
    
    @TableField("group_name")
    private String groupName;
    
    @TableField("machine_no")
    private String machineNo;
    
    @TableField("equipment_model")
    private String equipmentModel;
    
    @TableField("manufacturer")
    private String manufacturer;
    
    @TableField("purchase_date")
    private LocalDate purchaseDate;
    
    @TableField("robot_model")
    private String robotModel;
    
    @TableField("enable_date")
    private LocalDate enableDate;
    
    @TableField("service_life")
    private String serviceLife;
    
    @TableField("mold_temp_machine")
    private String moldTempMachine;
    
    @TableField("chiller")
    private String chiller;
    
    @TableField("basic_mold")
    private String basicMold;
    
    @TableField("spare_mold1")
    private String spareMold1;
    
    @TableField("spare_mold2")
    private String spareMold2;
    
    @TableField("spare_mold3")
    private String spareMold3;
    
    private Integer status; // 0-停用，1-正常，2-维修中
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
