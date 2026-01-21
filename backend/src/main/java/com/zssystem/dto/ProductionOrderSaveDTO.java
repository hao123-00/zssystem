package com.zssystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductionOrderSaveDTO {
    private Long id;

    private String orderNo; // 新增时自动生成，编辑时不可修改

    private String customerName;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    private String productCode;

    @NotNull(message = "订单数量不能为空")
    private Integer quantity;

    private LocalDate deliveryDate;

    @NotNull(message = "状态不能为空")
    private Integer status; // 0-待生产，1-生产中，2-已完成，3-已取消

    private String remark;
}
