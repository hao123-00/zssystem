package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工艺文件电子受控章表实体类
 */
@Data
@TableName("process_file_seal")
public class ProcessFileSeal {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long fileId;
    private String fileNo;
    private String sealNo;
    private String sealType;
    private String sealContent;
    private String sealImagePath;
    private LocalDateTime sealTime;
    private Long sealById;
    private String sealByName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
