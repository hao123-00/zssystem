package com.zssystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 工艺文件表单提交DTO
 */
@Data
public class ProcessFileFormDTO {
    private Long id; // 修改时传入
    
    /** 工艺文件编号（新建时必填，手动输入） */
    private String fileNo;
    /** 文件名称（新建时必填，手动输入） */
    private String fileName;
    
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;
    
    private String changeReason; // 修改时必填
    private String remark;
    
    // 基本信息
    private String productModel;
    private String productName;
    private String moldManufacturingCompany;
    private String partName;
    private String projectLeader;
    
    // 材料信息
    private String materialName;
    private String materialGrade;
    private String materialColor;
    private String pigmentName;
    private BigDecimal pigmentRatio;
    private BigDecimal partNetWeight;
    private BigDecimal partGrossWeight;
    private BigDecimal consumptionQuota;
    
    // 模具信息
    private String moldNumber;
    private Integer cavityQuantity;
    private BigDecimal clampingForce;
    
    // 产品关键尺寸（旧字段，已废弃）
    private String productKeyDimensions;
    // 产品关键尺寸图片路径（新字段，由后端填充）
    private String productKeyDimensionImage1;
    private String productKeyDimensionImage2;
    
    // 注塑成型工艺参数 - 合模
    private BigDecimal clamp1Pressure;
    private BigDecimal clamp1Flow;
    private BigDecimal clamp1Position;
    private BigDecimal clamp2Pressure;
    private BigDecimal clamp2Flow;
    private BigDecimal clamp2Position;
    private BigDecimal moldProtectionPressure;
    private BigDecimal moldProtectionFlow;
    private BigDecimal moldProtectionPosition;
    private BigDecimal highPressurePressure;
    private BigDecimal highPressureFlow;
    private BigDecimal highPressurePosition;
    
    // 进芯
    private BigDecimal corePull1InPressure;
    private BigDecimal corePull1InFlow;
    private BigDecimal corePull1InPosition;
    private BigDecimal corePull2InPressure;
    private BigDecimal corePull2InFlow;
    private BigDecimal corePull2InPosition;
    
    // 射胶（6段）
    private BigDecimal injection1Pressure;
    private BigDecimal injection1Flow;
    private BigDecimal injection1Position;
    private BigDecimal injection2Pressure;
    private BigDecimal injection2Flow;
    private BigDecimal injection2Position;
    private BigDecimal injection3Pressure;
    private BigDecimal injection3Flow;
    private BigDecimal injection3Position;
    private BigDecimal injection4Pressure;
    private BigDecimal injection4Flow;
    private BigDecimal injection4Position;
    private BigDecimal injection5Pressure;
    private BigDecimal injection5Flow;
    private BigDecimal injection5Position;
    private BigDecimal injection6Pressure;
    private BigDecimal injection6Flow;
    private BigDecimal injection6Position;
    
    // 保压（3段）
    private BigDecimal holding1Pressure;
    private BigDecimal holding1Flow;
    private BigDecimal holding1Position;
    private BigDecimal holding2Pressure;
    private BigDecimal holding2Flow;
    private BigDecimal holding2Position;
    private BigDecimal holding3Pressure;
    private BigDecimal holding3Flow;
    private BigDecimal holding3Position;
    
    // 开模
    private BigDecimal openMold1Pressure;
    private BigDecimal openMold1Flow;
    private BigDecimal openMold1Position;
    private BigDecimal openMold2Pressure;
    private BigDecimal openMold2Flow;
    private BigDecimal openMold2Position;
    private BigDecimal openMold3Pressure;
    private BigDecimal openMold3Flow;
    private BigDecimal openMold3Position;
    private BigDecimal openMold4Pressure;
    private BigDecimal openMold4Flow;
    private BigDecimal openMold4Position;
    
    // 抽芯
    private BigDecimal corePull1OutPressure;
    private BigDecimal corePull1OutFlow;
    private BigDecimal corePull1OutPosition;
    private BigDecimal corePull2OutPressure;
    private BigDecimal corePull2OutFlow;
    private BigDecimal corePull2OutPosition;
    
    // 熔胶
    private BigDecimal melt1Pressure;
    private BigDecimal melt1Flow;
    private BigDecimal melt1Position;
    private BigDecimal decompressionBeforeMeltPressure;
    private BigDecimal decompressionBeforeMeltFlow;
    private BigDecimal decompressionBeforeMeltPosition;
    private BigDecimal decompressionAfterMeltPressure;
    private BigDecimal decompressionAfterMeltFlow;
    private BigDecimal decompressionAfterMeltPosition;
    
    // 顶出
    private BigDecimal eject1SpeedPressure;
    private BigDecimal eject1SpeedFlow;
    private BigDecimal eject1SpeedPosition;
    private BigDecimal eject2SpeedPressure;
    private BigDecimal eject2SpeedFlow;
    private BigDecimal eject2SpeedPosition;
    private BigDecimal ejectRetract1SpeedPressure;
    private BigDecimal ejectRetract1SpeedFlow;
    private BigDecimal ejectRetract1SpeedPosition;
    private BigDecimal ejectRetract2SpeedPressure;
    private BigDecimal ejectRetract2SpeedFlow;
    private BigDecimal ejectRetract2SpeedPosition;
    
    // 特殊模式参数
    private String injectionMode;
    private String corePullInMethod;
    private String corePullOutMethod;
    private String nozzleContactMethod;
    private String ejectionMode;
    private Integer ejectionCount;
    private BigDecimal screwSpeed;
    private String corePullStrokeMethod;
    
    // 温度参数
    private BigDecimal barrelTemp1;
    private BigDecimal barrelTemp2;
    private BigDecimal barrelTemp3;
    private BigDecimal barrelTemp4;
    private BigDecimal moldTemp;
    
    // 时间参数
    private BigDecimal clampingTime;
    private BigDecimal moldProtectionTime;
    private BigDecimal corePull1InTime;
    private BigDecimal corePull2InTime;
    private BigDecimal injectionTime;
    private BigDecimal holdingTime;
    private BigDecimal coolingTime;
    private BigDecimal corePull1OutTime;
    private BigDecimal corePull2OutTime;
    private BigDecimal moldOpeningTime;
    private BigDecimal partEjectionTime;
    private BigDecimal totalTime;
    
    // 原材料干燥处理
    private String dryingEquipment;
    private String materialFillHeight;
    private BigDecimal materialTurningTime;
    private String dryingTemp;
    private String frontMoldCooling;
    
    // 零件后处理
    private String partPostTreatment;
    private String productPostTreatment;
    private BigDecimal heatingTemp;
    private BigDecimal holdingTemp;
    private BigDecimal dryingTime;
    private String rearMoldCooling;
    
    // 工序内容和品质检查
    private String processContent;
    private String qualityInspection;
    private String comprehensiveAssessment;
}
