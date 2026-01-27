package com.zssystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工艺文件电子受控章VO
 */
@Data
public class ProcessFileSealVO {
    private Long id;
    private Long fileId;
    private String fileNo;
    private String sealNo;
    private String sealType;
    private String sealContent;
    private String sealImagePath;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sealTime;
    
    private Long sealById;
    private String sealByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
