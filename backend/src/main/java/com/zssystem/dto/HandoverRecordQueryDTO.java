package com.zssystem.dto;

import lombok.Data;

@Data
public class HandoverRecordQueryDTO {
    private Long equipmentId;
    private String equipmentNo;
    private String recordMonth;  // YYYY-MM
    private String productName;
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}
