package com.zssystem.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductionRecordQueryDTO {
    private String recordNo;
    private Long orderId;
    private Long planId;
    private Long equipmentId;
    private LocalDate productionDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
