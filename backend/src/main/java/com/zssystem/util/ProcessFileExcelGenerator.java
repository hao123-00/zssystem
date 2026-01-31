package com.zssystem.util;

import com.zssystem.entity.ProcessFile;
import com.zssystem.entity.ProcessFileDetail;
import com.zssystem.entity.Equipment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 工艺文件Excel生成工具类
 * 严格按照"拆分Excel-plus1.md"文档的布局格式生成Excel文件
 */
public class ProcessFileExcelGenerator {
    
    /**
     * 生成工艺文件Excel
     */
    public static String generateProcessFileExcel(
            ProcessFile processFile,
            ProcessFileDetail processFileDetail,
            Equipment equipment,
            String savePath) throws IOException {
        
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("注塑工艺卡片");
        
        // 设置列宽（A-P列，16列），默认宽度为8.8个字符，D列为9.2个字符
        int defaultColumnWidth = (int) (8.8 * 256);
        for (int i = 0; i <= 15; i++) {
            sheet.setColumnWidth(i, defaultColumnWidth);
        }
        // D列（索引3）单独设置为9.2个字符
        int dColumnWidth = (int) (9.2 * 256);
        sheet.setColumnWidth(3, dColumnWidth);
        
        // 创建样式
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle labelStyle = createLabelStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        CellStyle verticalStyle = createVerticalStyle(workbook);
        CellStyle labelCenterStyle = createLabelCenterStyle(workbook);  // 居中标签样式
        CellStyle dataCenterStyle = createDataCenterStyle(workbook);    // 居中数据样式
        CellStyle dataLeftStyle = createDataLeftStyle(workbook);        // 左对齐数据样式
        
        // ========== Row 1-2: 表头 ==========
        createHeaderRows(sheet, processFileDetail, titleStyle, headerStyle, dataStyle);
        
        // ========== Row 3: 材料信息 ==========
        createMaterialInfoRow(sheet, processFileDetail, labelStyle, dataStyle, labelCenterStyle);
        
        // ========== Row 4: 零件信息 ==========
        createPartInfoRow(sheet, processFileDetail, equipment, labelStyle, dataStyle, labelCenterStyle, dataCenterStyle);
        
        // ========== Row 5-21: 模具和工艺参数 ==========
        createProcessParametersRows(sheet, processFileDetail, equipment, labelStyle, dataStyle, centerStyle, verticalStyle, labelCenterStyle);
        
        // ========== Row 22-27: 原料干燥处理和零件后处理 ==========
        createDryingAndProcessRows(sheet, processFileDetail, labelStyle, dataStyle, verticalStyle, labelCenterStyle, dataLeftStyle);
        
        // ========== Row 28-30: 签名和文件编号 ==========
        createSignatureAndFileNoRows(sheet, processFile, equipment, labelStyle, dataStyle, labelCenterStyle, dataLeftStyle);
        
        // ========== 设置所有行高为18磅（Row 1-30） ==========
        for (int i = 0; i <= 29; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(18f);
            }
        }
        
        // ========== 为所有单元格添加边框（A1-P30） ==========
        applyBorderToAllCells(sheet, workbook, 29, 15);
        
        // ========== 插入产品关键尺寸图片 ==========
        insertKeyDimensionImages(sheet, workbook, processFileDetail);

        // ========== 打印设置：横向、页边距上下左右 0.1 ==========
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        sheet.setMargin(Sheet.LeftMargin, 0.1);
        sheet.setMargin(Sheet.RightMargin, 0.1);
        sheet.setMargin(Sheet.TopMargin, 0.1);
        sheet.setMargin(Sheet.BottomMargin, 0.1);
        sheet.setMargin(Sheet.HeaderMargin, 0.0);
        sheet.setMargin(Sheet.FooterMargin, 0.0);
        sheet.setHorizontallyCenter(true);
        sheet.setVerticallyCenter(true);
        
        // 保存Excel文件
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String saveDir = savePath + "/" + yearMonth;
        FileUtil.createDirectoryIfNotExists(saveDir);
        
        String filePath = saveDir + "/" + processFile.getFileNo() + ".xlsx";
        
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }
        
        workbook.close();
        
        return filePath;
    }
    
    /**
     * 插入产品关键尺寸图片到Excel
     * 图片1插入到B8:D14，图片2插入到B15:D21
     */
    private static void insertKeyDimensionImages(XSSFSheet sheet, XSSFWorkbook workbook, ProcessFileDetail detail) {
        System.out.println("===== 开始插入产品关键尺寸图片 =====");
        System.out.println("图片1路径: " + detail.getProductKeyDimensionImage1());
        System.out.println("图片2路径: " + detail.getProductKeyDimensionImage2());
        
        // 获取或创建绘图对象
        XSSFDrawing drawing = sheet.getDrawingPatriarch();
        if (drawing == null) {
            drawing = sheet.createDrawingPatriarch();
        }
        
        // 插入图片1 (B8:D14, 列1-3, 行7-13)
        if (detail.getProductKeyDimensionImage1() != null && !detail.getProductKeyDimensionImage1().isEmpty()) {
            System.out.println("准备插入图片1...");
            insertImageToCellRange(sheet, workbook, drawing, detail.getProductKeyDimensionImage1(), 
                    1, 7, 4, 14);  // col1=1(B), row1=7(8), col2=4(D+1), row2=14(14+1)
        } else {
            System.out.println("图片1路径为空，跳过");
        }
        
        // 插入图片2 (B15:D21, 列1-3, 行14-20)
        if (detail.getProductKeyDimensionImage2() != null && !detail.getProductKeyDimensionImage2().isEmpty()) {
            System.out.println("准备插入图片2...");
            insertImageToCellRange(sheet, workbook, drawing, detail.getProductKeyDimensionImage2(), 
                    1, 14, 4, 21);  // col1=1(B), row1=14(15), col2=4(D+1), row2=21(21+1)
        } else {
            System.out.println("图片2路径为空，跳过");
        }
        
        System.out.println("===== 产品关键尺寸图片插入完成 =====");
    }
    
    /**
     * 插入图片到指定单元格范围
     */
    private static void insertImageToCellRange(XSSFSheet sheet, XSSFWorkbook workbook, XSSFDrawing drawing,
                                               String imagePath, int col1, int row1, int col2, int row2) {
        try {
            java.io.File imageFile = new java.io.File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("产品关键尺寸图片不存在: " + imagePath);
                return;
            }
            
            System.out.println("正在插入产品关键尺寸图片: " + imagePath);
            
            // 读取图片
            byte[] imageBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
            
            // 判断图片类型
            int pictureType = Workbook.PICTURE_TYPE_PNG;
            String lowerPath = imagePath.toLowerCase();
            if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
                pictureType = Workbook.PICTURE_TYPE_JPEG;
            }
            
            // 添加图片到工作簿
            int pictureIndex = workbook.addPicture(imageBytes, pictureType);
            
            // 创建锚点 - 图片从(col1, row1)开始，到(col2-1, row2-1)结束
            // dx/dy 是相对于单元格左上角的偏移量（EMU单位）
            int marginEmu = 5 * org.apache.poi.util.Units.EMU_PER_PIXEL;
            
            XSSFClientAnchor anchor = new XSSFClientAnchor(
                marginEmu,  // dx1 - 起始单元格内的X偏移
                marginEmu,  // dy1 - 起始单元格内的Y偏移
                0,          // dx2 - 结束单元格内的X偏移（0表示到单元格边缘）
                0,          // dy2 - 结束单元格内的Y偏移（0表示到单元格边缘）
                (short) col1, row1,   // 起始单元格 (col1, row1)
                (short) col2, row2    // 结束单元格 (col2, row2)
            );
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
            
            // 创建图片
            XSSFPicture picture = drawing.createPicture(anchor, pictureIndex);
            
            System.out.println("产品关键尺寸图片已插入: " + imagePath + ", 区域: B" + (row1+1) + ":D" + row2);
            
        } catch (Exception e) {
            System.err.println("插入产品关键尺寸图片失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建表头行（Row 1-2）
     * 严格按照拆分Excel-plus1.md的布局
     */
    private static void createHeaderRows(Sheet sheet, ProcessFileDetail detail, 
                                        CellStyle titleStyle, CellStyle headerStyle, CellStyle dataStyle) {
        Row row1 = sheet.createRow(0);
        Row row2 = sheet.createRow(1);
        
        // A1:D2 - 芜湖飞科电器有限公司
        Cell companyCell = row1.createCell(0);
        companyCell.setCellValue("芜湖飞科电器有限公司");
        companyCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 3));
        
        // E1:H2 - 塑料零件注塑卡片
        Cell titleCell = row1.createCell(4);
        titleCell.setCellValue("塑料零件注塑卡片");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 4, 7));
        
        // I1:J1 - 产品型号（标签）
        Cell productModelLabelCell = row1.createCell(8);
        productModelLabelCell.setCellValue("产品型号");
        productModelLabelCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 8, 9));
        
        // K1:L1 - 产品型号数据（填充到后一个单元格）
        Cell productModelDataCell = row1.createCell(10);
        productModelDataCell.setCellValue(detail.getProductModel() != null ? detail.getProductModel() : "");
        productModelDataCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 10, 11));
        
        // M1:N1 - 模具制造公司（标签）
        Cell moldCompanyLabelCell = row1.createCell(12);
        moldCompanyLabelCell.setCellValue("模具制造公司");
        moldCompanyLabelCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 12, 13));
        
        // O1:P1 - 模具制造公司数据（填充到后一个单元格）
        Cell moldCompanyDataCell = row1.createCell(14);
        moldCompanyDataCell.setCellValue(detail.getMoldManufacturingCompany() != null ? detail.getMoldManufacturingCompany() : "");
        moldCompanyDataCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 14, 15));
        
        // I2:J2 - 产品名称（标签）
        Cell productNameLabelCell = row2.createCell(8);
        productNameLabelCell.setCellValue("产品名称");
        productNameLabelCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 8, 9));
        
        // K2:L2 - 产品名称数据（填充到后一个单元格）
        Cell productNameDataCell = row2.createCell(10);
        productNameDataCell.setCellValue(detail.getProductName() != null ? detail.getProductName() : "");
        productNameDataCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 10, 11));
        
        // M2:N2 - 零件名称（标签）
        Cell partNameLabelCell = row2.createCell(12);
        partNameLabelCell.setCellValue("零件名称");
        partNameLabelCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 12, 13));
        
        // O2:P2 - 零件名称数据（填充到后一个单元格）
        Cell partNameDataCell = row2.createCell(14);
        partNameDataCell.setCellValue(detail.getPartName() != null ? detail.getPartName() : "");
        partNameDataCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 14, 15));
    }
    
    /**
     * 创建材料信息行（Row 3）
     * 严格按照拆分Excel-plus1.md的布局
     */
    private static void createMaterialInfoRow(Sheet sheet, ProcessFileDetail detail,
                                             CellStyle labelStyle, CellStyle dataStyle, CellStyle labelCenterStyle) {
        Row row3 = sheet.createRow(2);
        
        // A3 - 材料名称（标签）
        createLabelCell(row3, 0, "材料名称", labelStyle);
        // B3:C3 - 合并单元格，数据填充到B3:C3（后一个单元格）
        createDataCell(row3, 1, detail.getMaterialName(), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 2));
        
        // D3 - 材料牌号（标签）
        createLabelCell(row3, 3, "材料牌号", labelStyle);
        // E3:F3 - 合并单元格，数据填充到E3:F3（后一个单元格）
        createDataCell(row3, 4, detail.getMaterialGrade(), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 4, 5));
        
        // G3 - 材料颜色（标签）
        createLabelCell(row3, 6, "材料颜色", labelStyle);
        // H3:I3 - 合并单元格，数据填充到H3:I3（后一个单元格）
        createDataCell(row3, 7, detail.getMaterialColor(), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 7, 8));
        
        // J3 - 颜料名称（标签）
        createLabelCell(row3, 9, "颜料名称", labelStyle);
        // K3 - 空白单元格，数据填充到K3（后一个单元格）
        createDataCell(row3, 10, detail.getPigmentName(), dataStyle);
        
        // L3 - 比例（标签+数据组合显示在同一单元格）
        String ratioValue = formatDecimal(detail.getPigmentRatio());
        createDataCell(row3, 11, "比例：" + ratioValue, dataStyle);
        
        // M3:N3 - 项目负责人（标签，居中）
        createLabelCell(row3, 12, "项目负责人", labelCenterStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 12, 13));
        // O3:P3 - 项目负责人数据（所在行后一个合并单元格）
        createDataCell(row3, 14, detail.getProjectLeader(), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 14, 15));
    }
    
    /**
     * 创建零件信息行（Row 4）
     * 严格按照拆分Excel-plus1.md的布局
     */
    private static void createPartInfoRow(Sheet sheet, ProcessFileDetail detail, Equipment equipment,
                                          CellStyle labelStyle, CellStyle dataStyle, CellStyle labelCenterStyle, CellStyle dataCenterStyle) {
        Row row4 = sheet.getRow(3);
        if (row4 == null) row4 = sheet.createRow(3);
        
        // A4 - 零件净重（标签）
        createLabelCell(row4, 0, "零件净重", labelStyle);
        // B4:C4 - 合并单元格，数据填充到B4:C4（后一个单元格），带"g"单位
        createDataCell(row4, 1, formatWeight(detail.getPartNetWeight()), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 2));
        
        // D4 - 零件毛重（标签）
        createLabelCell(row4, 3, "零件毛重", labelStyle);
        // E4:F4 - 合并单元格，数据填充到E4:F4（后一个单元格），带"g"单位
        createDataCell(row4, 4, formatWeight(detail.getPartGrossWeight()), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 4, 5));
        
        // G4 - 消耗定额（标签）
        createLabelCell(row4, 6, "消耗定额", labelStyle);
        // H4:I4 - 合并单元格，数据填充到H4:I4（后一个单元格），带"g"单位
        createDataCell(row4, 7, formatWeight(detail.getConsumptionQuota()), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 7, 8));
        
        // J4:M4 - 使用设备名称/规格（标签，居中）
        createLabelCell(row4, 9, "使用设备名称/规格", labelCenterStyle);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 9, 12));
        
        // N4:P4 - 使用设备名称/规格数据（所在行后一个合并单元格，居中）
        String equipmentInfo = equipment != null 
            ? (equipment.getMachineNo() != null 
                ? equipment.getMachineNo() + " - " + equipment.getEquipmentName()
                : equipment.getEquipmentNo() + " - " + equipment.getEquipmentName())
            : "";
        createDataCell(row4, 13, equipmentInfo, dataCenterStyle);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 13, 15));
    }
    
    /**
     * 创建工艺参数行（Row 5-21）
     * 严格按照拆分Excel-plus1.md的布局
     */
    private static void createProcessParametersRows(Sheet sheet, ProcessFileDetail detail, Equipment equipment,
                                                   CellStyle labelStyle, CellStyle dataStyle, 
                                                   CellStyle centerStyle, CellStyle verticalStyle, CellStyle labelCenterStyle) {
        // Row 5
        Row row5 = sheet.getRow(4);
        if (row5 == null) row5 = sheet.createRow(4);
        
        // A5:A7 - 模具（纵向排列，字符正常方向）
        Cell moldCell = row5.createCell(0);
        moldCell.setCellValue(toVerticalText("模具"));
        moldCell.setCellStyle(verticalStyle);
        sheet.addMergedRegion(new CellRangeAddress(4, 6, 0, 0));
        
        // B5 - 模具编号（标签）
        createLabelCell(row5, 1, "模具编号", labelStyle);
        // C5:D5 - 合并单元格，数据填充到C5:D5（后一个单元格）
        createDataCell(row5, 2, detail.getMoldNumber(), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 2, 3));
        
        // E5:E21 - 注射成型工艺（纵向排列，字符正常方向）
        Cell processCell = row5.createCell(4);
        processCell.setCellValue(toVerticalText("注射成型工艺"));
        processCell.setCellStyle(verticalStyle);
        sheet.addMergedRegion(new CellRangeAddress(4, 20, 4, 4));
        
        // F5 - 动作
        createTitleCell(row5, 5, "动作", centerStyle);
        // G5 - 压力/bar
        createTitleCell(row5, 6, "压力/bar", centerStyle);
        // H5 - 流量/%
        createTitleCell(row5, 7, "流量/%", centerStyle);
        // I5 - 位置/mm
        createTitleCell(row5, 8, "位置/mm", centerStyle);
        // J5 - 动作
        createTitleCell(row5, 9, "动作", centerStyle);
        // K5 - 压力/bar
        createTitleCell(row5, 10, "压力/bar", centerStyle);
        // L5 - 流量/%
        createTitleCell(row5, 11, "流量/%", centerStyle);
        // M5 - 位置/mm
        createTitleCell(row5, 12, "位置/mm", centerStyle);
        
        // N5:N8 - 料筒温度℃（纵向排列，字符正常方向）
        Cell tempCell = row5.createCell(13);
        tempCell.setCellValue(toVerticalText("料筒温度℃"));
        tempCell.setCellStyle(verticalStyle);
        sheet.addMergedRegion(new CellRangeAddress(4, 7, 13, 13));
        
        // O5 - 第一段（标签）
        createLabelCell(row5, 14, "第一段", labelStyle);
        // P5 - 第一段温度数据
        createDataCell(row5, 15, formatTemperatureWithTolerance(detail.getBarrelTemp1()), dataStyle);
        
        // Row 6
        Row row6 = sheet.createRow(5);
        // B6 - 型腔数量（标签）
        createLabelCell(row6, 1, "型腔数量", labelStyle);
        // C6:D6 - 合并单元格，数据填充到C6:D6（后一个单元格）
        createDataCell(row6, 2, detail.getCavityQuantity() != null ? detail.getCavityQuantity().toString() : "", dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 2, 3));
        
        // F6 - 合模1（标签）
        createLabelCell(row6, 5, "合模1", labelStyle);
        // G6, H6, I6 - 三个数据单元格（压力、流量、位置）
        createDataCell(row6, 6, formatParameterWithTolerance(detail.getClamp1Pressure()), dataStyle);
        createDataCell(row6, 7, formatParameterWithTolerance(detail.getClamp1Flow()), dataStyle);
        createDataCell(row6, 8, formatParameterWithTolerance(detail.getClamp1Position()), dataStyle);
        
        // J6 - 开模1（标签）
        createLabelCell(row6, 9, "开模1", labelStyle);
        // K6, L6, M6 - 三个数据单元格（压力、流量、位置）
        createDataCell(row6, 10, formatParameterWithTolerance(detail.getOpenMold1Pressure()), dataStyle);
        createDataCell(row6, 11, formatParameterWithTolerance(detail.getOpenMold1Flow()), dataStyle);
        createDataCell(row6, 12, formatParameterWithTolerance(detail.getOpenMold1Position()), dataStyle);
        
        // O6 - 第二段（标签）
        createLabelCell(row6, 14, "第二段", labelStyle);
        // P6 - 第二段温度数据
        createDataCell(row6, 15, formatTemperatureWithTolerance(detail.getBarrelTemp2()), dataStyle);
        
        // Row 7
        Row row7 = sheet.createRow(6);
        // B7 - 锁模力（标签）
        createLabelCell(row7, 1, "锁模力", labelStyle);
        // C7:D7 - 合并单元格，数据填充到C7:D7（后一个单元格）
        createDataCell(row7, 2, formatDecimal(detail.getClampingForce()), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(6, 6, 2, 3));
        
        // F7 - 合模2（标签）
        createLabelCell(row7, 5, "合模2", labelStyle);
        // G7, H7, I7 - 三个数据单元格
        createDataCell(row7, 6, formatParameterWithTolerance(detail.getClamp2Pressure()), dataStyle);
        createDataCell(row7, 7, formatParameterWithTolerance(detail.getClamp2Flow()), dataStyle);
        createDataCell(row7, 8, formatParameterWithTolerance(detail.getClamp2Position()), dataStyle);
        
        // J7 - 开模2（标签）
        createLabelCell(row7, 9, "开模2", labelStyle);
        // K7, L7, M7 - 三个数据单元格
        createDataCell(row7, 10, formatParameterWithTolerance(detail.getOpenMold2Pressure()), dataStyle);
        createDataCell(row7, 11, formatParameterWithTolerance(detail.getOpenMold2Flow()), dataStyle);
        createDataCell(row7, 12, formatParameterWithTolerance(detail.getOpenMold2Position()), dataStyle);
        
        // O7 - 第三段（标签）
        createLabelCell(row7, 14, "第三段", labelStyle);
        // P7 - 第三段温度数据
        createDataCell(row7, 15, formatTemperatureWithTolerance(detail.getBarrelTemp3()), dataStyle);
        
        // Row 8
        Row row8 = sheet.createRow(7);
        // A8:A21 - 产品关键尺寸（纵向排列，字符正常方向）
        Cell keyDimCell = row8.createCell(0);
        keyDimCell.setCellValue(toVerticalText("产品关键尺寸"));
        keyDimCell.setCellStyle(verticalStyle);
        sheet.addMergedRegion(new CellRangeAddress(7, 20, 0, 0));
        
        // B8:D14 - 产品关键尺寸图片1区域（合并单元格）
        sheet.addMergedRegion(new CellRangeAddress(7, 13, 1, 3));
        createDataCell(row8, 1, "", dataStyle); // 空单元格，图片将由后续方法插入
        
        // B15:D21 - 产品关键尺寸图片2区域（合并单元格）
        Row keyDimRow15 = sheet.getRow(14);
        if (keyDimRow15 == null) keyDimRow15 = sheet.createRow(14);
        sheet.addMergedRegion(new CellRangeAddress(14, 20, 1, 3));
        createDataCell(keyDimRow15, 1, "", dataStyle); // 空单元格，图片将由后续方法插入
        
        // F8 - 模保（标签）
        createLabelCell(row8, 5, "模保", labelStyle);
        // G8, H8, I8 - 三个数据单元格
        createDataCell(row8, 6, formatParameterWithTolerance(detail.getMoldProtectionPressure()), dataStyle);
        createDataCell(row8, 7, formatParameterWithTolerance(detail.getMoldProtectionFlow()), dataStyle);
        createDataCell(row8, 8, formatParameterWithTolerance(detail.getMoldProtectionPosition()), dataStyle);
        
        // J8 - 开模3（标签）
        createLabelCell(row8, 9, "开模3", labelStyle);
        // K8, L8, M8 - 三个数据单元格
        createDataCell(row8, 10, formatParameterWithTolerance(detail.getOpenMold3Pressure()), dataStyle);
        createDataCell(row8, 11, formatParameterWithTolerance(detail.getOpenMold3Flow()), dataStyle);
        createDataCell(row8, 12, formatParameterWithTolerance(detail.getOpenMold3Position()), dataStyle);
        
        // O8 - 第四段（标签）
        createLabelCell(row8, 14, "第四段", labelStyle);
        // P8 - 第四段温度数据
        createDataCell(row8, 15, formatTemperatureWithTolerance(detail.getBarrelTemp4()), dataStyle);
        
        // Row 9
        Row row9 = sheet.createRow(8);
        // F9 - 高压（标签）
        createLabelCell(row9, 5, "高压", labelStyle);
        // G9, H9, I9 - 三个数据单元格
        createDataCell(row9, 6, formatParameterWithTolerance(detail.getHighPressurePressure()), dataStyle);
        createDataCell(row9, 7, formatParameterWithTolerance(detail.getHighPressureFlow()), dataStyle);
        createDataCell(row9, 8, formatParameterWithTolerance(detail.getHighPressurePosition()), dataStyle);
        
        // J9 - 开模4（标签）
        createLabelCell(row9, 9, "开模4", labelStyle);
        // K9, L9, M9 - 三个数据单元格
        createDataCell(row9, 10, formatParameterWithTolerance(detail.getOpenMold4Pressure()), dataStyle);
        createDataCell(row9, 11, formatParameterWithTolerance(detail.getOpenMold4Flow()), dataStyle);
        createDataCell(row9, 12, formatParameterWithTolerance(detail.getOpenMold4Position()), dataStyle);
        
        // N9:O9 - 模具温度℃（合并单元格，标签，居中）
        createLabelCell(row9, 13, "模具温度℃", labelCenterStyle);
        sheet.addMergedRegion(new CellRangeAddress(8, 8, 13, 14));
        // P9 - 模具温度数据
        createDataCell(row9, 15, formatTemperatureWithTolerance(detail.getMoldTemp()), dataStyle);
        
        // N10:N21 - 注射成型时间s（纵向排列，字符正常方向）
        Row row10 = sheet.createRow(9);
        Cell timeCell = row10.createCell(13);
        timeCell.setCellValue(toVerticalText("注射成型时间s"));
        timeCell.setCellStyle(verticalStyle);
        sheet.addMergedRegion(new CellRangeAddress(9, 20, 13, 13));
        
        // Row 10
        // F10 - 进芯一（标签）
        createLabelCell(row10, 5, "进芯一", labelStyle);
        // G10, H10, I10 - 三个数据单元格
        createDataCell(row10, 6, formatParameterWithTolerance(detail.getCorePull1InPressure()), dataStyle);
        createDataCell(row10, 7, formatParameterWithTolerance(detail.getCorePull1InFlow()), dataStyle);
        createDataCell(row10, 8, formatParameterWithTolerance(detail.getCorePull1InPosition()), dataStyle);
        
        // J10 - 抽芯一（标签）
        createLabelCell(row10, 9, "抽芯一", labelStyle);
        // K10, L10, M10 - 三个数据单元格
        createDataCell(row10, 10, formatParameterWithTolerance(detail.getCorePull1OutPressure()), dataStyle);
        createDataCell(row10, 11, formatParameterWithTolerance(detail.getCorePull1OutFlow()), dataStyle);
        createDataCell(row10, 12, formatParameterWithTolerance(detail.getCorePull1OutPosition()), dataStyle);
        
        // O10 - 合模（标签）
        createLabelCell(row10, 14, "合模", labelStyle);
        // P10 - 合模时间数据
        createDataCell(row10, 15, formatDecimal(detail.getClampingTime()), dataStyle);
        
        // Row 11
        Row row11 = sheet.createRow(10);
        // F11 - 进芯二（标签）
        createLabelCell(row11, 5, "进芯二", labelStyle);
        // G11, H11, I11 - 三个数据单元格
        createDataCell(row11, 6, formatParameterWithTolerance(detail.getCorePull2InPressure()), dataStyle);
        createDataCell(row11, 7, formatParameterWithTolerance(detail.getCorePull2InFlow()), dataStyle);
        createDataCell(row11, 8, formatParameterWithTolerance(detail.getCorePull2InPosition()), dataStyle);
        
        // J11 - 抽芯二（标签）
        createLabelCell(row11, 9, "抽芯二", labelStyle);
        // K11, L11, M11 - 三个数据单元格
        createDataCell(row11, 10, formatParameterWithTolerance(detail.getCorePull2OutPressure()), dataStyle);
        createDataCell(row11, 11, formatParameterWithTolerance(detail.getCorePull2OutFlow()), dataStyle);
        createDataCell(row11, 12, formatParameterWithTolerance(detail.getCorePull2OutPosition()), dataStyle);
        
        // O11 - 模保（标签）
        createLabelCell(row11, 14, "模保", labelStyle);
        // P11 - 模保时间数据
        createDataCell(row11, 15, formatDecimal(detail.getMoldProtectionTime()), dataStyle);
        
        // Row 12
        Row row12 = sheet.createRow(11);
        // F12 - 射胶一段（标签）
        createLabelCell(row12, 5, "射胶一段", labelStyle);
        // G12, H12, I12 - 三个数据单元格
        createDataCell(row12, 6, formatParameterWithTolerance(detail.getInjection1Pressure()), dataStyle);
        createDataCell(row12, 7, formatParameterWithTolerance(detail.getInjection1Flow()), dataStyle);
        createDataCell(row12, 8, formatParameterWithTolerance(detail.getInjection1Position()), dataStyle);
        
        // J12 - 熔胶1（标签）
        createLabelCell(row12, 9, "熔胶1", labelStyle);
        // K12, L12, M12 - 三个数据单元格
        createDataCell(row12, 10, formatParameterWithTolerance(detail.getMelt1Pressure()), dataStyle);
        createDataCell(row12, 11, formatParameterWithTolerance(detail.getMelt1Flow()), dataStyle);
        createDataCell(row12, 12, formatParameterWithTolerance(detail.getMelt1Position()), dataStyle);
        
        // O12 - 进芯1（标签）
        createLabelCell(row12, 14, "进芯1", labelStyle);
        // P12 - 进芯1时间数据
        createDataCell(row12, 15, formatDecimal(detail.getCorePull1InTime()), dataStyle);
        
        // Row 13
        Row row13 = sheet.createRow(12);
        // F13 - 射胶二段（标签）
        createLabelCell(row13, 5, "射胶二段", labelStyle);
        // G13, H13, I13 - 三个数据单元格
        createDataCell(row13, 6, formatParameterWithTolerance(detail.getInjection2Pressure()), dataStyle);
        createDataCell(row13, 7, formatParameterWithTolerance(detail.getInjection2Flow()), dataStyle);
        createDataCell(row13, 8, formatParameterWithTolerance(detail.getInjection2Position()), dataStyle);
        
        // J13 - 熔前松退（标签）
        createLabelCell(row13, 9, "熔前松退", labelStyle);
        // K13, L13, M13 - 三个数据单元格
        createDataCell(row13, 10, formatParameterWithTolerance(detail.getDecompressionBeforeMeltPressure()), dataStyle);
        createDataCell(row13, 11, formatParameterWithTolerance(detail.getDecompressionBeforeMeltFlow()), dataStyle);
        createDataCell(row13, 12, formatParameterWithTolerance(detail.getDecompressionBeforeMeltPosition()), dataStyle);
        
        // O13 - 进芯2（标签）
        createLabelCell(row13, 14, "进芯2", labelStyle);
        // P13 - 进芯2时间数据
        createDataCell(row13, 15, formatDecimal(detail.getCorePull2InTime()), dataStyle);
        
        // Row 14
        Row row14 = sheet.createRow(13);
        // F14 - 射胶三段（标签）
        createLabelCell(row14, 5, "射胶三段", labelStyle);
        // G14, H14, I14 - 三个数据单元格
        createDataCell(row14, 6, formatParameterWithTolerance(detail.getInjection3Pressure()), dataStyle);
        createDataCell(row14, 7, formatParameterWithTolerance(detail.getInjection3Flow()), dataStyle);
        createDataCell(row14, 8, formatParameterWithTolerance(detail.getInjection3Position()), dataStyle);
        
        // J14 - 熔后松退（标签）
        createLabelCell(row14, 9, "熔后松退", labelStyle);
        // K14, L14, M14 - 三个数据单元格
        createDataCell(row14, 10, formatParameterWithTolerance(detail.getDecompressionAfterMeltPressure()), dataStyle);
        createDataCell(row14, 11, formatParameterWithTolerance(detail.getDecompressionAfterMeltFlow()), dataStyle);
        createDataCell(row14, 12, formatParameterWithTolerance(detail.getDecompressionAfterMeltPosition()), dataStyle);
        
        // O14 - 注射（标签）
        createLabelCell(row14, 14, "注射", labelStyle);
        // P14 - 注射时间数据
        createDataCell(row14, 15, formatDecimal(detail.getInjectionTime()), dataStyle);
        
        // Row 15
        Row row15 = sheet.createRow(14);
        // F15 - 射胶四段（标签）
        createLabelCell(row15, 5, "射胶四段", labelStyle);
        // G15, H15, I15 - 三个数据单元格
        createDataCell(row15, 6, formatParameterWithTolerance(detail.getInjection4Pressure()), dataStyle);
        createDataCell(row15, 7, formatParameterWithTolerance(detail.getInjection4Flow()), dataStyle);
        createDataCell(row15, 8, formatParameterWithTolerance(detail.getInjection4Position()), dataStyle);
        
        // J15 - 顶出一速（标签）
        createLabelCell(row15, 9, "顶出一速", labelStyle);
        // K15, L15, M15 - 三个数据单元格
        createDataCell(row15, 10, formatParameterWithTolerance(detail.getEject1SpeedPressure()), dataStyle);
        createDataCell(row15, 11, formatParameterWithTolerance(detail.getEject1SpeedFlow()), dataStyle);
        createDataCell(row15, 12, formatParameterWithTolerance(detail.getEject1SpeedPosition()), dataStyle);
        
        // O15 - 保压（标签）
        createLabelCell(row15, 14, "保压", labelStyle);
        // P15 - 保压时间数据
        createDataCell(row15, 15, formatDecimal(detail.getHoldingTime()), dataStyle);
        
        // Row 16
        Row row16 = sheet.createRow(15);
        // F16 - 射胶五段（标签）
        createLabelCell(row16, 5, "射胶五段", labelStyle);
        // G16, H16, I16 - 三个数据单元格
        createDataCell(row16, 6, formatParameterWithTolerance(detail.getInjection5Pressure()), dataStyle);
        createDataCell(row16, 7, formatParameterWithTolerance(detail.getInjection5Flow()), dataStyle);
        createDataCell(row16, 8, formatParameterWithTolerance(detail.getInjection5Position()), dataStyle);
        
        // J16 - 顶出二速（标签）
        createLabelCell(row16, 9, "顶出二速", labelStyle);
        // K16, L16, M16 - 三个数据单元格
        createDataCell(row16, 10, formatParameterWithTolerance(detail.getEject2SpeedPressure()), dataStyle);
        createDataCell(row16, 11, formatParameterWithTolerance(detail.getEject2SpeedFlow()), dataStyle);
        createDataCell(row16, 12, formatParameterWithTolerance(detail.getEject2SpeedPosition()), dataStyle);
        
        // O16 - 冷却（标签）
        createLabelCell(row16, 14, "冷却", labelStyle);
        // P16 - 冷却时间数据
        createDataCell(row16, 15, formatDecimal(detail.getCoolingTime()), dataStyle);
        
        // Row 17
        Row row17 = sheet.createRow(16);
        // F17 - 射胶六段（标签）
        createLabelCell(row17, 5, "射胶六段", labelStyle);
        // G17, H17, I17 - 三个数据单元格
        createDataCell(row17, 6, formatParameterWithTolerance(detail.getInjection6Pressure()), dataStyle);
        createDataCell(row17, 7, formatParameterWithTolerance(detail.getInjection6Flow()), dataStyle);
        createDataCell(row17, 8, formatParameterWithTolerance(detail.getInjection6Position()), dataStyle);
        
        // J17 - 顶退一速（标签）
        createLabelCell(row17, 9, "顶退一速", labelStyle);
        // K17, L17, M17 - 三个数据单元格
        createDataCell(row17, 10, formatParameterWithTolerance(detail.getEjectRetract1SpeedPressure()), dataStyle);
        createDataCell(row17, 11, formatParameterWithTolerance(detail.getEjectRetract1SpeedFlow()), dataStyle);
        createDataCell(row17, 12, formatParameterWithTolerance(detail.getEjectRetract1SpeedPosition()), dataStyle);
        
        // O17 - 抽芯1（标签）
        createLabelCell(row17, 14, "抽芯1", labelStyle);
        // P17 - 抽芯1时间数据
        createDataCell(row17, 15, formatDecimal(detail.getCorePull1OutTime()), dataStyle);
        
        // Row 18
        Row row18 = sheet.createRow(17);
        // F18 - 保压一段（标签）
        createLabelCell(row18, 5, "保压一段", labelStyle);
        // G18, H18, I18 - 三个数据单元格
        createDataCell(row18, 6, formatParameterWithTolerance(detail.getHolding1Pressure()), dataStyle);
        createDataCell(row18, 7, formatParameterWithTolerance(detail.getHolding1Flow()), dataStyle);
        createDataCell(row18, 8, formatParameterWithTolerance(detail.getHolding1Position()), dataStyle);
        
        // J18 - 顶退二速（标签）
        createLabelCell(row18, 9, "顶退二速", labelStyle);
        // K18, L18, M18 - 三个数据单元格
        createDataCell(row18, 10, formatParameterWithTolerance(detail.getEjectRetract2SpeedPressure()), dataStyle);
        createDataCell(row18, 11, formatParameterWithTolerance(detail.getEjectRetract2SpeedFlow()), dataStyle);
        createDataCell(row18, 12, formatParameterWithTolerance(detail.getEjectRetract2SpeedPosition()), dataStyle);
        
        // O18 - 抽芯2（标签）
        createLabelCell(row18, 14, "抽芯2", labelStyle);
        // P18 - 抽芯2时间数据
        createDataCell(row18, 15, formatDecimal(detail.getCorePull2OutTime()), dataStyle);
        
        // Row 19
        Row row19 = sheet.createRow(18);
        // F19 - 保压二段（标签）
        createLabelCell(row19, 5, "保压二段", labelStyle);
        // G19, H19, I19 - 三个数据单元格
        createDataCell(row19, 6, formatParameterWithTolerance(detail.getHolding2Pressure()), dataStyle);
        createDataCell(row19, 7, formatParameterWithTolerance(detail.getHolding2Flow()), dataStyle);
        createDataCell(row19, 8, formatParameterWithTolerance(detail.getHolding2Position()), dataStyle);
        
        // J19, K19, L19, M19 - 空白单元格
        
        // O19 - 开模（标签）
        createLabelCell(row19, 14, "开模", labelStyle);
        // P19 - 开模时间数据
        createDataCell(row19, 15, formatDecimal(detail.getMoldOpeningTime()), dataStyle);
        
        // Row 20
        Row row20 = sheet.createRow(19);
        // F20 - 保压三段（标签）
        createLabelCell(row20, 5, "保压三段", labelStyle);
        // G20, H20, I20 - 三个数据单元格
        createDataCell(row20, 6, formatParameterWithTolerance(detail.getHolding3Pressure()), dataStyle);
        createDataCell(row20, 7, formatParameterWithTolerance(detail.getHolding3Flow()), dataStyle);
        createDataCell(row20, 8, formatParameterWithTolerance(detail.getHolding3Position()), dataStyle);
        
        // J20, K20, L20, M20 - 空白单元格
        
        // O20 - 取件时间（标签）
        createLabelCell(row20, 14, "取件时间", labelStyle);
        // P20 - 取件时间数据
        createDataCell(row20, 15, formatDecimal(detail.getPartEjectionTime()), dataStyle);
        
        // Row 21
        Row row21 = sheet.createRow(20);
        // F21 - 注射模式（标签）
        createLabelCell(row21, 5, "注射模式", labelStyle);
        // G21 - 空白单元格，数据填充到G21（后一个单元格）
        createDataCell(row21, 6, detail.getInjectionMode(), dataStyle);
        
        // H21 - 进芯方式（标签）
        createLabelCell(row21, 7, "进芯方式", labelStyle);
        // I21 - 空白单元格，数据填充到I21（后一个单元格）
        createDataCell(row21, 8, detail.getCorePullInMethod(), dataStyle);
        
        // J21 - 抽芯方式（标签，文档中写的是J22，但应该是J21）
        createLabelCell(row21, 9, "抽芯方式", labelStyle);
        // K21 - 空白单元格，数据填充到K21（后一个单元格）
        createDataCell(row21, 10, detail.getCorePullOutMethod(), dataStyle);
        
        // L21 - 座台方式（标签，文档中写的是L12，但应该是L21）
        createLabelCell(row21, 11, "座台方式", labelStyle);
        // M21 - 空白单元格，数据填充到M21（后一个单元格）
        createDataCell(row21, 12, detail.getNozzleContactMethod(), dataStyle);
        
        // O21 - 总时间（标签）
        createLabelCell(row21, 14, "总时间", labelStyle);
        // P21 - 总时间数据
        createDataCell(row21, 15, formatDecimal(detail.getTotalTime()), dataStyle);
    }
    
    /**
     * 创建原料干燥处理和零件后处理行（Row 22-27）
     * 严格按照拆分Excel-plus1.md的布局
     */
    private static void createDryingAndProcessRows(Sheet sheet, ProcessFileDetail detail,
                                                   CellStyle labelStyle, CellStyle dataStyle, CellStyle verticalStyle, CellStyle labelCenterStyle, CellStyle dataLeftStyle) {
        // Row 22
        Row row22 = sheet.createRow(21);
        // A22:A27 - 原料干燥处理（纵向排列，字符正常方向）
        Cell dryingCell = row22.createCell(0);
        dryingCell.setCellValue(toVerticalText("原料干燥处理"));
        dryingCell.setCellStyle(verticalStyle);
        sheet.addMergedRegion(new CellRangeAddress(21, 26, 0, 0));
        
        // B22 - 使用设备（标签）
        createLabelCell(row22, 1, "使用设备", labelStyle);
        // C22 - 空白单元格，数据填充到C22（后一个单元格）
        createDataCell(row22, 2, detail.getDryingEquipment(), dataStyle);
        
        // D22 - 零件后处理（标签）
        createLabelCell(row22, 3, "零件后处理", labelStyle);
        // E22 - 空白单元格，数据填充到E22（后一个单元格）
        createDataCell(row22, 4, detail.getPartPostTreatment(), dataStyle);
        
        // F22 - 顶针模式（标签）
        createLabelCell(row22, 5, "顶针模式", labelStyle);
        // G22 - 空白单元格，数据填充到G22（后一个单元格）
        createDataCell(row22, 6, detail.getEjectionMode(), dataStyle);
        
        // H22 - 顶针次数（标签）
        createLabelCell(row22, 7, "顶针次数", labelStyle);
        // I22 - 空白单元格，数据填充到I22（后一个单元格）
        createDataCell(row22, 8, detail.getEjectionCount() != null ? detail.getEjectionCount().toString() : "", dataStyle);
        
        // J22 - 螺杆转速（标签，文档中写的是G22，但G22已被顶针模式占用，应该是J22）
        createLabelCell(row22, 9, "螺杆转速", labelStyle);
        // K22 - 空白单元格，数据填充到K22（后一个单元格）
        createDataCell(row22, 10, formatDecimal(detail.getScrewSpeed()), dataStyle);
        
        // L22:M22 - 抽芯行程方式（标签）
        createLabelCell(row22, 11, "抽芯行程方式", labelStyle);
        sheet.addMergedRegion(new CellRangeAddress(21, 21, 11, 12));
        // N22:P22 - 抽芯行程方式数据（所在行后一个合并单元格）
        createDataCell(row22, 13, detail.getCorePullStrokeMethod(), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(21, 21, 13, 15));
        
        // Row 23
        Row row23 = sheet.createRow(22);
        // B23 - 盛料高度（标签）
        createLabelCell(row23, 1, "盛料高度", labelStyle);
        // C23 - 空白单元格，数据填充到C23（后一个单元格）
        createDataCell(row23, 2, formatDecimal(detail.getMaterialFillHeight()), dataStyle);
        
        // D23 - 产品后处理（标签）
        createLabelCell(row23, 3, "产品后处理", labelStyle);
        // E23 - 空白单元格，数据填充到E23（后一个单元格）
        createDataCell(row23, 4, detail.getProductPostTreatment(), dataStyle);
        
        // F23 - 工序号（标签）
        createLabelCell(row23, 5, "工序号", labelStyle);
        // G23:K23 - 工序内容（标签，居中）
        createLabelCell(row23, 6, "工序内容", labelCenterStyle);
        sheet.addMergedRegion(new CellRangeAddress(22, 22, 6, 10));
        // L23:P23 - 品质检查（标签，居中）
        createLabelCell(row23, 11, "品质检查", labelCenterStyle);
        sheet.addMergedRegion(new CellRangeAddress(22, 22, 11, 15));
        
        // Row 24
        Row row24 = sheet.createRow(23);
        // B24:B25 - 翻料时间（合并单元格）
        createLabelCell(row24, 1, "翻料时间", labelStyle);
        sheet.addMergedRegion(new CellRangeAddress(23, 24, 1, 1));
        // C24:C25 - 翻料时间数据（合并单元格），带"h"单位
        createDataCell(row24, 2, formatHour(detail.getMaterialTurningTime()), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(23, 24, 2, 2));
        
        // D24 - 加热温度（标签）
        createLabelCell(row24, 3, "加热温度", labelStyle);
        // E24 - 空白单元格，数据填充到E24（后一个单元格）
        createDataCell(row24, 4, formatCelsius(detail.getHeatingTemp()), dataStyle);
        
        // F24 - 1（标签）
        createLabelCell(row24, 5, "1", labelStyle);
        // G24:K24 - 工序内容1（左对齐）
        String[] processContent = getProcessContent(detail);
        createDataCell(row24, 6, "1." + processContent[0], dataLeftStyle);
        sheet.addMergedRegion(new CellRangeAddress(23, 23, 6, 10));
        // L24:P24 - 品质检查1（左对齐）
        String[] qualityInspection = getQualityInspection(detail);
        createDataCell(row24, 11, "1." + qualityInspection[0], dataLeftStyle);
        sheet.addMergedRegion(new CellRangeAddress(23, 23, 11, 15));
        
        // Row 25
        Row row25 = sheet.createRow(24);
        // D25 - 保温温度（标签）
        createLabelCell(row25, 3, "保温温度", labelStyle);
        // E25 - 空白单元格，数据填充到E25（后一个单元格）
        createDataCell(row25, 4, formatCelsius(detail.getHoldingTemp()), dataStyle);
        
        // F25 - 2（标签）
        createLabelCell(row25, 5, "2", labelStyle);
        // G25:K25 - 工序内容2（左对齐）
        createDataCell(row25, 6, "2." + processContent[1], dataLeftStyle);
        sheet.addMergedRegion(new CellRangeAddress(24, 24, 6, 10));
        // L25:P25 - 品质检查2（左对齐）
        createDataCell(row25, 11, "2." + qualityInspection[1], dataLeftStyle);
        sheet.addMergedRegion(new CellRangeAddress(24, 24, 11, 15));
        
        // Row 26
        Row row26 = sheet.createRow(25);
        // B26 - 干燥温度（标签）
        createLabelCell(row26, 1, "干燥温度", labelStyle);
        // C26 - 空白单元格，数据填充到C26（后一个单元格）
        createDataCell(row26, 2, formatCelsius(detail.getDryingTemp()), dataStyle);
        
        // D26 - 干燥时间（标签）
        createLabelCell(row26, 3, "干燥时间", labelStyle);
        // E26 - 空白单元格，数据填充到E26（后一个单元格），带"h"单位
        createDataCell(row26, 4, formatHour(detail.getDryingTime()), dataStyle);
        
        // F26 - 3（标签）
        createLabelCell(row26, 5, "3", labelStyle);
        // G26:K26 - 工序内容3（左对齐）
        createDataCell(row26, 6, "3." + processContent[2], dataLeftStyle);
        sheet.addMergedRegion(new CellRangeAddress(25, 25, 6, 10));
        // L26:P26 - 品质检查3（左对齐）
        createDataCell(row26, 11, "3." + qualityInspection[2], dataLeftStyle);
        sheet.addMergedRegion(new CellRangeAddress(25, 25, 11, 15));
        
        // Row 27
        Row row27 = sheet.createRow(26);
        // B27 - 前模冷却（标签）
        createLabelCell(row27, 1, "前模冷却", labelStyle);
        // C27 - 空白单元格，数据填充到C27（后一个单元格）
        createDataCell(row27, 2, detail.getFrontMoldCooling(), dataStyle);
        
        // D27 - 后模冷却（标签）
        createLabelCell(row27, 3, "后模冷却", labelStyle);
        // E27 - 空白单元格，数据填充到E27（后一个单元格）
        createDataCell(row27, 4, detail.getRearMoldCooling(), dataStyle);
        
        // F27 - 4（标签）
        createLabelCell(row27, 5, "4", labelStyle);
        // G27:K27 - 工序内容4（左对齐）
        createDataCell(row27, 6, "4." + processContent[3], dataLeftStyle);
        sheet.addMergedRegion(new CellRangeAddress(26, 26, 6, 10));
        // L27:P27 - 品质检查4（左对齐）
        createDataCell(row27, 11, "4." + qualityInspection[3], dataLeftStyle);
        sheet.addMergedRegion(new CellRangeAddress(26, 26, 11, 15));
    }
    
    /**
     * 创建签名和文件编号行（Row 28-30）
     * 严格按照拆分Excel-plus1.md的布局
     */
    private static void createSignatureAndFileNoRows(Sheet sheet, ProcessFile processFile, Equipment equipment,
                                                     CellStyle labelStyle, CellStyle dataStyle, CellStyle labelCenterStyle, CellStyle dataLeftStyle) {
        // Row 28
        Row row28 = sheet.createRow(27);
        // A28:G29 - 模具及注塑工艺综合评估（合并单元格，左对齐）
        createLabelCell(row28, 0, "模具及注塑工艺综合评估:可以量产。", dataLeftStyle);
        sheet.addMergedRegion(new CellRangeAddress(27, 28, 0, 6));
        
        // H28:I28 - 编制人（标签，居中）
        createLabelCell(row28, 7, "编制人", labelCenterStyle);
        sheet.addMergedRegion(new CellRangeAddress(27, 27, 7, 8));
        // 数据填充到H29:I29（Row 29，索引28，对应列的下一个单元格）
        Row row29 = sheet.createRow(28);
        createDataCell(row29, 7, "", dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(28, 28, 7, 8));
        
        // J28:K28 - 审核人（标签，居中）
        createLabelCell(row28, 9, "审核人", labelCenterStyle);
        sheet.addMergedRegion(new CellRangeAddress(27, 27, 9, 10));
        // 数据填充到J29:K29
        createDataCell(row29, 9, "", dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(28, 28, 9, 10));
        
        // L28:M28 - 批准人（标签，居中）
        createLabelCell(row28, 11, "批准人", labelCenterStyle);
        sheet.addMergedRegion(new CellRangeAddress(27, 27, 11, 12));
        // 数据填充到L29:M29
        createDataCell(row29, 11, "", dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(28, 28, 11, 12));
        
        // N28:P28 - 会签（标签，居中）
        createLabelCell(row28, 13, "会签", labelCenterStyle);
        sheet.addMergedRegion(new CellRangeAddress(27, 27, 13, 15));
        // 数据填充到N29:P29
        createDataCell(row29, 13, "", dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(28, 28, 13, 15));
        
        // Row 30
        Row row30 = sheet.createRow(29);
        // A30:H30 - 工艺参数文件编号（标签和数据组合显示）
        Cell fileNoCell = row30.createCell(0);
        fileNoCell.setCellValue("工艺参数文件编号：");
        fileNoCell.setCellStyle(labelStyle);
        sheet.addMergedRegion(new CellRangeAddress(29, 29, 0, 7));
        // 数据填充到A30:H30（当前单元格）
        if (processFile.getFileNo() != null && !processFile.getFileNo().isEmpty()) {
            fileNoCell.setCellValue("工艺参数文件编号：" + processFile.getFileNo());
        }
        
        // I30:P30 - 机械手参数型号（标签+数据，直接显示在同一单元格内）
        Cell robotCell = row30.createCell(8);
        String robotModel = (equipment != null && equipment.getRobotModel() != null) ? equipment.getRobotModel() : "";
        robotCell.setCellValue("机械手参数型号：" + robotModel);
        robotCell.setCellStyle(labelStyle);
        sheet.addMergedRegion(new CellRangeAddress(29, 29, 8, 15));
    }
    
    /**
     * 获取工序内容
     */
    private static String[] getProcessContent(ProcessFileDetail detail) {
        String processContent = detail.getProcessContent();
        if (processContent != null && !processContent.isEmpty()) {
            String[] lines = processContent.split("\n");
            String[] result = new String[4];
            for (int i = 0; i < 4; i++) {
                result[i] = i < lines.length ? lines[i].trim() : "";
            }
            return result;
        }
        return new String[]{
            "按照要求将模具码调好并调整。",
            "按照要求配料(配色)并将其按标准将原料烘干。",
            "按照注塑工艺要求将加工工艺调整。",
            "检查注塑设备及模具运行的安全性。"
        };
    }
    
    /**
     * 获取品质检查内容
     */
    private static String[] getQualityInspection(ProcessFileDetail detail) {
        String qualityInspection = detail.getQualityInspection();
        if (qualityInspection != null && !qualityInspection.isEmpty()) {
            String[] lines = qualityInspection.split("\n");
            String[] result = new String[4];
            for (int i = 0; i < 4; i++) {
                result[i] = i < lines.length ? lines[i].trim() : "";
            }
            return result;
        }
        return new String[]{
            "产品的尺寸光洁度符合图样要求,尺寸稳定。",
            "形状完整,表面光滑,结合部位平滑。",
            "产品无明显收缩及明显溶解线。",
            "产品外观颜色一致,无色差,杂质,油垢等。"
        };
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 为指定范围内的所有单元格添加细边框。
     * 不覆盖现有样式的其他属性（字体、对齐等），只在原样式基础上补充边框。
     */
    private static void applyBorderToAllCells(Sheet sheet, Workbook workbook, int lastRowIndex, int lastColIndex) {
        Map<CellStyle, CellStyle> styleCache = new HashMap<>();
        
        for (int r = 0; r <= lastRowIndex; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                row = sheet.createRow(r);
            }
            for (int c = 0; c <= lastColIndex; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) {
                    cell = row.createCell(c);
                    // 对于新建的空单元格，创建一个只包含边框且垂直居中的基础样式
                    CellStyle baseBorderStyle = workbook.createCellStyle();
                    baseBorderStyle.setBorderBottom(BorderStyle.THIN);
                    baseBorderStyle.setBorderTop(BorderStyle.THIN);
                    baseBorderStyle.setBorderLeft(BorderStyle.THIN);
                    baseBorderStyle.setBorderRight(BorderStyle.THIN);
                    baseBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    cell.setCellStyle(baseBorderStyle);
                } else {
                    CellStyle originalStyle = cell.getCellStyle();
                    if (originalStyle == null) {
                        CellStyle baseBorderStyle = workbook.createCellStyle();
                        baseBorderStyle.setBorderBottom(BorderStyle.THIN);
                        baseBorderStyle.setBorderTop(BorderStyle.THIN);
                        baseBorderStyle.setBorderLeft(BorderStyle.THIN);
                        baseBorderStyle.setBorderRight(BorderStyle.THIN);
                        baseBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                        cell.setCellStyle(baseBorderStyle);
                    } else {
                        CellStyle borderedStyle = styleCache.get(originalStyle);
                        if (borderedStyle == null) {
                            borderedStyle = workbook.createCellStyle();
                            borderedStyle.cloneStyleFrom(originalStyle);
                            borderedStyle.setBorderBottom(BorderStyle.THIN);
                            borderedStyle.setBorderTop(BorderStyle.THIN);
                            borderedStyle.setBorderLeft(BorderStyle.THIN);
                            borderedStyle.setBorderRight(BorderStyle.THIN);
                            styleCache.put(originalStyle, borderedStyle);
                        }
                        cell.setCellStyle(borderedStyle);
                    }
                }
            }
        }
    }
    
    private static void createLabelCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
    
    private static void createDataCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
    
    private static void createTitleCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
    
    private static String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString();
    }
    
    /**
     * 格式化重量值，添加"g"单位（用于零件净重、零件毛重、消耗定额等）
     */
    private static String formatWeight(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString() + "g";
    }
    
    /**
     * 格式化时间值，添加"h"单位（用于翻料时间、干燥时间等，单位为小时）
     */
    private static String formatHour(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString() + "h";
    }
    
    /**
     * 格式化温度值，添加"℃"单位（用于干燥温度、加热温度、保温温度等）
     */
    private static String formatCelsius(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString() + "℃";
    }
    
    /**
     * 格式化参数值，添加±10后缀（用于合模、进芯、射胶、保压、开模、抽芯、熔胶、顶出等参数）
     */
    private static String formatParameterWithTolerance(BigDecimal value) {
        if (value == null) {
            return "";
        }
        String formatted = value.stripTrailingZeros().toPlainString();
        return formatted + "±10";
    }
    
    /**
     * 格式化温度参数，带±5公差
     */
    private static String formatTemperatureWithTolerance(BigDecimal value) {
        if (value == null) {
            return "";
        }
        String formatted = value.stripTrailingZeros().toPlainString();
        return formatted + "±5";
    }
    
    // 创建样式
    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private static CellStyle createLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 创建居中标签样式（用于需要居中显示的标签单元格）
     */
    private static CellStyle createLabelCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 创建居中数据样式（用于需要居中显示的数据单元格）
     */
    private static CellStyle createDataCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 创建左对齐数据样式（用于工序内容和品质检查等需要左对齐的数据单元格）
     */
    private static CellStyle createDataLeftStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private static CellStyle createCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 创建“纵向排列”样式：不旋转文字，通过换行符将字符竖向排列，并开启自动换行
     */
    private static CellStyle createVerticalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true); // 允许换行以实现竖排
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 将字符串转换为竖向排列的文本（每个字符之间加入换行符）
     */
    private static String toVerticalText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            sb.append(chars[i]);
            if (i < chars.length - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
    
    /**
     * 在Excel中插入签名图片
     */
    public static void insertSignatureToGeneratedExcel(String excelFilePath, String signatureImagePath, String signatureType) {
        ProcessFileExcelUtil.insertSignatureToExcel(excelFilePath, signatureImagePath, signatureType);
    }
}
