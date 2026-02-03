package com.zssystem.vo;

import lombok.Data;

import java.time.LocalTime;

@Data
public class Site5sAreaScheduleVO {
    private Long id;
    private Long areaId;
    private Integer slotIndex;
    private LocalTime scheduledTime;
    private Integer toleranceMinutes;
    private String remark;
}
