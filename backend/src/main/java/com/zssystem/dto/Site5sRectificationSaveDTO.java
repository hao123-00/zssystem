package com.zssystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Site5sRectificationSaveDTO {
    private Long id;
    
    private Long checkId; // 检查记录ID（可选，可以从检查记录生成）
    
    @NotBlank(message = "问题描述不能为空")
    private String problemDescription;
    
    @NotBlank(message = "区域不能为空")
    private String area;
    
    private String department;
    private String responsiblePerson;
    
    private LocalDate deadline;
    
    private String rectificationContent;
    private LocalDate rectificationDate;
    
    private String verifierName;
    private LocalDate verificationDate;
    private String verificationResult;
    
    private Integer status; // 0-待整改，1-整改中，2-待验证，3-已完成
    
    private String remark;
}
