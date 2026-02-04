package com.zssystem.vo;

import lombok.Data;

import java.util.List;

@Data
public class AreaTaskVO {
    private Long areaId;
    private String areaCode;
    private String areaName;
    private String checkItem;
    private Long responsibleUserId;
    private String responsibleUserName;
    private Long responsibleUserId2;
    private String responsibleUserName2;
    private Integer totalSlots;
    private Integer completedSlots;
    private Integer status;         // 0-异常，1-正常，2-放假
    private Boolean dayOff;         // 当日是否放假（true 时状态显示放假）
    private List<AreaTaskSlotVO> slots;
}
