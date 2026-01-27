package com.zssystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Site5sCheckSaveDTO {
    private Long id;
    
    @NotNull(message = "检查日期不能为空")
    private LocalDate checkDate;
    
    @NotNull(message = "检查区域不能为空")
    private String checkArea;
    
    private String checkerName;
    
    // 5S各项评分（每项满分20分）
    @Min(value = 0, message = "整理得分必须在0-20之间")
    @Max(value = 20, message = "整理得分必须在0-20之间")
    private Integer sortScore; // 整理得分
    
    @Min(value = 0, message = "整顿得分必须在0-20之间")
    @Max(value = 20, message = "整顿得分必须在0-20之间")
    private Integer setScore; // 整顿得分
    
    @Min(value = 0, message = "清扫得分必须在0-20之间")
    @Max(value = 20, message = "清扫得分必须在0-20之间")
    private Integer shineScore; // 清扫得分
    
    @Min(value = 0, message = "清洁得分必须在0-20之间")
    @Max(value = 20, message = "清洁得分必须在0-20之间")
    private Integer standardizeScore; // 清洁得分
    
    @Min(value = 0, message = "素养得分必须在0-20之间")
    @Max(value = 20, message = "素养得分必须在0-20之间")
    private Integer sustainScore; // 素养得分
    
    private String problemDescription;
    private String remark;
}
