package com.zssystem.dto;

import lombok.Data;

@Data
public class ProductionPlanQueryDTO {
    private String planNo;
    private Long orderId;
    private Long equipmentId;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
