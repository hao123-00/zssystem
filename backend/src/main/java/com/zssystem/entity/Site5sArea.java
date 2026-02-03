package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("site_5s_area")
public class Site5sArea {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("area_code")
    private String areaCode;

    @TableField("area_name")
    private String areaName;

    @TableField("duty_name")
    private String dutyName;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
