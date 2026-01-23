package com.zssystem.vo.excel;

import java.sql.Date;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 生产计划排程导出VO
 */
@Data
@ColumnWidth(20)
public class ProductionScheduleExportVO {
    
    // 设备信息
    @ExcelProperty(value = "组别", index = 0)
    @ColumnWidth(12)
    private String groupName;
    
    @ExcelProperty(value = "机台号", index = 1)
    @ColumnWidth(15)
    private String machineNo;
    
    @ExcelProperty(value = "设备型号", index = 2)
    @ColumnWidth(20)
    private String equipmentModel;
    
    @ExcelProperty(value = "机械手型号", index = 3)
    @ColumnWidth(20)
    private String robotModel;
    
    @ExcelProperty(value = "启用日期", index = 4)
    @ColumnWidth(15)
    private String enableDate;
    
    @ExcelProperty(value = "使用年限", index = 5)
    @ColumnWidth(12)
    private String serviceLife;
    
    @ExcelProperty(value = "模温机", index = 6)
    @ColumnWidth(15)
    private String moldTempMachine;
    
    @ExcelProperty(value = "冻水机", index = 7)
    @ColumnWidth(15)
    private String chiller;
    
    @ExcelProperty(value = "基本排模", index = 8)
    @ColumnWidth(15)
    private String basicMold;
    
    @ExcelProperty(value = "备用排模1", index = 9)
    @ColumnWidth(15)
    private String spareMold1;
    
    @ExcelProperty(value = "备用排模2", index = 10)
    @ColumnWidth(15)
    private String spareMold2;
    
    @ExcelProperty(value = "备用排模3", index = 11)
    @ColumnWidth(15)
    private String spareMold3;
    
    // 产品信息
    @ExcelProperty(value = "产品名称", index = 12)
    @ColumnWidth(20)
    private String productName;
    
    @ExcelProperty(value = "订单数量", index = 13)
    @ColumnWidth(12)
    private String orderQuantity;
    
    @ExcelProperty(value = "产能", index = 14)
    @ColumnWidth(12)
    private String dailyCapacity;
    
    // 每天的排程情况（动态列，最多30天，排除星期天）
    // 注意：列标题将在导出时动态设置为具体日期
    @ExcelProperty(value = "日期1", index = 15)
    @ColumnWidth(18)
    private String day1;
    
    @ExcelProperty(value = "日期2", index = 16)
    @ColumnWidth(18)
    private String day2;
    
    @ExcelProperty(value = "日期3", index = 17)
    @ColumnWidth(18)
    private String day3;
    
    @ExcelProperty(value = "日期4", index = 18)
    @ColumnWidth(18)
    private String day4;
    
    @ExcelProperty(value = "日期5", index = 19)
    @ColumnWidth(18)
    private String day5;
    
    @ExcelProperty(value = "日期6", index = 20)
    @ColumnWidth(18)
    private String day6;
    
    @ExcelProperty(value = "日期7", index = 21)
    @ColumnWidth(18)
    private String day7;
    
    @ExcelProperty(value = "日期8", index = 22)
    @ColumnWidth(18)
    private String day8;
    
    @ExcelProperty(value = "日期9", index = 23)
    @ColumnWidth(18)
    private String day9;
    
    @ExcelProperty(value = "日期10", index = 24)
    @ColumnWidth(18)
    private String day10;
    
    @ExcelProperty(value = "日期11", index = 25)
    @ColumnWidth(18)
    private String day11;
    
    @ExcelProperty(value = "日期12", index = 26)
    @ColumnWidth(18)
    private String day12;
    
    @ExcelProperty(value = "日期13", index = 27)
    @ColumnWidth(18)
    private String day13;
    
    @ExcelProperty(value = "日期14", index = 28)
    @ColumnWidth(18)
    private String day14;
    
    @ExcelProperty(value = "日期15", index = 29)
    @ColumnWidth(18)
    private String day15;
    
    @ExcelProperty(value = "日期16", index = 30)
    @ColumnWidth(18)
    private String day16;
    
    @ExcelProperty(value = "日期17", index = 31)
    @ColumnWidth(18)
    private String day17;
    
    @ExcelProperty(value = "日期18", index = 32)
    @ColumnWidth(18)
    private String day18;
    
    @ExcelProperty(value = "日期19", index = 33)
    @ColumnWidth(18)
    private String day19;
    
    @ExcelProperty(value = "日期20", index = 34)
    @ColumnWidth(18)
    private String day20;
    
    @ExcelProperty(value = "日期21", index = 35)
    @ColumnWidth(18)
    private String day21;
    
    @ExcelProperty(value = "日期22", index = 36)
    @ColumnWidth(18)
    private String day22;
    
    @ExcelProperty(value = "日期23", index = 37)
    @ColumnWidth(18)
    private String day23;
    
    @ExcelProperty(value = "日期24", index = 38)
    @ColumnWidth(18)
    private String day24;
    
    @ExcelProperty(value = "日期25", index = 39)
    @ColumnWidth(18)
    private String day25;
    
    @ExcelProperty(value = "日期26", index = 40)
    @ColumnWidth(18)
    private String day26;
    
    @ExcelProperty(value = "日期27", index = 41)
    @ColumnWidth(18)
    private String day27;
    
    @ExcelProperty(value = "日期28", index = 42)
    @ColumnWidth(18)
    private String day28;
    
    @ExcelProperty(value = "日期29", index = 43)
    @ColumnWidth(18)
    private String day29;
    
    @ExcelProperty(value = "日期30", index = 44)
    @ColumnWidth(18)
    private String day30;
}
