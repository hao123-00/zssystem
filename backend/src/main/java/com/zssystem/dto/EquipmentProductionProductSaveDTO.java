package com.zssystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipmentProductionProductSaveDTO {
    private Long id;
    
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;
    
    private String productCode;
    
    @NotNull(message = "产品名称不能为空")
    private String productName;
    
    @NotNull(message = "订单数量不能为空")
    private Integer orderQuantity;
    
    @NotNull(message = "日产能不能为空")
    private Integer dailyCapacity;
    
    private Integer sortOrder = 0; // 默认排序为0
    
    private String remark;
}
