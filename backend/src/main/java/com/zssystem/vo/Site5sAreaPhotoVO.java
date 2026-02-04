package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Site5sAreaPhotoVO {
    private Long id;
    private Long areaId;
    private String areaName;
    private String checkItem;
    private LocalDate photoDate;
    private Integer slotIndex;
    private String photoPath;
    private Long uploaderId;
    private String uploaderName;
    private LocalDateTime uploadTime;
    private Integer isOnTime;
    private String remark;
}
