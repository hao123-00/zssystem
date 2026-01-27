package com.zssystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工艺文件电子签名VO
 */
@Data
public class ProcessFileSignatureVO {
    private Long id;
    private Long fileId;
    private String fileNo;
    private String signatureType;
    private String signatureTypeText; // 签名类型文本：提交、审核、会签、批准
    private Long signerId;
    private String signerName;
    private String signerRole;
    private String signatureImagePath;
    private String signatureImageUrl; // 签名图片访问URL
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signatureTime;
    
    private String ipAddress;
    private String deviceInfo;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
