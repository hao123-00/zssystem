package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("production_order")
public class ProductionOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("customer_name")
    private String customerName;

    @TableField("product_name")
    private String productName;

    @TableField("product_code")
    private String productCode;

    private Integer quantity;

    @TableField("completed_quantity")
    private Integer completedQuantity;

    @TableField("delivery_date")
    private LocalDate deliveryDate;

    private Integer status; // 0-待生产，1-生产中，2-已完成，3-已取消

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
