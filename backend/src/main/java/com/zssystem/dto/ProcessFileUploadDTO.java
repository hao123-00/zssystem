package com.zssystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 工艺文件上传DTO
 */
@Data
public class ProcessFileUploadDTO {
    private Long id; // 修改时传入
    
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;
    
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
    
    private String changeReason; // 修改时必填
    
    private String remark;
}
