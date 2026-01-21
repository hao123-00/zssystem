package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductionPlanVO {
    private Long id;
    private String planNo;
    private Long orderId;
    private String orderNo;
    private String productName;
    private Long equipmentId;
    private String equipmentName;
    private Long moldId;
    private String moldName;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private Integer planQuantity;
    private Integer completedQuantity;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
