package com.zssystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EquipmentMaintenanceVO {
    private Long id;
    private Long equipmentId;
    private String equipmentNo;
    private String equipmentName;
    private LocalDate maintenanceDate;
    private String maintenanceType;
    private String maintenanceContent;
    private String maintainerName;
    private BigDecimal cost;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
