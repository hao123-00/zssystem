package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("site_5s_area")
public class Site5sArea {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("area_code")
    private String areaCode;

    @TableField("area_name")
    private String areaName;

    @TableField("check_item")
    private String checkItem;

    @TableField("responsible_user_id")
    private Long responsibleUserId;

    @TableField("responsible_user_id_2")
    private Long responsibleUserId2;

    @TableField("morning_photo_time")
    private LocalTime morningPhotoTime;

    @TableField("evening_photo_time")
    private LocalTime eveningPhotoTime;

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
