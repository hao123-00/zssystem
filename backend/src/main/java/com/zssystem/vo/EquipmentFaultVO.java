package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentFaultVO {
    private Long id;
    private Long equipmentId;
    private String equipmentNo;
    private String equipmentName;
    private LocalDateTime faultDate;
    private String faultDescription;
    private String handleMethod;
    private String handlerName;
    private LocalDateTime handleDate;
    private Integer status;
    private String statusText; // 状态文本：待处理、处理中、已处理
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
