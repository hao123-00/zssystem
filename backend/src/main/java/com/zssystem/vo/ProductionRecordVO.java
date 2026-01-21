package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProductionRecordVO {
    private Long id;
    private String recordNo;
    private Long orderId;
    private String orderNo;
    private String productName;
    private Long planId;
    private String planNo;
    private Long equipmentId;
    private String equipmentName;
    private Long moldId;
    private String moldName;
    private Long operatorId;
    private String operatorName;
    private LocalDate productionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quantity;
    private Integer defectQuantity;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
