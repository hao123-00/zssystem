package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("site_area")
public class SiteArea {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("area_code")
    private String areaCode;
    
    @TableField("area_name")
    private String areaName;
    
    @TableField("department")
    private String department;
    
    @TableField("status")
    private Integer status; // 0-停用，1-启用
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
