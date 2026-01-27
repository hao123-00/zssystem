package com.zssystem.util;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.zssystem.vo.excel.ProductionScheduleExportVO;
import org.apache.poi.ss.usermodel.*;

import java.util.List;

/**
 * 生产计划排程Excel数据行写入处理器
 * 用于将每个日期字段的数据拆分为3列：部门名称、排产数量、剩余数量
 */
public class ScheduleDataWriteHandler implements RowWriteHandler {
    
    private final List<String> dateList; // 从前端传递的日期列表
    
    public ScheduleDataWriteHandler(List<String> dateList) {
        this.dateList = dateList;
    }
    
    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, 
                                WriteTableHolder writeTableHolder, 
                                Row row, 
                                Integer relativeRowIndex, 
                                Boolean isHead) {
        // 只处理数据行（不是表头行）
        // EasyExcel的数据行从索引1开始（索引0是表头），但我们的表头占用了前两行（索引0和1）
        // 所以数据行的实际索引应该是 relativeRowIndex + 1
        // 但由于我们在afterSheetCreate中已经插入了第二行表头，EasyExcel写入数据时会从索引1开始
        // 这会导致数据覆盖第二行表头，所以我们需要检查并调整
        if (isHead != null && !isHead) {
            // 检查当前行是否是第二行（索引1），如果是，说明需要向下移动
            Sheet sheet = writeSheetHolder.getSheet();
            int currentRowIndex = row.getRowNum();
            if (currentRowIndex == 1) {
                // 这是第二行，但应该是数据行，说明第二行表头还没有插入或者被覆盖了
                // 我们需要将这一行移动到第三行
                Row targetRow = sheet.getRow(2);
                if (targetRow == null) {
                    targetRow = sheet.createRow(2);
                }
                // 复制当前行到第三行
                copyRowData(row, targetRow);
                // 清空当前行（第二行应该留给表头）
                clearRow(row);
                // 处理第三行的数据
                writeScheduleDataFromRow(targetRow);
            } else {
                // 正常处理数据行
                writeScheduleDataFromRow(row);
            }
        }
    }
    
    /**
     * 复制行数据
     */
    private void copyRowData(Row sourceRow, Row targetRow) {
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            Cell sourceCell = sourceRow.getCell(i);
            if (sourceCell != null) {
                Cell targetCell = targetRow.createCell(i);
                if (sourceCell.getCellType() == CellType.STRING) {
                    targetCell.setCellValue(sourceCell.getStringCellValue());
                } else if (sourceCell.getCellType() == CellType.NUMERIC) {
                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                } else if (sourceCell.getCellType() == CellType.BOOLEAN) {
                    targetCell.setCellValue(sourceCell.getBooleanCellValue());
                }
            }
        }
    }
    
    /**
     * 清空行数据
     */
    private void clearRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                row.removeCell(cell);
            }
        }
    }
    
    /**
     * 从已写入的行中读取数据并重新写入（每个日期3列）
     */
    private void writeScheduleDataFromRow(Row row) {
        if (dateList == null || dateList.isEmpty()) {
            return;
        }
        
        int startColumnIndex = 15; // 日期列从第15列开始（EasyExcel写入的原始位置）
        
        // 为每个日期读取原始数据并拆分为3列
        for (int i = 0; i < dateList.size() && i < 30; i++) {
            // 读取原始单元格的值（EasyExcel写入的位置：每个日期间隔3列）
            int originalColumnIndex = startColumnIndex + (i * 3);
            Cell originalCell = row.getCell(originalColumnIndex);
            String dayValue = "-";
            if (originalCell != null) {
                if (originalCell.getCellType() == CellType.STRING) {
                    dayValue = originalCell.getStringCellValue();
                } else if (originalCell.getCellType() == CellType.NUMERIC) {
                    dayValue = String.valueOf((int) originalCell.getNumericCellValue());
                }
            }
            
            int baseColumnIndex = startColumnIndex + (i * 3);
            
            // 解析数据：格式为"产品名称|排产数量|剩余数量"
            String[] parts = dayValue != null && !dayValue.equals("-") ? dayValue.split("\\|") : new String[]{"-", "0", "0"};
            String productName = parts.length > 0 ? parts[0] : "-";
            String productionQuantity = parts.length > 1 ? parts[1] : "0";
            String remainingQuantity = parts.length > 2 ? parts[2] : "0";
            
            // 删除原始单元格
            if (originalCell != null) {
                row.removeCell(originalCell);
            }
            
            // 产品名称
            Cell productCell = row.createCell(baseColumnIndex);
            productCell.setCellValue(productName);
            
            // 排产数量（产能）
            Cell productionCell = row.createCell(baseColumnIndex + 1);
            try {
                productionCell.setCellValue(Integer.parseInt(productionQuantity));
            } catch (NumberFormatException e) {
                productionCell.setCellValue(0);
            }
            
            // 剩余数量
            Cell remainingCell = row.createCell(baseColumnIndex + 2);
            try {
                remainingCell.setCellValue(Integer.parseInt(remainingQuantity));
            } catch (NumberFormatException e) {
                remainingCell.setCellValue(0);
            }
        }
    }
}
