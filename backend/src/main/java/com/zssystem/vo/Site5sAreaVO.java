package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class Site5sAreaVO {
    private Long id;
    private String areaCode;
    private String areaName;
    private String checkItem;
    private Long responsibleUserId;
    private String responsibleUserName;
    private Long responsibleUserId2;
    private String responsibleUserName2;
    private LocalTime morningPhotoTime;
    private LocalTime eveningPhotoTime;
    private Integer sortOrder;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
