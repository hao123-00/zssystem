package com.zssystem.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductionScheduleQueryDTO {
    private String machineNo; // 机台号
    private LocalDate startDate; // 排程开始日期（默认从今天开始）
}
