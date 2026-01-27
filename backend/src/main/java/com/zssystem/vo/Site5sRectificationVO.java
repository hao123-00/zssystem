package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Site5sRectificationVO {
    private Long id;
    private String taskNo;
    private Long checkId;
    private String checkNo; // 关联的检查单号
    private String problemDescription;
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
    private String statusText; // 状态文本
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
