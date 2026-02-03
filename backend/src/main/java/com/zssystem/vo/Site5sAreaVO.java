package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Site5sAreaVO {
    private Long id;
    private String areaCode;
    private String areaName;
    private String dutyName;
    private Integer sortOrder;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Site5sAreaScheduleVO> schedules;
}
