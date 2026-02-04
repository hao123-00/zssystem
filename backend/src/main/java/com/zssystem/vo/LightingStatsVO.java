package com.zssystem.vo;

import lombok.Data;

/**
 * 灯光管理示意图统计：本月至今完成次数、拍照完成率
 */
@Data
public class LightingStatsVO {
    /** 本月第一天至今天的完成次数（按时拍照次数汇总） */
    private Integer completionCount;
    /** 本月第一天至今天的天数 */
    private Integer days;
    /** 拍照完成率 = 完成次数 / (天数 * 2)，0~1 */
    private Double completionRate;
}
