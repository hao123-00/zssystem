package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工艺文件电子签名实体类
 */
@Data
@TableName("process_file_signature")
public class ProcessFileSignature {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("file_id")
    private Long fileId;
    
    @TableField("file_no")
    private String fileNo;
    
    @TableField("signature_type")
    private String signatureType; // SUBMIT-提交，APPROVE_LEVEL1-审核，APPROVE_LEVEL2-会签，APPROVE_LEVEL3-批准
    
    @TableField("signer_id")
    private Long signerId;
    
    @TableField("signer_name")
    private String signerName;
    
    @TableField("signer_role")
    private String signerRole;
    
    @TableField("signature_image_path")
    private String signatureImagePath;
    
    @TableField("signature_time")
    private LocalDateTime signatureTime;
    
    @TableField("ip_address")
    private String ipAddress;
    
    @TableField("device_info")
    private String deviceInfo;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
