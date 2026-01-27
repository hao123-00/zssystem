package com.zssystem.util;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

/**
 * 生产计划排程Excel导出自定义表头处理器
 * 用于动态设置多行表头（第一行：日期，第二行：部门名称、排产数量、剩余数量）
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
            // 确保第二行表头存在
            Sheet sheet = writeSheetHolder.getSheet();
            Row secondHeaderRow = sheet.getRow(1);
            if (secondHeaderRow == null) {
                addSecondHeaderRow(sheet, row);
            }
        }
    }
    
    /**
     * 在Sheet创建后添加第二行表头
     */
    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, 
                                 WriteSheetHolder writeSheetHolder) {
        Sheet sheet = writeSheetHolder.getSheet();
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            updateDateHeaders(headerRow);
            // 添加第二行表头（部门名称、排产数量、剩余数量）
            addSecondHeaderRow(sheet, headerRow);
            // 设置排产数量列的宽度为16mm
            setProductionQuantityColumnWidth(sheet);
            // 隐藏超出本月范围的列
            hideExtraColumns(sheet);
        }
    }
    
    /**
     * 更新第一行日期列标题
     */
    private void updateDateHeaders(Row headerRow) {
        if (dateList == null || dateList.isEmpty()) {
            return;
        }
        
        Sheet sheet = headerRow.getSheet();
        Workbook workbook = sheet.getWorkbook();
        int startColumnIndex = 15;
        
        // 创建统一的表头样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // 设置实际日期列的标题（每个日期需要3列，所以索引需要乘以3）
        // 只设置本月范围内的日期列
        for (int i = 0; i < dateList.size() && i < 30; i++) {
            int baseColumnIndex = startColumnIndex + (i * 3);
            
            // 先清除该位置可能存在的单元格
            Cell existingCell = headerRow.getCell(baseColumnIndex);
            if (existingCell != null) {
                headerRow.removeCell(existingCell);
            }
            
            // 创建新单元格并设置日期值（格式：yyyy-MM-dd，如：2026-01-02）
            Cell cell = headerRow.createCell(baseColumnIndex);
            cell.setCellValue(dateList.get(i));
            cell.setCellStyle(headerStyle);
            
            // 合并单元格：每个日期跨越3列（第一行的日期标题合并3列）
            // 注意：需要先删除可能存在的合并区域，避免重复合并
            try {
                CellRangeAddress mergedRegion = new CellRangeAddress(
                    0, 0, // 第一行（索引0）
                    baseColumnIndex, baseColumnIndex + 2 // 跨越3列
                );
                sheet.addMergedRegion(mergedRegion);
            } catch (Exception e) {
                // 如果合并失败（可能已存在），忽略错误
            }
        }
        
        // 清除超出本月范围的日期列标题（避免显示"日期28"、"日期29"等）
        int maxDays = 30;
        for (int i = dateList.size(); i < maxDays; i++) {
            int baseColumnIndex = startColumnIndex + (i * 3);
            // 清除该日期的3列标题
            for (int j = 0; j < 3; j++) {
                Cell cell = headerRow.getCell(baseColumnIndex + j);
                if (cell != null) {
                    headerRow.removeCell(cell);
                }
            }
        }
        
        // 为第一行的其他列也应用统一样式
        applyHeaderStyle(headerRow, workbook);
    }
    
    /**
     * 创建统一的表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }
    
    /**
     * 添加第二行表头：在日期列下面添加"产品名称"、"排产数量"、"剩余数量"
     * 同时合并静态表头列（前15列：组别、机台号、设备型号、机械手型号、启用日期、使用年限、
     * 模温机、冻水机、基本排模、备用排模1、备用排模2、备用排模3、产品名称、订单数量、产能）
     * 的第一行和第二行
     */
    private void addSecondHeaderRow(Sheet sheet, Row firstHeaderRow) {
        if (dateList == null || dateList.isEmpty()) {
            return;
        }
        
        // 创建第二行
        Row secondHeaderRow = sheet.createRow(1);
        
        // 创建统一的表头样式
        Workbook workbook = sheet.getWorkbook();
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // 合并静态表头列（索引0-14）：组别、机台号、设备型号、机械手型号、启用日期、使用年限、
        // 模温机、冻水机、基本排模、备用排模1、备用排模2、备用排模3、产品名称、订单数量、产能
        // 这15列的第一行和第二行需要合并
        for (int i = 0; i < 15; i++) {
            // 在第二行创建单元格（用于合并，但内容为空）
            Cell cell = secondHeaderRow.createCell(i);
            cell.setCellValue("");
            cell.setCellStyle(headerStyle);
            
            // 合并第一行和第二行（行索引0和1）
            try {
                CellRangeAddress mergedRegion = new CellRangeAddress(
                    0, 1, // 第一行和第二行（索引0和1）
                    i, i  // 单列
                );
                sheet.addMergedRegion(mergedRegion);
            } catch (Exception e) {
                // 如果合并失败（可能已存在），忽略错误
            }
        }
        
        // 从第15列开始，为每个日期添加3列：产品名称、排产数量、剩余数量
        int startColumnIndex = 15;
        for (int i = 0; i < dateList.size() && i < 30; i++) {
            int baseColumnIndex = startColumnIndex + (i * 3);
            
            // 产品名称
            Cell productCell = secondHeaderRow.createCell(baseColumnIndex);
            productCell.setCellValue("产品名称");
            productCell.setCellStyle(headerStyle);
            
            // 排产数量
            Cell productionCell = secondHeaderRow.createCell(baseColumnIndex + 1);
            productionCell.setCellValue("排产数量");
            productionCell.setCellStyle(headerStyle);
            
            // 剩余数量
            Cell remainingCell = secondHeaderRow.createCell(baseColumnIndex + 2);
            remainingCell.setCellValue("剩余数量");
            remainingCell.setCellStyle(headerStyle);
        }
    }
    
    /**
     * 为第一行表头应用统一样式
     */
    private void applyHeaderStyle(Row headerRow, Workbook workbook) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // 为所有表头单元格应用样式
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && cell.getCellStyle() == null) {
                cell.setCellStyle(headerStyle);
            }
        }
    }
    
    /**
     * 设置日期项目内列的宽度为16mm
     * Excel列宽单位：1/256个字符宽度
     * 16mm ≈ 45.28像素 ≈ 6.47个字符宽度 ≈ 1656个单位
     * 设置产品名称列和排产数量列的宽度为16mm
     */
    private void setProductionQuantityColumnWidth(Sheet sheet) {
        if (dateList == null || dateList.isEmpty()) {
            return;
        }
        
        int startColumnIndex = 15; // 日期列从第15列开始
        
        // 16mm转换为Excel列宽单位
        // 1mm ≈ 2.83像素，16mm ≈ 45.28像素
        // Excel默认字符宽度约7像素，所以16mm ≈ 6.47个字符宽度
        // POI列宽单位是1/256个字符宽度，所以16mm ≈ 6.47 * 256 ≈ 1656
        int columnWidth = 1656; // 16mm对应的列宽单位
        
        // 为每个日期的产品名称列和排产数量列设置宽度
        for (int i = 0; i < dateList.size() && i < 30; i++) {
            int baseColumnIndex = startColumnIndex + (i * 3);
            // 产品名称列（第1列）
            int productNameColumnIndex = baseColumnIndex;
            sheet.setColumnWidth(productNameColumnIndex, columnWidth);
            // 排产数量列（第2列）
            int productionQuantityColumnIndex = baseColumnIndex + 1;
            sheet.setColumnWidth(productionQuantityColumnIndex, columnWidth);
        }
    }
    
    /**
     * 隐藏超出本月范围的日期列
     */
    private void hideExtraColumns(Sheet sheet) {
        if (dateList == null || dateList.isEmpty()) {
            return;
        }
        
        int startColumnIndex = 15; // 日期列从第15列开始
        int maxDays = 30; // 最多30天
        
        // 隐藏超出本月范围的列（每个日期3列）
        for (int i = dateList.size(); i < maxDays; i++) {
            int baseColumnIndex = startColumnIndex + (i * 3);
            // 隐藏该日期的3列
            for (int j = 0; j < 3; j++) {
                sheet.setColumnHidden(baseColumnIndex + j, true);
            }
        }
    }
}
