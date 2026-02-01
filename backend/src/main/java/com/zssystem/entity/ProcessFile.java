package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工艺文件主表实体类
 */
@Data
@TableName("process_file")
public class ProcessFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String fileNo;
    private Long equipmentId;
    private String equipmentNo;
    private String machineNo;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private Integer version;
    private Integer status;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime submitTime;
    private LocalDateTime approvalTime;
    private LocalDateTime effectiveTime;
    private LocalDateTime invalidTime;
    private String sealImagePath;
    private Integer isCurrent;
    /** 启用状态：1-启用，0-搁置，同机台号只能有一个启用 */
    private Integer enabled;
    private Long parentFileId;
    private String changeReason;
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
