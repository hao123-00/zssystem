package com.zssystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EquipmentMaintenanceSaveDTO {
    private Long id;
    
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;
    
    @NotNull(message = "维护日期不能为空")
    private LocalDate maintenanceDate;
    
    private String maintenanceType;
    
    private String maintenanceContent;
    
    private String maintainerName;
    
    private BigDecimal cost;
    
    private String remark;
}
