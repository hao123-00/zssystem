package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("site_5s_area_schedule")
public class Site5sAreaSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("area_id")
    private Long areaId;

    @TableField("slot_index")
    private Integer slotIndex;

    @TableField("scheduled_time")
    private LocalTime scheduledTime;

    @TableField("tolerance_minutes")
    private Integer toleranceMinutes;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
