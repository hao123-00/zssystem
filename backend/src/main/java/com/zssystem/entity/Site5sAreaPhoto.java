package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("site_5s_area_photo")
public class Site5sAreaPhoto {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("area_id")
    private Long areaId;

    @TableField("photo_date")
    private LocalDate photoDate;

    @TableField("slot_index")
    private Integer slotIndex;

    @TableField("photo_path")
    private String photoPath;

    @TableField("uploader_id")
    private Long uploaderId;

    @TableField("uploader_name")
    private String uploaderName;

    @TableField("upload_time")
    private LocalDateTime uploadTime;

    @TableField("is_on_time")
    private Integer isOnTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
