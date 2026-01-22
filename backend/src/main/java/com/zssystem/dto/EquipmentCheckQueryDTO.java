package com.zssystem.dto;

import lombok.Data;

@Data
public class EquipmentCheckQueryDTO {
    private String equipmentNo;
    private String equipmentName;
    private String checkMonth; // YYYY-MM
    private String checkerName;
    private Integer pageNum = 1;
    private Integer pageSize = 30; // 默认30条（一个月）
}
