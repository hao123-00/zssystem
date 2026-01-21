package com.zssystem.dto;

import lombok.Data;

@Data
public class ProductionOrderQueryDTO {
    private String orderNo;
    private String customerName;
    private String productName;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
