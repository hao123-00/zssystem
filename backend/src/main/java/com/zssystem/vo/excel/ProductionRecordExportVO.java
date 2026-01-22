package com.zssystem.vo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 生产记录导出VO
 */
@Data
@ColumnWidth(20)
public class ProductionRecordExportVO {
    
    @ExcelProperty(value = "记录编号", index = 0)
    @ColumnWidth(18)
    private String recordNo;
    
    // 设备信息
    @ExcelProperty(value = "组别", index = 1)
    @ColumnWidth(12)
    private String groupName;
    
    @ExcelProperty(value = "机台号", index = 2)
    @ColumnWidth(15)
    private String machineNo;
    
    @ExcelProperty(value = "设备编号", index = 3)
    @ColumnWidth(15)
    private String equipmentNo;
    
    @ExcelProperty(value = "设备名称", index = 4)
    @ColumnWidth(20)
    private String equipmentName;
    
    @ExcelProperty(value = "设备型号", index = 5)
    @ColumnWidth(20)
    private String equipmentModel;
    
    @ExcelProperty(value = "机械手型号", index = 6)
    @ColumnWidth(20)
    private String robotModel;
    
    @ExcelProperty(value = "启用日期", index = 7)
    @ColumnWidth(15)
    private String enableDate;
    
    @ExcelProperty(value = "使用年限", index = 8)
    @ColumnWidth(12)
    private String serviceLife;
    
    @ExcelProperty(value = "模温机", index = 9)
    @ColumnWidth(15)
    private String moldTempMachine;
    
    @ExcelProperty(value = "冻水机", index = 10)
    @ColumnWidth(15)
    private String chiller;
    
    @ExcelProperty(value = "基本排模", index = 11)
    @ColumnWidth(15)
    private String basicMold;
    
    @ExcelProperty(value = "备用排模1", index = 12)
    @ColumnWidth(15)
    private String spareMold1;
    
    @ExcelProperty(value = "备用排模2", index = 13)
    @ColumnWidth(15)
    private String spareMold2;
    
    @ExcelProperty(value = "备用排模3", index = 14)
    @ColumnWidth(15)
    private String spareMold3;
    
    // 产品信息
    @ExcelProperty(value = "产品名称", index = 15)
    @ColumnWidth(20)
    private String productName;
    
    @ExcelProperty(value = "订单数量", index = 16)
    @ColumnWidth(12)
    private String orderQuantity;
    
    @ExcelProperty(value = "产能", index = 17)
    @ColumnWidth(12)
    private String dailyCapacity;
    
    @ExcelProperty(value = "剩余数量", index = 18)
    @ColumnWidth(12)
    private String remainingQuantity;
    
    // 排程情况（汇总信息）
    @ExcelProperty(value = "排程日期", index = 19)
    @ColumnWidth(15)
    private String scheduleDates;
    
    @ExcelProperty(value = "排程产品", index = 20)
    @ColumnWidth(20)
    private String scheduleProducts;
    
    // 生产记录信息
    @ExcelProperty(value = "生产日期", index = 21)
    @ColumnWidth(15)
    private String productionDate;
    
    @ExcelProperty(value = "产量", index = 22)
    @ColumnWidth(12)
    private Integer quantity;
    
    @ExcelProperty(value = "不良品数量", index = 23)
    @ColumnWidth(15)
    private Integer defectQuantity;
    
    @ExcelProperty(value = "合格率", index = 24)
    @ColumnWidth(12)
    private String passRate;
    
    @ExcelProperty(value = "开始时间", index = 25)
    @ColumnWidth(20)
    private String startTime;
    
    @ExcelProperty(value = "结束时间", index = 26)
    @ColumnWidth(20)
    private String endTime;
    
    @ExcelProperty(value = "备注", index = 27)
    @ColumnWidth(30)
    private String remark;
}
