package com.zssystem.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EquipmentMaintenanceQueryDTO {
    private Long equipmentId;
    private String maintenanceType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
