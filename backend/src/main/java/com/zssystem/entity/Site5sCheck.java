package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("site_5s_check")
public class Site5sCheck {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("check_no")
    private String checkNo;
    
    @TableField("check_date")
    private LocalDate checkDate;
    
    @TableField("check_area")
    private String checkArea;
    
    @TableField("checker_name")
    private String checkerName;
    
    // 5S各项评分（每项满分20分）
    @TableField("sort_score")
    private Integer sortScore; // 整理得分
    
    @TableField("set_score")
    private Integer setScore; // 整顿得分
    
    @TableField("shine_score")
    private Integer shineScore; // 清扫得分
    
    @TableField("standardize_score")
    private Integer standardizeScore; // 清洁得分
    
    @TableField("sustain_score")
    private Integer sustainScore; // 素养得分
    
    @TableField("total_score")
    private Integer totalScore; // 总分
    
    @TableField("problem_description")
    private String problemDescription;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
