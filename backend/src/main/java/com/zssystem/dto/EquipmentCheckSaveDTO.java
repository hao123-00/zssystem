package com.zssystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EquipmentCheckSaveDTO {
    private Long id;
    
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;
    
    @NotNull(message = "检查日期不能为空")
    private LocalDate checkDate;
    
    @NotNull(message = "检点人姓名不能为空")
    private String checkerName;
    
    // 电路部分
    private Integer circuitItem1;
    private Integer circuitItem2;
    private Integer circuitItem3;
    
    // 机架部分
    private Integer frameItem1;
    private Integer frameItem2;
    private Integer frameItem3;
    
    // 油路部分
    private Integer oilItem1;
    private Integer oilItem2;
    private Integer oilItem3;
    private Integer oilItem4;
    private Integer oilItem5;
    
    // 周边设备
    private Integer peripheralItem1;
    private Integer peripheralItem2;
    private Integer peripheralItem3;
    private Integer peripheralItem4;
    private Integer peripheralItem5;
    
    private String remark;
}
