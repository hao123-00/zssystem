package com.zssystem.dto;

import lombok.Data;

@Data
public class EquipmentQueryDTO {
    private String equipmentNo;
    private String equipmentName;
    private String groupName;
    private String machineNo;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
