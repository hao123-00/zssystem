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
        for (int i = 0; i < Math.min(30, dateList.size()); i++) {
            int columnIndex = startColumnIndex + i;
            Cell cell = headerRow.getCell(columnIndex);
            if (cell == null) {
                // 如果单元格不存在，创建它
                cell = headerRow.createCell(columnIndex);
            }
            if (i < dateList.size()) {
                // 使用从前端传递的日期值（格式：yyyy-MM-dd，如：2026-01-02）
                cell.setCellValue(dateList.get(i));
            }
        }
    }
}
