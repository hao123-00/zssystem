package com.zssystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentFaultSaveDTO {
    private Long id;
    
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;
    
    @NotNull(message = "故障日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime faultDate;
    
    @NotNull(message = "故障描述不能为空")
    private String faultDescription;
    
    private String handleMethod;
    
    private String handlerName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime handleDate;
    
    private Integer status; // 0-待处理，1-处理中，2-已处理
    
    private String remark;
}
