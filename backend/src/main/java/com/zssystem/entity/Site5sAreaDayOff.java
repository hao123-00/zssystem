package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("site_5s_area_day_off")
public class Site5sAreaDayOff {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("area_id")
    private Long areaId;

    @TableField("off_date")
    private LocalDate offDate;

    private LocalDateTime createTime;
}
