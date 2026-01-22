package com.zssystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipmentProductSaveDTO {
    private Long id;
    
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;
    
    @NotNull(message = "产品编码不能为空")
    private String productCode;
    
    @NotNull(message = "产品名称不能为空")
    private String productName;
}
