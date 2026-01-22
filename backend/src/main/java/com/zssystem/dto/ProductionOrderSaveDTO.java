package com.zssystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProductionOrderSaveDTO {
    private Long id;

    private String orderNo; // 新增时自动生成，编辑时不可修改

    @NotBlank(message = "机台号不能为空")
    private String machineNo; // 机台号

    @Valid
    @NotNull(message = "产品列表不能为空")
    @Size(min = 1, max = 3, message = "产品数量必须在1-3个之间")
    private List<ProductInfo> products; // 产品列表（最多3个）

    private String remark;

    @Data
    public static class ProductInfo {
        @NotBlank(message = "产品名称不能为空")
        private String productName; // 产品名称

        private String productCode; // 产品编码

        @NotNull(message = "订单数量不能为空")
        private Integer orderQuantity; // 订单数量

        @NotNull(message = "产能不能为空")
        private Integer dailyCapacity; // 日产能（每天能生产的数量）
    }
}
