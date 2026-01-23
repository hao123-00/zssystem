package com.zssystem.util;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.*;

import java.util.List;

/**
 * 生产计划排程Excel导出自定义表头处理器
 * 用于动态设置日期列标题（使用从前端传递的日期列表）
 * 同时实现 RowWriteHandler 和 SheetWriteHandler 以确保表头被正确修改
 */
public class ScheduleExcelWriteHandler implements RowWriteHandler, SheetWriteHandler {
    
    private final List<String> dateList; // 从前端传递的日期列表
    
    public ScheduleExcelWriteHandler(List<String> dateList) {
        this.dateList = dateList;
    }
    
    /**
     * 在表头行写入后修改日期列标题
     */
    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, 
                                WriteTableHolder writeTableHolder, 
                                Row row, 
                                Integer relativeRowIndex, 
                                Boolean isHead) {
        // 只处理表头行（第一行，索引0）
        if (isHead != null && isHead && relativeRowIndex != null && relativeRowIndex == 0) {
            updateDateHeaders(row);
        }
    }
    
    /**
     * 在Sheet创建后再次检查并更新表头（作为备用方案）
     */
    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, 
                                 WriteSheetHolder writeSheetHolder) {
        Sheet sheet = writeSheetHolder.getSheet();
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            updateDateHeaders(headerRow);
            // 隐藏超出本月范围的列
            hideExtraColumns(sheet);
        }
    }
    
    /**
     * 隐藏超出本月范围的日期列
     */
    private void hideExtraColumns(Sheet sheet) {
        if (dateList == null || dateList.isEmpty()) {
            return;
        }
        
        int startColumnIndex = 15; // 日期列从第15列开始（索引15，对应day1）
        int maxColumns = 30; // ProductionScheduleExportVO最多有30个日期列
        
        // 隐藏超出本月范围的列（从dateList.size()开始到maxColumns）
        for (int i = dateList.size(); i < maxColumns; i++) {
            int columnIndex = startColumnIndex + i;
            sheet.setColumnHidden(columnIndex, true);
        }
    }
    
    /**
     * 更新日期列标题的通用方法
     */
    private void updateDateHeaders(Row headerRow) {
        if (dateList == null || dateList.isEmpty()) {
            return;
        }
        
        // 从第15列开始（索引15，对应day1）设置日期标题
        int startColumnIndex = 15;
        int maxColumns = 30; // ProductionScheduleExportVO最多有30个日期列
        
        // 设置实际日期列的标题
        for (int i = 0; i < dateList.size() && i < maxColumns; i++) {
            int columnIndex = startColumnIndex + i;
            Cell cell = headerRow.getCell(columnIndex);
            if (cell == null) {
                cell = headerRow.createCell(columnIndex);
            }
            // 使用从前端传递的日期值（格式：yyyy-MM-dd，如：2026-01-02）
            cell.setCellValue(dateList.get(i));
        }
        
        // 对于超出本月范围的列，隐藏或设置为空（避免显示"日期28"、"日期29"等）
        for (int i = dateList.size(); i < maxColumns; i++) {
            int columnIndex = startColumnIndex + i;
            Cell cell = headerRow.getCell(columnIndex);
            if (cell != null) {
                // 将超出范围的列标题设置为空字符串，或者可以隐藏列
                cell.setCellValue("");
            }
        }
    }
}
