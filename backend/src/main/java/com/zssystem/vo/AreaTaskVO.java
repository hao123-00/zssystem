package com.zssystem.vo;

import lombok.Data;

import java.util.List;

@Data
public class AreaTaskVO {
    private Long areaId;
    private String areaCode;
    private String areaName;
    private String dutyName;
    private Integer totalSlots;
    private Integer completedSlots;
    private Integer status;         // 0-异常，1-正常（当日全部按时完成）
    private List<AreaTaskSlotVO> slots;
}
