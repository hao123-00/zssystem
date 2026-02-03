package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EquipmentVO {
    private Long id;
    private String equipmentNo;
    private String equipmentName;
    private String groupName;
    private String machineNo;
    private String equipmentModel;
    private String manufacturer;
    private LocalDate purchaseDate;
    private String robotModel;
    private LocalDate enableDate;
    private String serviceLife;
    private String moldTempMachine;
    private String chiller;
    private String basicMold;
    private String spareMold1;
    private String spareMold2;
    private String spareMold3;
    private String basicMold4;
    private Integer status;
    private String statusText; // 状态文本：停用、正常、维修中
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
