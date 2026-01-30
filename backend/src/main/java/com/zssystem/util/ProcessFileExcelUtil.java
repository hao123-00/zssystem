package com.zssystem.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.util.Units;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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
     * @param signatureType 签名类型：SUBMIT-编制人，APPROVE_LEVEL1-审核人，APPROVE_LEVEL2-批准人，APPROVE_LEVEL3-会签
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
            
            // 读取并处理签名图片（裁剪空白区域）
            byte[] imageBytes = Files.readAllBytes(Paths.get(signatureImagePath));
            System.out.println("原始签名图片大小: " + imageBytes.length + " 字节");
            
            // 裁剪签名图片的空白区域
            byte[] croppedImageBytes = cropSignatureWhitespace(imageBytes);
            System.out.println("裁剪后签名图片大小: " + croppedImageBytes.length + " 字节");
            
            // 插入图片到Excel（传递签名类型用于特殊处理）
            insertImageToCell(sheet, position, croppedImageBytes, workbook, signatureType);
            
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
        
        // 打印Row 27和Row 28的所有单元格内容，帮助调试
        System.out.println("=== Row 27 (Excel第28行) 单元格内容 ===");
        Row row27 = sheet.getRow(27);
        if (row27 != null) {
            for (int col = 0; col <= 15; col++) {
                Cell cell = row27.getCell(col);
                String value = cell != null ? getCellValueAsString(cell) : "(null)";
                if (value != null && !value.isEmpty()) {
                    System.out.println("  列" + col + ": '" + value + "'");
                }
            }
        } else {
            System.out.println("  Row 27 为空");
        }
    }
    
    /**
     * 根据签名类型获取标签文本
     * 审批流程：注塑组长提交(编制人) → 车间主任审核(审核人) → 生产技术部经理批准(批准人) → 注塑部经理会签(会签)
     */
    private static String getLabelBySignatureType(String signatureType) {
        return switch (signatureType) {
            case "SUBMIT" -> PREPARER_LABEL; // 编制人（注塑组长）
            case "APPROVE_LEVEL1" -> REVIEWER_LABEL; // 审核人（车间主任）
            case "APPROVE_LEVEL2" -> APPROVER_LABEL; // 批准人（生产技术部经理）
            case "APPROVE_LEVEL3" -> COUNTERSIGN_LABEL; // 会签（注塑部经理）
            default -> null;
        };
    }
    
    /**
     * 查找单元格位置（通过搜索文本）
     */
    private static CellPosition findCellPosition(Sheet sheet, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            System.out.println("findCellPosition: 搜索文本为空");
            return null;
        }
        
        System.out.println("findCellPosition: 开始搜索标签 '" + searchText + "'");
        System.out.println("findCellPosition: Sheet总行数 = " + (sheet.getLastRowNum() + 1));
        
        // 专门搜索第27行（Excel第28行）- 签名标签所在行
        Row row27 = sheet.getRow(27);
        if (row27 != null) {
            System.out.println("findCellPosition: 搜索第27行（Excel第28行）");
            for (int colIndex = 0; colIndex < 16; colIndex++) {
                Cell cell = row27.getCell(colIndex);
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell);
                    if (cellValue != null && !cellValue.isEmpty()) {
                        String trimmedCellValue = cellValue.trim();
                        String trimmedSearchText = searchText.trim();
                        
                        if (trimmedCellValue.equals(trimmedSearchText)) {
                            System.out.println("findCellPosition: 精确匹配找到标签 '" + searchText + "' 在: 行27, 列" + colIndex);
                            return new CellPosition(28, colIndex); // 返回下一行
                        }
                    }
                }
            }
        }
        
        // 遍历所有行搜索
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            
            for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                Cell cell = row.getCell(colIndex);
                if (cell == null) continue;
                
                String cellValue = getCellValueAsString(cell);
                if (cellValue != null) {
                    String trimmedCellValue = cellValue.trim();
                    String trimmedSearchText = searchText.trim();
                    
                    if (trimmedCellValue.equals(trimmedSearchText)) {
                        System.out.println("findCellPosition: 精确匹配找到标签 '" + searchText + "' 在: 行" + rowIndex + ", 列" + colIndex);
                        return new CellPosition(rowIndex + 1, colIndex);
                    }
                    if (trimmedCellValue.contains(trimmedSearchText)) {
                        System.out.println("findCellPosition: 包含匹配找到标签 '" + searchText + "' 在: 行" + rowIndex + ", 列" + colIndex);
                        return new CellPosition(rowIndex + 1, colIndex);
                    }
                }
            }
        }
        
        System.out.println("findCellPosition: 未找到标签 '" + searchText + "'");
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
     * 裁剪图片的空白区域（公开方法，供其他类调用）
     */
    public static byte[] cropImageWhitespace(byte[] imageBytes) {
        return cropSignatureWhitespace(imageBytes);
    }
    
    /**
     * 裁剪签名图片的空白区域，只保留签名部分
     */
    private static byte[] cropSignatureWhitespace(byte[] imageBytes) {
        try {
            // 读取图片
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(bais);
            
            if (originalImage == null) {
                System.err.println("无法读取签名图片");
                return imageBytes;
            }
            
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            System.out.println("原始图片尺寸: " + width + "x" + height);
            
            // 查找签名的边界（非空白区域）
            int minX = width, minY = height, maxX = 0, maxY = 0;
            
            // 白色和接近白色的阈值（RGB值大于此值视为空白）
            int whiteThreshold = 240;
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = originalImage.getRGB(x, y);
                    int alpha = (rgb >> 24) & 0xff;
                    int red = (rgb >> 16) & 0xff;
                    int green = (rgb >> 8) & 0xff;
                    int blue = rgb & 0xff;
                    
                    // 检查是否为非空白像素（考虑透明度和颜色）
                    boolean isTransparent = alpha < 128;
                    boolean isWhite = red > whiteThreshold && green > whiteThreshold && blue > whiteThreshold;
                    
                    if (!isTransparent && !isWhite) {
                        // 找到非空白像素
                        if (x < minX) minX = x;
                        if (y < minY) minY = y;
                        if (x > maxX) maxX = x;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            
            // 检查是否找到签名内容
            if (minX >= maxX || minY >= maxY) {
                System.out.println("未找到签名内容，使用原图");
                return imageBytes;
            }
            
            // 添加少量边距（5像素）
            int padding = 5;
            minX = Math.max(0, minX - padding);
            minY = Math.max(0, minY - padding);
            maxX = Math.min(width - 1, maxX + padding);
            maxY = Math.min(height - 1, maxY + padding);
            
            int cropWidth = maxX - minX + 1;
            int cropHeight = maxY - minY + 1;
            System.out.println("签名区域: (" + minX + "," + minY + ") 到 (" + maxX + "," + maxY + ")");
            System.out.println("裁剪后尺寸: " + cropWidth + "x" + cropHeight);
            
            // 裁剪图片
            BufferedImage croppedImage = originalImage.getSubimage(minX, minY, cropWidth, cropHeight);
            
            // 转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(croppedImage, "png", baos);
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.err.println("裁剪签名图片失败: " + e.getMessage());
            e.printStackTrace();
            return imageBytes;
        }
    }
    
    /**
     * 插入图片到指定单元格位置（不改变单元格大小）
     * @param signatureType 签名类型，用于特殊处理（如会签使用N:O列计算缩放）
     */
    private static void insertImageToCell(Sheet sheet, CellPosition position, byte[] imageBytes, Workbook workbook, String signatureType) {
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
        
        // 获取单元格范围（考虑合并单元格）
        int startCol = position.col;
        int endCol = position.col;
        int startRow = position.row;
        int endRow = position.row;
        
        // 查找合并单元格范围
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            org.apache.poi.ss.util.CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.isInRange(position.row, position.col)) {
                startCol = mergedRegion.getFirstColumn();
                endCol = mergedRegion.getLastColumn();
                startRow = mergedRegion.getFirstRow();
                endRow = mergedRegion.getLastRow();
                System.out.println("检测到合并单元格: 列" + startCol + "-" + endCol + ", 行" + startRow + "-" + endRow);
                break;
            }
        }
        
        // 计算单元格的实际像素尺寸
        // 列宽：Excel单位为1/256字符宽度，约7像素/字符
        double cellWidthPixels = 0;
        
        // 对于会签（APPROVE_LEVEL3），使用N:O列（列13-14）计算缩放宽度
        // 但实际单元格范围仍然是N:P列（列13-15）
        int scaleEndCol = endCol;
        if ("APPROVE_LEVEL3".equals(signatureType) && startCol == 13 && endCol == 15) {
            scaleEndCol = 14; // 使用N:O列计算缩放
            System.out.println("会签签名：使用N:O列（13-14）计算缩放，实际单元格范围N:P（13-15）");
        }
        
        for (int col = startCol; col <= scaleEndCol; col++) {
            int colWidth = sheet.getColumnWidth(col);
            cellWidthPixels += colWidth / 256.0 * 7;
        }
        
        // 行高：Excel单位为点(pt)，1点约1.33像素
        double cellHeightPixels = 0;
        for (int r = startRow; r <= endRow; r++) {
            Row targetRow = sheet.getRow(r);
            if (targetRow != null) {
                cellHeightPixels += targetRow.getHeightInPoints() * 1.33;
            } else {
                cellHeightPixels += sheet.getDefaultRowHeightInPoints() * 1.33;
            }
        }
        
        System.out.println("用于缩放计算的尺寸(像素): " + cellWidthPixels + "x" + cellHeightPixels);
        
        // 创建绘图对象
        XSSFDrawing drawing = xssfSheet.getDrawingPatriarch();
        if (drawing == null) {
            drawing = xssfSheet.createDrawingPatriarch();
        }
        
        // 读取图片尺寸
        BufferedImage img = null;
        try {
            img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException e) {
            System.err.println("读取图片尺寸失败: " + e.getMessage());
        }
        
        if (img == null) {
            System.err.println("无法读取签名图片");
            return;
        }
        
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        System.out.println("签名图片尺寸(像素): " + imgWidth + "x" + imgHeight);
        
        // 计算缩放比例，使图片适应单元格（留出边距）
        // 边距：水平方向留10%，垂直方向留15%
        double availableWidth = cellWidthPixels * 0.90;
        double availableHeight = cellHeightPixels * 0.85;
        
        double scaleX = availableWidth / imgWidth;
        double scaleY = availableHeight / imgHeight;
        double scale = Math.min(scaleX, scaleY);
        
        // 确保缩放比例在合理范围内
        if (scale > 1.0) {
            scale = 1.0; // 不放大图片
        }
        if (scale < 0.1) {
            scale = 0.1; // 最小缩放10%
        }
        
        // 计算缩放后的图片尺寸
        int scaledWidth = (int) (imgWidth * scale);
        int scaledHeight = (int) (imgHeight * scale);
        System.out.println("缩放比例: " + String.format("%.2f", scale) + ", 缩放后尺寸: " + scaledWidth + "x" + scaledHeight);
        
        // 计算图片在单元格内的居中偏移（像素）
        // 对于会签，居中计算也使用N:O列的宽度
        int offsetXPixels = (int) ((cellWidthPixels - scaledWidth) / 2);
        int offsetYPixels = (int) ((cellHeightPixels - scaledHeight) / 2);
        
        // 确保偏移为正值
        offsetXPixels = Math.max(2, offsetXPixels);
        offsetYPixels = Math.max(2, offsetYPixels);
        
        System.out.println("居中偏移(像素): (" + offsetXPixels + ", " + offsetYPixels + ")");
        
        // 添加图片到工作簿
        int pictureIndex = xssfWorkbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
        
        // 创建图片锚点
        // EMU单位：1英寸 = 914400 EMU，1像素 ≈ 9525 EMU (96 DPI)
        int emuPerPixel = Units.EMU_PER_PIXEL;
        
        // 计算起始偏移（EMU）
        int dx1 = offsetXPixels * emuPerPixel;
        int dy1 = offsetYPixels * emuPerPixel;
        
        // 计算结束偏移（从起始单元格计算）
        int dx2 = (offsetXPixels + scaledWidth) * emuPerPixel;
        int dy2 = (offsetYPixels + scaledHeight) * emuPerPixel;
        
        // 创建锚点 - 图片完全在起始单元格范围内
        XSSFClientAnchor anchor = new XSSFClientAnchor(
            dx1, dy1,  // 左上角偏移
            dx2, dy2,  // 右下角偏移（相对于起始单元格）
            (short) startCol, startRow,  // 起始单元格
            (short) startCol, startRow   // 结束单元格（同一单元格）
        );
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        
        // 创建图片对象
        XSSFPicture picture = drawing.createPicture(anchor, pictureIndex);
        
        System.out.println("图片已插入到位置: 行" + startRow + ", 列" + startCol);
    }
    
    /** 受控章在 Excel 中的位置：L24:P27（列11-15，行23-26，0-based） */
    private static final int SEAL_COL1 = 11;
    private static final int SEAL_ROW1 = 23;
    private static final int SEAL_COL2 = 15;
    private static final int SEAL_ROW2 = 26;
    
    /**
     * 工艺文件会签完成后，在 Excel 区域 L24:P27 加盖受控章（红框 + 红色「受控」）。
     *
     * @param excelFilePath Excel 文件路径
     * @return 是否成功
     */
    public static boolean insertControlledSealToExcel(String excelFilePath) {
        File excelFile = new File(excelFilePath);
        if (!excelFile.exists()) {
            System.err.println("insertControlledSeal: Excel文件不存在: " + excelFilePath);
            return false;
        }
        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (!(sheet instanceof XSSFSheet) || !(workbook instanceof XSSFWorkbook)) {
                return false;
            }
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
            byte[] sealBytes = createControlledSealImage();
            if (sealBytes == null || sealBytes.length == 0) {
                System.err.println("insertControlledSeal: 生成受控章图片失败");
                return false;
            }
            XSSFDrawing drawing = xssfSheet.getDrawingPatriarch();
            if (drawing == null) {
                drawing = xssfSheet.createDrawingPatriarch();
            }
            int pictureIndex = xssfWorkbook.addPicture(sealBytes, Workbook.PICTURE_TYPE_PNG);
            XSSFClientAnchor anchor = new XSSFClientAnchor(
                0, 0, 0, 0,
                (short) SEAL_COL1, SEAL_ROW1,
                (short) SEAL_COL2, SEAL_ROW2
            );
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
            drawing.createPicture(anchor, pictureIndex);
            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
            }
            System.out.println("受控章已插入到 Excel 区域 L24:P27");
            return true;
        } catch (Exception e) {
            System.err.println("插入受控章失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 生成受控章图片：红矩形边框 + 红色「受控」横排居中，透明底，半透明水印效果。外框长度减少 40%（为原 60%）。
     */
    private static byte[] createControlledSealImage() {
        int w = 168;   // 280 * 0.6，外框长度减少 40%
        int h = 60;    // 100 * 0.6
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0f));
            g2.fillRect(0, 0, w, h);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            java.awt.Color red = new java.awt.Color(200, 0, 0);
            g2.setColor(red);
            g2.setStroke(new BasicStroke(6f));  // 外框线宽随外框缩小
            int margin = 3;
            g2.drawRect(margin, margin, w - margin * 2, h - margin * 2);
            java.awt.Font font = getSealFont(48);  // 框内字体不变
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics(font);
            String text = "受控";
            int tw = fm.stringWidth(text);
            int ascent = fm.getAscent();
            g2.drawString(text, (w - tw) / 2f, (h + ascent) / 2f - 4);
        } finally {
            g2.dispose();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            System.err.println("写入受控章PNG失败: " + e.getMessage());
            return new byte[0];
        }
    }
    
    private static java.awt.Font getSealFont(int size) {
        String[] names = {"SimSun", "宋体", "Microsoft YaHei", "微软雅黑", "STSong", "Dialog"};
        for (String name : names) {
            java.awt.Font f = new java.awt.Font(name, java.awt.Font.BOLD, size);
            if (f.getFamily().equals(name) || f.canDisplayUpTo("受控") == -1) {
                return f;
            }
        }
        return new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, size);
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
