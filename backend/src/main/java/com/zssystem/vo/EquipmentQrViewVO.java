package com.zssystem.vo;

import lombok.Data;
import java.util.List;

/**
 * 设备扫码查看页数据 VO（供微信扫码后展示）
 */
@Data
public class EquipmentQrViewVO {
    private EquipmentBriefVO equipment;
    private List<EquipmentCheckVO> checkRecords;
    private ProcessFileBriefVO enabledProcessFile;
    private String checkMonth; // 当前月份 yyyy-MM

    @Data
    public static class EquipmentBriefVO {
        private Long id;
        private String equipmentNo;
        private String equipmentName;
        private String machineNo;
    }

    @Data
    public static class ProcessFileBriefVO {
        private Long id;
        private String fileNo;
        private String fileName;
        private String versionText;
    }
}
