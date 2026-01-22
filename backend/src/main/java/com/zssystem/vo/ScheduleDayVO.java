package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleDayVO {
    private Integer dayNumber; // 第几天（排除星期天后的天数）
    private LocalDate scheduleDate; // 排程日期
    private String productName; // 产品名称
    private Integer productionQuantity; // 排产数量（等于产能）
    private Integer dailyCapacity; // 产能
    private Integer remainingQuantity; // 剩余数量
}
