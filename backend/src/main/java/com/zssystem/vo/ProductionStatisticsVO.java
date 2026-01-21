package com.zssystem.vo;

import lombok.Data;

@Data
public class ProductionStatisticsVO {
    private String dimension; // 统计维度：date, order, equipment, product
    private String dimensionValue; // 维度值
    private Integer totalQuantity; // 总产量
    private Integer totalDefectQuantity; // 总不良品数量
    private Double passRate; // 合格率
}
