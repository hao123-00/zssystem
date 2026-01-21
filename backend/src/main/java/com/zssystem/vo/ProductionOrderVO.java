package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProductionOrderVO {
    private Long id;
    private String orderNo;
    private String customerName;
    private String productName;
    private String productCode;
    private Integer quantity;
    private Integer completedQuantity;
    private LocalDate deliveryDate;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
