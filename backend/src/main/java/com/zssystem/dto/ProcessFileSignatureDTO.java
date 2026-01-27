package com.zssystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 工艺文件电子签名DTO
 */
@Data
public class ProcessFileSignatureDTO {
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
    
    @NotBlank(message = "签名类型不能为空")
    private String signatureType; // SUBMIT-提交，APPROVE_LEVEL1-审核，APPROVE_LEVEL2-会签，APPROVE_LEVEL3-批准
    
    @NotNull(message = "签名图片不能为空")
    private MultipartFile signatureImage; // 签名图片文件（Base64或图片文件）
    
    private String ipAddress; // 签名IP地址（可选）
    
    private String deviceInfo; // 设备信息（可选）
}
