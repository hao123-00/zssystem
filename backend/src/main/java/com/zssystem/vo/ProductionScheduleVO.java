package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProductionScheduleVO {
    private String machineNo; // 机台号
    private Long equipmentId; // 设备ID（可选）
    private String equipmentNo; // 设备编号（可选）
    private String equipmentName; // 设备名称（可选）
    private String groupName; // 组别（可选）
    private LocalDate scheduleStartDate; // 排程开始日期
    private List<ScheduleDayVO> scheduleDays; // 排程详情（排除星期天）
    private Boolean canCompleteTarget; // 是否能在指定时间内完成生产目标
}
