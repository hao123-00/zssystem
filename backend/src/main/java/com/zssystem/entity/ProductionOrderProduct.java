package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("production_order_product")
public class ProductionOrderProduct {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId; // 订单ID（关联production_order表）

    @TableField("order_no")
    private String orderNo; // 订单编号

    @TableField("product_name")
    private String productName; // 产品名称

    @TableField("product_code")
    private String productCode; // 产品编码

    @TableField("order_quantity")
    private Integer orderQuantity; // 订单数量

    @TableField("daily_capacity")
    private Integer dailyCapacity; // 日产能（每天能生产的数量）

    @TableField("sort_order")
    private Integer sortOrder; // 排序（1-第一产品，2-第二产品，3-第三产品）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
