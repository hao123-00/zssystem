package com.zssystem.vo;

import lombok.Data;

@Data
public class EquipmentProductVO {
    private Long id;
    private Long equipmentId;
    private String equipmentNo;
    private String equipmentName;
    private String productCode;
    private String productName;
}
