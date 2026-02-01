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
    /** 启用状态：1-启用，0-搁置 */
    private Integer enabled;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
