package com.zssystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class Site5sAreaScheduleDTO {
    private Long id;

    @NotNull(message = "时段序号不能为空")
    @Min(value = 1, message = "时段序号从1开始")
    @Max(value = 10, message = "最多10个时段")
    private Integer slotIndex;

    @NotNull(message = "规定拍照时间不能为空")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime scheduledTime;

    @Min(value = 0, message = "容忍分钟数不能为负")
    @Max(value = 120, message = "容忍分钟数不超过120")
    private Integer toleranceMinutes = 30;

    private String remark;
}
