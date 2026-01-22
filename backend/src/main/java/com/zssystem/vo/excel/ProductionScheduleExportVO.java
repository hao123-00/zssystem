package com.zssystem.vo.excel;

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
    @ExcelProperty(value = "第1天", index = 15)
    @ColumnWidth(18)
    private String day1;
    
    @ExcelProperty(value = "第2天", index = 16)
    @ColumnWidth(18)
    private String day2;
    
    @ExcelProperty(value = "第3天", index = 17)
    @ColumnWidth(18)
    private String day3;
    
    @ExcelProperty(value = "第4天", index = 18)
    @ColumnWidth(18)
    private String day4;
    
    @ExcelProperty(value = "第5天", index = 19)
    @ColumnWidth(18)
    private String day5;
    
    @ExcelProperty(value = "第6天", index = 20)
    @ColumnWidth(18)
    private String day6;
    
    @ExcelProperty(value = "第7天", index = 21)
    @ColumnWidth(18)
    private String day7;
    
    @ExcelProperty(value = "第8天", index = 22)
    @ColumnWidth(18)
    private String day8;
    
    @ExcelProperty(value = "第9天", index = 23)
    @ColumnWidth(18)
    private String day9;
    
    @ExcelProperty(value = "第10天", index = 24)
    @ColumnWidth(18)
    private String day10;
    
    @ExcelProperty(value = "第11天", index = 25)
    @ColumnWidth(18)
    private String day11;
    
    @ExcelProperty(value = "第12天", index = 26)
    @ColumnWidth(18)
    private String day12;
    
    @ExcelProperty(value = "第13天", index = 27)
    @ColumnWidth(18)
    private String day13;
    
    @ExcelProperty(value = "第14天", index = 28)
    @ColumnWidth(18)
    private String day14;
    
    @ExcelProperty(value = "第15天", index = 29)
    @ColumnWidth(18)
    private String day15;
    
    @ExcelProperty(value = "第16天", index = 30)
    @ColumnWidth(18)
    private String day16;
    
    @ExcelProperty(value = "第17天", index = 31)
    @ColumnWidth(18)
    private String day17;
    
    @ExcelProperty(value = "第18天", index = 32)
    @ColumnWidth(18)
    private String day18;
    
    @ExcelProperty(value = "第19天", index = 33)
    @ColumnWidth(18)
    private String day19;
    
    @ExcelProperty(value = "第20天", index = 34)
    @ColumnWidth(18)
    private String day20;
    
    @ExcelProperty(value = "第21天", index = 35)
    @ColumnWidth(18)
    private String day21;
    
    @ExcelProperty(value = "第22天", index = 36)
    @ColumnWidth(18)
    private String day22;
    
    @ExcelProperty(value = "第23天", index = 37)
    @ColumnWidth(18)
    private String day23;
    
    @ExcelProperty(value = "第24天", index = 38)
    @ColumnWidth(18)
    private String day24;
    
    @ExcelProperty(value = "第25天", index = 39)
    @ColumnWidth(18)
    private String day25;
    
    @ExcelProperty(value = "第26天", index = 40)
    @ColumnWidth(18)
    private String day26;
    
    @ExcelProperty(value = "第27天", index = 41)
    @ColumnWidth(18)
    private String day27;
    
    @ExcelProperty(value = "第28天", index = 42)
    @ColumnWidth(18)
    private String day28;
    
    @ExcelProperty(value = "第29天", index = 43)
    @ColumnWidth(18)
    private String day29;
    
    @ExcelProperty(value = "第30天", index = 44)
    @ColumnWidth(18)
    private String day30;
}
