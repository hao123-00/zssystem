package com.zssystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EquipmentSaveDTO {
    private Long id;
    
    @NotNull(message = "设备编号不能为空")
    private String equipmentNo;
    
    @NotNull(message = "设备名称不能为空")
    private String equipmentName;
    
    private String groupName;
    
    private String machineNo;
    
    private String equipmentModel;
    
    private String manufacturer;
    
    private LocalDate purchaseDate;
    
    private String robotModel;
    
    private LocalDate enableDate;
    
    private String serviceLife;
    
    private String moldTempMachine;
    
    private String chiller;
    
    private String basicMold;
    
    private String spareMold1;
    
    private String spareMold2;
    
    private String spareMold3;
    
    private String basicMold4;
    
    private Integer status; // 0-停用，1-正常，2-维修中
    
    private String remark;
}
