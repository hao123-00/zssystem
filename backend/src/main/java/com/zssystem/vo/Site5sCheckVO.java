package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Site5sCheckVO {
    private Long id;
    private String checkNo;
    private LocalDate checkDate;
    private String checkArea;
    private String checkerName;
    
    // 5S各项评分
    private Integer sortScore; // 整理得分
    private Integer setScore; // 整顿得分
    private Integer shineScore; // 清扫得分
    private Integer standardizeScore; // 清洁得分
    private Integer sustainScore; // 素养得分
    private Integer totalScore; // 总分
    
    private String problemDescription;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
