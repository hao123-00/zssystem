package com.zssystem.dto;

import lombok.Data;

/**
 * 工艺文件查询DTO
 */
@Data
public class ProcessFileQueryDTO {
    private String fileNo;
    private String equipmentNo;
    private String machineNo;
    private String fileName;
    private Integer status;
    private String creatorName;
    private Integer version;
    private Integer isCurrent;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
