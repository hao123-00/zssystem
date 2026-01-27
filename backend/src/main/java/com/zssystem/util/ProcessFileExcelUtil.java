package com.zssystem.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 工艺文件Excel签名插入工具类
 */
public class ProcessFileExcelUtil {
    
    // 签名位置标识文本
    private static final String PREPARER_LABEL = "编制人";
    private static final String REVIEWER_LABEL = "审核人";
    private static final String COUNTERSIGN_LABEL = "会签";
    private static final String APPROVER_LABEL = "批准人";
    
    /**
     * 在工艺文件Excel中插入签名图片
     * 
     * @param excelFilePath Excel文件路径
     * @param signatureImagePath 签名图片路径
     * @param signatureType 签名类型：SUBMIT-编制人，APPROVE_LEVEL1-审核人，APPROVE_LEVEL2-会签，APPROVE_LEVEL3-批准人
     * @return 是否成功
     */
    public static boolean insertSignatureToExcel(String excelFilePath, String signatureImagePath, String signatureType) {
        System.out.println("========== 开始插入签名到Excel ==========");
        System.out.println("Excel文件路径: " + excelFilePath);
        System.out.println("签名图片路径: " + signatureImagePath);
        System.out.println("签名类型: " + signatureType);
        
        // 验证Excel文件是否存在
        File excelFile = new File(excelFilePath);
        if (!excelFile.exists()) {
            System.err.println("错误: Excel文件不存在: " + excelFilePath);
            return false;
        }
        System.out.println("Excel文件存在，大小: " + excelFile.length() + " 字节");
        
        // 验证签名图片是否存在
        File signatureFile = new File(signatureImagePath);
        if (!signatureFile.exists()) {
            System.err.println("错误: 签名图片不存在: " + signatureImagePath);
            return false;
        }
        System.out.println("签名图片存在，大小: " + signatureFile.length() + " 字节");
        
        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0); // 使用第一个Sheet
            System.out.println("Sheet名称: " + sheet.getSheetName());
            System.out.println("Sheet总行数: " + (sheet.getLastRowNum() + 1));
            
            // 查找签名位置
            String labelText = getLabelBySignatureType(signatureType);
            System.out.println("查找标签: " + labelText);
            
            CellPosition position = findCellPosition(sheet, labelText);
            
            if (position == null) {
                System.err.println("错误: 未找到标签: " + labelText);
                System.out.println("尝试搜索所有可能的标签...");
                // 尝试搜索所有标签，帮助调试
                searchAllLabels(sheet);
                return false;
            }
            
            System.out.println("找到标签位置: " + position);
            
            // 读取签名图片
            byte[] imageBytes = Files.readAllBytes(Paths.get(signatureImagePath));
            System.out.println("签名图片大小: " + imageBytes.length + " 字节");
            
            // 插入图片到Excel
            insertImageToCell(sheet, position, imageBytes, workbook);
            
            // 保存Excel文件
            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
                System.out.println("Excel文件已保存");
            }
            
            System.out.println("========== 签名图片已成功插入到Excel ==========");
            System.out.println("标签: " + labelText + ", 位置: " + position);
            return true;
            
        } catch (Exception e) {
            System.err.println("========== 插入签名图片到Excel失败 ==========");
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 搜索所有可能的标签（用于调试）
     */
    private static void searchAllLabels(Sheet sheet) {
        String[] allLabels = {PREPARER_LABEL, REVIEWER_LABEL, COUNTERSIGN_LABEL, APPROVER_LABEL};
        System.out.println("搜索所有标签:");
        for (String label : allLabels) {
            CellPosition pos = findCellPosition(sheet, label);
            if (pos != null) {
                System.out.println("  ✓ 找到 '" + label + "' 在位置: " + pos);
            } else {
                System.out.println("  ✗ 未找到 '" + label + "'");
            }
        }
    }
    
    /**
     * 根据签名类型获取标签文本
     */
    private static String getLabelBySignatureType(String signatureType) {
        return switch (signatureType) {
            case "SUBMIT" -> PREPARER_LABEL; // 编制人
            case "APPROVE_LEVEL1" -> REVIEWER_LABEL; // 审核人
            case "APPROVE_LEVEL2" -> COUNTERSIGN_LABEL; // 会签
            case "APPROVE_LEVEL3" -> APPROVER_LABEL; // 批准人
            default -> null;
        };
    }
    
    /**
     * 查找单元格位置（通过搜索文本）
     */
    private static CellPosition findCellPosition(Sheet sheet, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return null;
        }
        
        // 先尝试精确匹配
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            
            for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                Cell cell = row.getCell(colIndex);
                if (cell == null) continue;
                
                String cellValue = getCellValueAsString(cell);
                if (cellValue != null) {
                    // 去除空格后比较
                    String trimmedCellValue = cellValue.trim();
                    String trimmedSearchText = searchText.trim();
                    
                    // 精确匹配
                    if (trimmedCellValue.equals(trimmedSearchText)) {
                        System.out.println("精确匹配找到标签 '" + searchText + "' 在: 行" + rowIndex + ", 列" + colIndex);
                        // 找到标签，返回下方位置（行号+1，列号相同）
                        return new CellPosition(rowIndex + 1, colIndex);
                    }
                    // 包含匹配（作为备选）
                    if (trimmedCellValue.contains(trimmedSearchText)) {
                        System.out.println("包含匹配找到标签 '" + searchText + "' 在: 行" + rowIndex + ", 列" + colIndex);
                        return new CellPosition(rowIndex + 1, colIndex);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 获取单元格值（字符串）
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        // 处理整数和小数
                        double numValue = cell.getNumericCellValue();
                        if (numValue == (long) numValue) {
                            return String.valueOf((long) numValue);
                        } else {
                            return String.valueOf(numValue);
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    // 对于公式单元格，尝试获取计算结果
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception e) {
                        return cell.getCellFormula();
                    }
                case BLANK:
                    return "";
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("获取单元格值失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 插入图片到指定单元格位置
     */
    private static void insertImageToCell(Sheet sheet, CellPosition position, byte[] imageBytes, Workbook workbook) {
        if (!(sheet instanceof XSSFSheet) || !(workbook instanceof XSSFWorkbook)) {
            throw new RuntimeException("仅支持XLSX格式的Excel文件");
        }
        
        XSSFSheet xssfSheet = (XSSFSheet) sheet;
        XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
        
        // 获取或创建目标行
        Row row = sheet.getRow(position.row);
        if (row == null) {
            row = sheet.createRow(position.row);
        }
        
        // 获取或创建目标单元格
        Cell cell = row.getCell(position.col);
        if (cell == null) {
            cell = row.createCell(position.col);
        }
        
        // 设置行高和列宽（为图片预留空间）
        // 行高：80磅（约106像素），适合签名图片
        row.setHeightInPoints(80);
        
        // 列宽：如果当前列宽小于20个字符，则设置为20
        int currentWidth = sheet.getColumnWidth(position.col);
        int minWidth = 20 * 256; // 20个字符宽度
        if (currentWidth < minWidth) {
            sheet.setColumnWidth(position.col, minWidth);
        }
        
        // 创建绘图对象（如果已存在则获取）
        XSSFDrawing drawing = xssfSheet.getDrawingPatriarch();
        if (drawing == null) {
            drawing = xssfSheet.createDrawingPatriarch();
        }
        
        // 创建图片锚点（定位图片位置和大小）
        // dx1, dy1: 左上角偏移（单位：EMU，1/360000 cm）
        // dx2, dy2: 右下角偏移
        // col1, row1: 起始列和行
        // col2, row2: 结束列和行
        // 图片大小：约15行高，2列宽
        XSSFClientAnchor anchor = new XSSFClientAnchor(
            0, 0, // dx1, dy1: 左上角偏移（从单元格左上角开始）
            1023 * 2, 1023 * 15, // dx2, dy2: 右下角偏移（2列宽，15行高）
            (short) position.col, position.row, // 起始位置
            (short) (position.col + 1), position.row // 结束位置（跨1列）
        );
        
        // 添加图片到工作簿
        int pictureIndex = xssfWorkbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
        
        // 创建图片对象
        XSSFPicture picture = drawing.createPicture(anchor, pictureIndex);
        
        // 调整图片大小以适应单元格（保持宽高比）
        picture.resize();
        
        System.out.println("图片已插入到位置: 行" + position.row + ", 列" + position.col);
    }
    
    /**
     * 单元格位置内部类
     */
    private static class CellPosition {
        int row;
        int col;
        
        CellPosition(int row, int col) {
            this.row = row;
            this.col = col;
        }
        
        @Override
        public String toString() {
            return "行" + row + ", 列" + col;
        }
    }
}
