package com.zssystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductionPlanSaveDTO {
    private Long id;

    private String planNo; // 新增时自动生成，编辑时不可修改

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    private Long equipmentId;

    private Long moldId;

    private Long operatorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime planStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime planEndTime;

    @NotNull(message = "计划数量不能为空")
    private Integer planQuantity;

    @NotNull(message = "状态不能为空")
    private Integer status; // 0-待执行，1-执行中，2-已完成

    private String remark;
}
