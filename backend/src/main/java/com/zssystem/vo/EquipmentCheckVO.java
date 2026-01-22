package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EquipmentCheckVO {
    private Long id;
    private Long equipmentId;
    private String equipmentNo;
    private String equipmentName;
    private String checkMonth;
    private LocalDate checkDate;
    private String checkerName;
    
    // 电路部分（3项）
    private Integer circuitItem1;
    private Integer circuitItem2;
    private Integer circuitItem3;
    
    // 机架部分（3项）
    private Integer frameItem1;
    private Integer frameItem2;
    private Integer frameItem3;
    
    // 油路部分（5项）
    private Integer oilItem1;
    private Integer oilItem2;
    private Integer oilItem3;
    private Integer oilItem4;
    private Integer oilItem5;
    
    // 周边设备（5项）
    private Integer peripheralItem1;
    private Integer peripheralItem2;
    private Integer peripheralItem3;
    private Integer peripheralItem4;
    private Integer peripheralItem5;
    
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
