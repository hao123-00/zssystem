package com.zssystem.vo;

import lombok.Data;

@Data
public class EquipmentProductionProductVO {
    private Long id;
    private Long equipmentId;
    private String equipmentNo;
    private String equipmentName;
    private String productCode;
    private String productName;
    private Integer orderQuantity;
    private Integer dailyCapacity;
    private Integer sortOrder;
    private String remark;
    private Integer estimatedDays; // 预计完成天数 = ceil(订单数量 / 日产能)
}
