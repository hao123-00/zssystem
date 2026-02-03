package com.zssystem.vo;

import lombok.Data;

import java.time.LocalTime;

@Data
public class AreaTaskSlotVO {
    private Integer slotIndex;
    private LocalTime scheduledTime;
    private Integer toleranceMinutes;
    private Boolean completed;      // 是否已完成
    private Boolean onTime;         // 若已完成，是否按时
    private Long photoId;           // 拍照记录ID，已完成时有值
    private String uploaderName;
    private String uploadTimeStr;
}
