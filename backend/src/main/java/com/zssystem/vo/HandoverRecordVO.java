package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HandoverRecordVO {
    private Long id;
    private Long equipmentId;
    private String equipmentNo;
    private LocalDateTime recordDate;
    private String shift;
    private String productName;
    private String material;
    private String equipmentCleaning;
    private String floorCleaning;
    private String leakage;
    private String itemPlacement;
    private String injectionMachine;
    private String robot;
    private String assemblyLine;
    private String mold;
    private String process;
    private String handoverLeader;
    private String receivingLeader;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 是否有照片（新增时上传的拍照） */
    private Boolean hasPhoto;
}
