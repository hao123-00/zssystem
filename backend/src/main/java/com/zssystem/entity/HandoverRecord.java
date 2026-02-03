package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("handover_record")
public class HandoverRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("equipment_id")
    private Long equipmentId;

    @TableField("equipment_no")
    private String equipmentNo;

    @TableField("record_date")
    private LocalDateTime recordDate;

    private String shift;          // 班次
    private String productName;    // 产品名称
    private String material;       // 材质

    @TableField("equipment_cleaning")
    private String equipmentCleaning;  // 设备清洁

    @TableField("floor_cleaning")
    private String floorCleaning;      // 地面清洁

    private String leakage;            // 有无漏油

    @TableField("item_placement")
    private String itemPlacement;      // 物品摆放

    @TableField("injection_machine")
    private String injectionMachine;   // 注塑机

    private String robot;              // 机械手

    @TableField("assembly_line")
    private String assemblyLine;       // 流水线

    private String mold;               // 模具
    private String process;            // 工艺

    @TableField("handover_leader")
    private String handoverLeader;     // 交接组长

    @TableField("receiving_leader")
    private String receivingLeader;    // 接班组长

    @TableField("photo_path")
    private String photoPath;           // 拍照照片路径，15天后自动删除

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
