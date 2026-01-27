package com.zssystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工艺文件审批记录VO
 */
@Data
public class ProcessFileApprovalVO {
    private Long id;
    private Long fileId;
    private String fileNo;
    private Integer approvalLevel;
    private String approvalLevelText; // 审批级别文本
    private Long approverId;
    private String approverName;
    private String approverRole;
    private Integer approvalResult;
    private String approvalResultText; // 审批结果文本
    private String approvalOpinion;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;
    
    private Long signatureId; // 电子签名ID
    private ProcessFileSignatureVO signatureInfo; // 电子签名信息
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
