package com.zssystem.dto;

import lombok.Data;

@Data
public class ProductionOrderQueryDTO {
    private String orderNo;
    private String machineNo; // 机台号
    private String productName; // 产品名称
    private Integer status; // 0-待排程，1-排程中，2-已完成
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
