package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("site_5s_rectification")
public class Site5sRectification {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("task_no")
    private String taskNo;
    
    @TableField("check_id")
    private Long checkId;
    
    @TableField("problem_description")
    private String problemDescription;
    
    @TableField("area")
    private String area;
    
    @TableField("department")
    private String department;
    
    @TableField("responsible_person")
    private String responsiblePerson;
    
    @TableField("deadline")
    private LocalDate deadline;
    
    @TableField("rectification_content")
    private String rectificationContent;
    
    @TableField("rectification_date")
    private LocalDate rectificationDate;
    
    @TableField("verifier_name")
    private String verifierName;
    
    @TableField("verification_date")
    private LocalDate verificationDate;
    
    @TableField("verification_result")
    private String verificationResult;
    
    @TableField("status")
    private Integer status; // 0-待整改，1-整改中，2-待验证，3-已完成
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
