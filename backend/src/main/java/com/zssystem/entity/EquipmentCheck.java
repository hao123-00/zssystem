package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("equipment_check")
public class EquipmentCheck {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("equipment_id")
    private Long equipmentId;
    
    @TableField("equipment_no")
    private String equipmentNo;
    
    @TableField("equipment_name")
    private String equipmentName;
    
    @TableField("check_month")
    private String checkMonth; // YYYY-MM
    
    @TableField("check_date")
    private LocalDate checkDate;
    
    @TableField("checker_name")
    private String checkerName;
    
    // 电路部分（3项）
    @TableField("circuit_item1")
    private Integer circuitItem1;
    
    @TableField("circuit_item2")
    private Integer circuitItem2;
    
    @TableField("circuit_item3")
    private Integer circuitItem3;
    
    // 机架部分（3项）
    @TableField("frame_item1")
    private Integer frameItem1;
    
    @TableField("frame_item2")
    private Integer frameItem2;
    
    @TableField("frame_item3")
    private Integer frameItem3;
    
    // 油路部分（5项）
    @TableField("oil_item1")
    private Integer oilItem1;
    
    @TableField("oil_item2")
    private Integer oilItem2;
    
    @TableField("oil_item3")
    private Integer oilItem3;
    
    @TableField("oil_item4")
    private Integer oilItem4;
    
    @TableField("oil_item5")
    private Integer oilItem5;
    
    // 周边设备（5项）
    @TableField("peripheral_item1")
    private Integer peripheralItem1;
    
    @TableField("peripheral_item2")
    private Integer peripheralItem2;
    
    @TableField("peripheral_item3")
    private Integer peripheralItem3;
    
    @TableField("peripheral_item4")
    private Integer peripheralItem4;
    
    @TableField("peripheral_item5")
    private Integer peripheralItem5;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
