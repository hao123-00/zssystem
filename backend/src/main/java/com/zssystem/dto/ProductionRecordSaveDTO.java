package com.zssystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProductionRecordSaveDTO {
    private Long id;

    private String recordNo; // 新增时自动生成，编辑时不可修改

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    private Long planId;

    private Long equipmentId;

    private Long moldId;

    private Long operatorId;

    @NotNull(message = "生产日期不能为空")
    private LocalDate productionDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NotNull(message = "产量不能为空")
    private Integer quantity;

    private Integer defectQuantity = 0;

    private String remark;
}
