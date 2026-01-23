package com.zssystem.util;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 生产计划排程Excel导出自定义表头处理器
 * 用于动态设置日期列标题
 */
public class ScheduleExcelWriteHandler implements SheetWriteHandler {
    
    private final LocalDate startDate;
    
    public ScheduleExcelWriteHandler(LocalDate startDate) {
        this.startDate = startDate != null ? startDate : LocalDate.now();
    }
    
    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, 
                                 WriteSheetHolder writeSheetHolder) {
        Sheet sheet = writeSheetHolder.getSheet();
        Row headerRow = sheet.getRow(0);
        
        if (headerRow == null) {
            return;
        }
        
        // 生成30天的日期列表（排除星期天）
        List<LocalDate> dateList = generateDateList(startDate, 30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 从第15列开始（索引15，对应day1）设置日期标题
        int startColumnIndex = 15;
        for (int i = 0; i < Math.min(30, dateList.size()); i++) {
            int columnIndex = startColumnIndex + i;
            Cell cell = headerRow.getCell(columnIndex);
            if (cell != null) {
                // 将日期格式化为字符串（与前端格式一致：yyyy-MM-dd，如：2026-01-02）
                cell.setCellValue(dateList.get(i).format(formatter));
            }
        }
    }
    
    /**
     * 生成日期列表（排除星期天）
     */
    private List<LocalDate> generateDateList(LocalDate startDate, int maxDays) {
        List<LocalDate> dateList = new java.util.ArrayList<>();
        LocalDate currentDate = startDate;
        int dayCount = 0;
        
        while (dayCount < maxDays && dateList.size() < maxDays) {
            if (currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                dateList.add(currentDate);
                dayCount++;
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return dateList;
    }
}
