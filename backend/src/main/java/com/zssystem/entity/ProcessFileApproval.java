package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工艺文件审批记录表实体类
 */
@Data
@TableName("process_file_approval")
public class ProcessFileApproval {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long fileId;
    private String fileNo;
    private Integer approvalLevel;
    private Long approverId;
    private String approverName;
    private String approverRole;
    private Integer approvalResult;
    private String approvalOpinion;
    private LocalDateTime approvalTime;
    private Long signatureId; // 电子签名ID
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
