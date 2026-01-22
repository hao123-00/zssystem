package com.zssystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentFaultQueryDTO {
    private Long equipmentId;
    private Integer status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
