package com.zssystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HandoverRecordSaveDTO {
    private Long id;  // 编辑时传

    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    /** 记录时间由服务端在提交时自动设置为当前时间，前端不传 */
    private LocalDateTime recordDate;

    private String shift;           // 班次
    private String productName;     // 产品名称
    private String material;        // 材质

    private String equipmentCleaning;  // 设备清洁
    private String floorCleaning;      // 地面清洁
    private String leakage;            // 有无漏油
    private String itemPlacement;      // 物品摆放
    private String injectionMachine;   // 注塑机
    private String robot;              // 机械手
    private String assemblyLine;       // 流水线
    private String mold;              // 模具
    private String process;           // 工艺
    private String handoverLeader;    // 交接组长
    private String receivingLeader;   // 接班组长
    private String photoPath;         // 拍照照片路径（新增时必填，由上传接口返回）
}
