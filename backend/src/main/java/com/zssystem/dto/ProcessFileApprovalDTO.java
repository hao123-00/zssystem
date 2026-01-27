package com.zssystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工艺文件审批DTO
 */
@Data
public class ProcessFileApprovalDTO {
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
    
    @NotNull(message = "审批结果不能为空")
    private Integer approvalResult; // 1-通过，0-驳回
    
    private String approvalOpinion; // 驳回时必填
}
