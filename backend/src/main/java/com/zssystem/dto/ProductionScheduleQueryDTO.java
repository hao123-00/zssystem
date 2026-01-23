package com.zssystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProductionScheduleQueryDTO {
    private String machineNo; // 机台号
    private LocalDate startDate; // 排程开始日期
    private List<String> dateList; // 日期列表（用于Excel列标题，从前端传递）
}
