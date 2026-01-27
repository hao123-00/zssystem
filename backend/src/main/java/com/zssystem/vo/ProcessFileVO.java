package com.zssystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工艺文件VO
 */
@Data
public class ProcessFileVO {
    private Long id;
    private String fileNo;
    private Long equipmentId;
    private String equipmentNo;
    private String machineNo;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileSizeText; // 格式化后的文件大小
    private String fileType;
    private Integer version;
    private String versionText; // 格式化后的版本号，如 V1.0
    private Integer status;
    private String statusText; // 状态文本
    private Long creatorId;
    private String creatorName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime invalidTime;
    
    private String sealImagePath;
    private Integer isCurrent;
    private Long parentFileId;
    private String changeReason;
    private String remark;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    // 关联数据
    private List<ProcessFileApprovalVO> approvalHistory; // 审批历史
    private ProcessFileSealVO sealInfo; // 受控章信息
    
    private Integer currentApprovalLevel; // 当前审批级别
    private String nextApproverRole; // 下一个审批人角色
}
