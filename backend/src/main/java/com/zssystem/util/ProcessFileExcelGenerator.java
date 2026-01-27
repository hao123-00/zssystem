package com.zssystem.util;

import com.zssystem.entity.ProcessFile;
import com.zssystem.entity.ProcessFileDetail;
import com.zssystem.entity.Equipment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 工艺文件Excel生成工具类
 * 按照"塑料零件注塑卡片"格式生成Excel文件
 */
public class ProcessFileExcelGenerator {
    
    /**
     * 生成工艺文件Excel
     * 
     * @param processFile 工艺文件主表
     * @param processFileDetail 工艺文件详细内容
     * @param equipment 设备信息
     * @param savePath 保存路径
     * @return Excel文件路径
     */
    public static String generateProcessFileExcel(
            ProcessFile processFile,
            ProcessFileDetail processFileDetail,
            Equipment equipment,
            String savePath) throws IOException {
        
        // 创建Workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("注塑工艺卡片");
        
        // 设置列宽
        setColumnWidths(sheet);
        
        // 创建样式
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle labelStyle = createLabelStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        CellStyle borderStyle = createBorderStyle(workbook);
        
        int currentRow = 0;
        
        // 1. 表头（第1行）
        Row headerRow = sheet.createRow(currentRow++);
        createHeaderRow(headerRow, sheet, processFile, titleStyle, headerStyle);
        
        // 2. 基本信息行（第2行）
        Row infoRow = sheet.createRow(currentRow++);
        createInfoRow(infoRow, processFileDetail, labelStyle, dataStyle);
        
        // 3. 材料信息行（第3-4行）
        Row materialRow1 = sheet.createRow(currentRow++);
        Row materialRow2 = sheet.createRow(currentRow++);
        createMaterialRows(materialRow1, materialRow2, processFileDetail, labelStyle, dataStyle);
        
        // 4. 模具信息行（第5-6行）
        Row moldRow1 = sheet.createRow(currentRow++);
        Row moldRow2 = sheet.createRow(currentRow++);
        int nextRow = createMoldRows(moldRow1, moldRow2, processFileDetail, equipment, labelStyle, dataStyle, sheet, currentRow);
        currentRow = nextRow;
        
        // 5. 注塑成型工艺参数（第7-27行）
        currentRow = createProcessParameters(sheet, currentRow, processFileDetail, labelStyle, dataStyle, centerStyle);
        
        // 6. 温度和时间参数（第7-26行，右侧）
        createTemperatureAndTimeParams(sheet, processFileDetail, labelStyle, dataStyle);
        
        // 7. 特殊模式参数（第28-29行）
        currentRow = createSpecialModeParams(sheet, currentRow, processFileDetail, labelStyle, dataStyle);
        
        // 8. 原材料干燥处理和零件后处理（第30-33行）
        currentRow = createDryingAndPostTreatment(sheet, currentRow, processFileDetail, labelStyle, dataStyle);
        
        // 9. 工序内容和品质检查（第34-37行）
        currentRow = createProcessContentAndQuality(sheet, currentRow, processFileDetail, labelStyle, dataStyle);
        
        // 10. 综合评估（第38行）
        Row assessmentRow = sheet.createRow(currentRow++);
        createAssessmentRow(assessmentRow, processFileDetail, labelStyle, dataStyle, sheet);
        
        // 11. 审批签名区域（第39-42行）
        createSignatureArea(sheet, currentRow, labelStyle, sheet);
        
        // 保存Excel文件（按年月分类存储）
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
     * 设置列宽
     */
    private static void setColumnWidths(XSSFSheet sheet) {
        // 根据实际需要设置列宽（单位：1/256个字符宽度）
        sheet.setColumnWidth(0, 12 * 256);  // A列
        sheet.setColumnWidth(1, 12 * 256);  // B列
        sheet.setColumnWidth(2, 12 * 256);  // C列
        sheet.setColumnWidth(3, 12 * 256);  // D列
        sheet.setColumnWidth(4, 12 * 256);  // E列
        sheet.setColumnWidth(5, 12 * 256);  // F列
        sheet.setColumnWidth(6, 12 * 256);  // G列
        sheet.setColumnWidth(7, 12 * 256);  // H列
        sheet.setColumnWidth(8, 12 * 256);  // I列
        sheet.setColumnWidth(9, 12 * 256);  // J列
        sheet.setColumnWidth(10, 12 * 256); // K列
        sheet.setColumnWidth(11, 12 * 256); // L列
        sheet.setColumnWidth(12, 12 * 256); // M列
        sheet.setColumnWidth(13, 12 * 256); // N列
        sheet.setColumnWidth(14, 12 * 256); // O列
    }
    
    /**
     * 创建表头行
     */
    private static void createHeaderRow(Row row, Sheet sheet, ProcessFile processFile, 
                                       CellStyle titleStyle, CellStyle headerStyle) {
        // 左侧：公司名称
        Cell cell1 = row.createCell(0);
        cell1.setCellValue("芜湖飞科电器有限公司");
        cell1.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
        
        // 中间：标题
        Cell cell2 = row.createCell(3);
        cell2.setCellValue("塑料零件注塑卡片");
        cell2.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 8));
        
        // 右侧：基本信息（产品型号、产品名称等）
        int col = 9;
        createHeaderCell(row, col++, "产品型号", headerStyle);
        createHeaderCell(row, col++, "产品名称", headerStyle);
        createHeaderCell(row, col++, "模具制造公司", headerStyle);
        createHeaderCell(row, col++, "零件名称", headerStyle);
        createHeaderCell(row, col++, "项目负责人", headerStyle);
    }
    
    /**
     * 创建基本信息行
     */
    private static void createInfoRow(Row row, ProcessFileDetail detail, 
                                      CellStyle labelStyle, CellStyle dataStyle) {
        int col = 9;
        createDataCell(row, col++, detail.getProductModel(), dataStyle);
        createDataCell(row, col++, detail.getProductName(), dataStyle);
        createDataCell(row, col++, detail.getMoldManufacturingCompany(), dataStyle);
        createDataCell(row, col++, detail.getPartName(), dataStyle);
        createDataCell(row, col++, detail.getProjectLeader(), dataStyle);
    }
    
    /**
     * 创建材料信息行
     */
    private static void createMaterialRows(Row row1, Row row2, ProcessFileDetail detail,
                                          CellStyle labelStyle, CellStyle dataStyle) {
        // 第一行：标签
        int col = 0;
        createLabelCell(row1, col++, "材料名称", labelStyle);
        createLabelCell(row1, col++, "材料牌号", labelStyle);
        createLabelCell(row1, col++, "材料颜色", labelStyle);
        createLabelCell(row1, col++, "颜料名称", labelStyle);
        createLabelCell(row1, col++, "比例: %", labelStyle);
        createLabelCell(row1, col++, "零件净重 g", labelStyle);
        createLabelCell(row1, col++, "零件毛重 g", labelStyle);
        createLabelCell(row1, col++, "消耗定额 g", labelStyle);
        
        // 第二行：数据
        col = 0;
        createDataCell(row2, col++, detail.getMaterialName(), dataStyle);
        createDataCell(row2, col++, detail.getMaterialGrade(), dataStyle);
        createDataCell(row2, col++, detail.getMaterialColor(), dataStyle);
        createDataCell(row2, col++, detail.getPigmentName(), dataStyle);
        createDataCell(row2, col++, formatDecimal(detail.getPigmentRatio()), dataStyle);
        createDataCell(row2, col++, formatDecimal(detail.getPartNetWeight()) + " g", dataStyle);
        createDataCell(row2, col++, formatDecimal(detail.getPartGrossWeight()) + " g", dataStyle);
        createDataCell(row2, col++, formatDecimal(detail.getConsumptionQuota()) + " g", dataStyle);
    }
    
    /**
     * 创建模具信息行
     */
    private static int createMoldRows(Row row1, Row row2, ProcessFileDetail detail, Equipment equipment,
                                      CellStyle labelStyle, CellStyle dataStyle, Sheet sheet, int startRow) {
        // 第一行：标签
        int col = 0;
        createLabelCell(row1, col++, "模具编号", labelStyle);
        createLabelCell(row1, col++, "型腔数量", labelStyle);
        createLabelCell(row1, col++, "锁模力 %Mpa", labelStyle);
        createLabelCell(row1, col++, "使用设备名称/规格", labelStyle);
        
        // 第二行：数据
        col = 0;
        createDataCell(row2, col++, detail.getMoldNumber(), dataStyle);
        createDataCell(row2, col++, detail.getCavityQuantity() != null ? detail.getCavityQuantity().toString() : "", dataStyle);
        createDataCell(row2, col++, formatDecimal(detail.getClampingForce()) + " Mpa", dataStyle);
        
        // 设备信息
        String equipmentInfo = equipment != null 
            ? (equipment.getMachineNo() != null 
                ? equipment.getMachineNo() + " - " + equipment.getEquipmentName()
                : equipment.getEquipmentNo() + " - " + equipment.getEquipmentName())
            : (detail.getEquipmentName() != null ? detail.getEquipmentName() : "");
        createDataCell(row2, col++, equipmentInfo, dataStyle);
        
        // 产品关键尺寸（合并单元格）- 只合并到下一行，不跨太多行
        int keyDimCol = col;
        Cell keyDimLabel = row1.createCell(keyDimCol);
        keyDimLabel.setCellValue("产品关键尺寸");
        keyDimLabel.setCellStyle(labelStyle);
        
        Cell keyDimData = row2.createCell(keyDimCol);
        keyDimData.setCellValue(detail.getProductKeyDimensions() != null ? detail.getProductKeyDimensions() : "");
        keyDimData.setCellStyle(dataStyle);
        
        // 合并单元格（只合并当前两行，避免与后续区域重叠）
        // 第一行：标签行，合并到右侧
        sheet.addMergedRegion(new CellRangeAddress(row1.getRowNum(), row1.getRowNum(), keyDimCol, keyDimCol + 5));
        // 第二行：数据行，合并到右侧
        sheet.addMergedRegion(new CellRangeAddress(row2.getRowNum(), row2.getRowNum(), keyDimCol, keyDimCol + 5));
        
        // 返回下一行索引
        return startRow + 2;
    }
    
    /**
     * 创建注塑成型工艺参数
     */
    private static int createProcessParameters(Sheet sheet, int startRow, ProcessFileDetail detail,
                                              CellStyle labelStyle, CellStyle dataStyle, CellStyle centerStyle) {
        int currentRow = startRow;
        
        // 创建表头
        Row headerRow = sheet.createRow(currentRow++);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("注塑成型工艺");
        headerCell.setCellStyle(labelStyle);
        sheet.addMergedRegion(new CellRangeAddress(headerRow.getRowNum(), headerRow.getRowNum(), 0, 14));
        
        // 创建列标题行
        Row titleRow = sheet.createRow(currentRow++);
        createTitleCell(titleRow, 0, "动作", centerStyle);
        createTitleCell(titleRow, 1, "压力/bar", centerStyle);
        createTitleCell(titleRow, 2, "流量/%", centerStyle);
        createTitleCell(titleRow, 3, "位置/mm", centerStyle);
        createTitleCell(titleRow, 4, "动作", centerStyle);
        createTitleCell(titleRow, 5, "压力/bar", centerStyle);
        createTitleCell(titleRow, 6, "流量/%", centerStyle);
        createTitleCell(titleRow, 7, "位置/mm", centerStyle);
        
        // 合模参数
        currentRow = createParameterRow(sheet, currentRow, "合模1", 
            detail.getClamp1Pressure(), detail.getClamp1Flow(), detail.getClamp1Position(),
            "合模2", detail.getClamp2Pressure(), detail.getClamp2Flow(), detail.getClamp2Position(),
            labelStyle, dataStyle);
        
        currentRow = createParameterRow(sheet, currentRow, "模保",
            detail.getMoldProtectionPressure(), detail.getMoldProtectionFlow(), detail.getMoldProtectionPosition(),
            "高压", detail.getHighPressurePressure(), detail.getHighPressureFlow(), detail.getHighPressurePosition(),
            labelStyle, dataStyle);
        
        // 进芯参数
        currentRow = createParameterRow(sheet, currentRow, "进芯一",
            detail.getCorePull1InPressure(), detail.getCorePull1InFlow(), detail.getCorePull1InPosition(),
            "进芯二", detail.getCorePull2InPressure(), detail.getCorePull2InFlow(), detail.getCorePull2InPosition(),
            labelStyle, dataStyle);
        
        // 射胶参数（6段）
        for (int i = 1; i <= 6; i++) {
            BigDecimal pressure = getInjectionPressure(detail, i);
            BigDecimal flow = getInjectionFlow(detail, i);
            BigDecimal position = getInjectionPosition(detail, i);
            
            String leftLabel = "射胶" + (i == 1 ? "一段" : i == 2 ? "二段" : i == 3 ? "三段" : i == 4 ? "四段" : i == 5 ? "五段" : "六段");
            
            // 每两段一行
            if (i % 2 == 1) {
                BigDecimal pressure2 = getInjectionPressure(detail, i + 1);
                BigDecimal flow2 = getInjectionFlow(detail, i + 1);
                BigDecimal position2 = getInjectionPosition(detail, i + 1);
                String rightLabel = "射胶" + (i + 1 == 2 ? "二段" : i + 1 == 3 ? "三段" : i + 1 == 4 ? "四段" : i + 1 == 5 ? "五段" : "六段");
                
                currentRow = createParameterRow(sheet, currentRow, leftLabel, pressure, flow, position,
                    rightLabel, pressure2, flow2, position2, labelStyle, dataStyle);
            }
        }
        
        // 保压参数（3段）
        for (int i = 1; i <= 3; i++) {
            BigDecimal pressure = getHoldingPressure(detail, i);
            BigDecimal flow = getHoldingFlow(detail, i);
            BigDecimal position = getHoldingPosition(detail, i);
            
            String leftLabel = "保压" + (i == 1 ? "一段" : i == 2 ? "二段" : "三段");
            
            // 每两段一行，第三段单独一行
            if (i == 1) {
                BigDecimal pressure2 = getHoldingPressure(detail, 2);
                BigDecimal flow2 = getHoldingFlow(detail, 2);
                BigDecimal position2 = getHoldingPosition(detail, 2);
                currentRow = createParameterRow(sheet, currentRow, leftLabel, pressure, flow, position,
                    "保压二段", pressure2, flow2, position2, labelStyle, dataStyle);
            } else if (i == 3) {
                currentRow = createParameterRow(sheet, currentRow, "保压三段", pressure, flow, position,
                    "", null, null, null, labelStyle, dataStyle);
            }
        }
        
        // 开模参数
        for (int i = 1; i <= 4; i++) {
            BigDecimal pressure = getOpenMoldPressure(detail, i);
            BigDecimal flow = getOpenMoldFlow(detail, i);
            BigDecimal position = getOpenMoldPosition(detail, i);
            
            String leftLabel = "开模" + i;
            
            // 每两个一行
            if (i % 2 == 1) {
                BigDecimal pressure2 = getOpenMoldPressure(detail, i + 1);
                BigDecimal flow2 = getOpenMoldFlow(detail, i + 1);
                BigDecimal position2 = getOpenMoldPosition(detail, i + 1);
                String rightLabel = "开模" + (i + 1);
                
                currentRow = createParameterRow(sheet, currentRow, leftLabel, pressure, flow, position,
                    rightLabel, pressure2, flow2, position2, labelStyle, dataStyle);
            }
        }
        
        // 抽芯参数
        currentRow = createParameterRow(sheet, currentRow, "抽芯一",
            detail.getCorePull1OutPressure(), detail.getCorePull1OutFlow(), detail.getCorePull1OutPosition(),
            "抽芯二", detail.getCorePull2OutPressure(), detail.getCorePull2OutFlow(), detail.getCorePull2OutPosition(),
            labelStyle, dataStyle);
        
        // 熔胶参数
        currentRow = createParameterRow(sheet, currentRow, "熔胶1",
            detail.getMelt1Pressure(), detail.getMelt1Flow(), detail.getMelt1Position(),
            "熔前松退", detail.getDecompressionBeforeMeltPressure(), detail.getDecompressionBeforeMeltFlow(), 
            detail.getDecompressionBeforeMeltPosition(), labelStyle, dataStyle);
        
        currentRow = createParameterRow(sheet, currentRow, "熔后松退",
            detail.getDecompressionAfterMeltPressure(), detail.getDecompressionAfterMeltFlow(), 
            detail.getDecompressionAfterMeltPosition(),
            "", null, null, null, labelStyle, dataStyle);
        
        // 顶出参数
        currentRow = createParameterRow(sheet, currentRow, "顶出一速",
            detail.getEject1SpeedPressure(), detail.getEject1SpeedFlow(), detail.getEject1SpeedPosition(),
            "顶出二速", detail.getEject2SpeedPressure(), detail.getEject2SpeedFlow(), detail.getEject2SpeedPosition(),
            labelStyle, dataStyle);
        
        currentRow = createParameterRow(sheet, currentRow, "顶退一速",
            detail.getEjectRetract1SpeedPressure(), detail.getEjectRetract1SpeedFlow(), detail.getEjectRetract1SpeedPosition(),
            "顶退二速", detail.getEjectRetract2SpeedPressure(), detail.getEjectRetract2SpeedFlow(), detail.getEjectRetract2SpeedPosition(),
            labelStyle, dataStyle);
        
        return currentRow;
    }
    
    /**
     * 创建温度和时间参数（右侧列）
     */
    private static void createTemperatureAndTimeParams(Sheet sheet, ProcessFileDetail detail,
                                                       CellStyle labelStyle, CellStyle dataStyle) {
        int startRow = 6; // 从第7行开始（0-based index）
        int col = 8; // 从第I列开始
        
        // 料筒温度标题
        Row tempHeaderRow = sheet.getRow(startRow);
        if (tempHeaderRow == null) tempHeaderRow = sheet.createRow(startRow);
        createLabelCell(tempHeaderRow, col, "料筒温度 ℃", labelStyle);
        
        // 料筒温度（4段）
        for (int i = 1; i <= 4; i++) {
            Row row = sheet.getRow(startRow + i);
            if (row == null) row = sheet.createRow(startRow + i);
            BigDecimal temp = getBarrelTemp(detail, i);
            createLabelCell(row, col, "第" + (i == 1 ? "一" : i == 2 ? "二" : i == 3 ? "三" : "四") + "段", labelStyle);
            createDataCell(row, col + 1, formatDecimal(temp) + " ±5", dataStyle);
        }
        
        // 模具温度
        Row moldTempRow = sheet.getRow(startRow + 5);
        if (moldTempRow == null) moldTempRow = sheet.createRow(startRow + 5);
        createLabelCell(moldTempRow, col, "模具温度 ℃", labelStyle);
        createDataCell(moldTempRow, col + 1, formatDecimal(detail.getMoldTemp()) + " ±5", dataStyle);
        
        // 注塑成型时间标题
        Row timeHeaderRow = sheet.getRow(startRow + 6);
        if (timeHeaderRow == null) timeHeaderRow = sheet.createRow(startRow + 6);
        createLabelCell(timeHeaderRow, col, "注塑成型时间 s", labelStyle);
        
        // 时间参数
        String[] timeLabels = {"合模", "模保", "进芯1", "进芯2", "注射", "保压", "冷却", 
                               "抽芯1", "抽芯2", "开模", "取件时间", "总时间"};
        BigDecimal[] timeValues = {
            detail.getClampingTime(), detail.getMoldProtectionTime(), 
            detail.getCorePull1InTime(), detail.getCorePull2InTime(),
            detail.getInjectionTime(), detail.getHoldingTime(), detail.getCoolingTime(),
            detail.getCorePull1OutTime(), detail.getCorePull2OutTime(),
            detail.getMoldOpeningTime(), detail.getPartEjectionTime(), detail.getTotalTime()
        };
        
        for (int i = 0; i < timeLabels.length; i++) {
            Row row = sheet.getRow(startRow + 7 + i);
            if (row == null) row = sheet.createRow(startRow + 7 + i);
            createLabelCell(row, col, timeLabels[i], labelStyle);
            createDataCell(row, col + 1, formatDecimal(timeValues[i]), dataStyle);
        }
    }
    
    /**
     * 创建特殊模式参数
     */
    private static int createSpecialModeParams(Sheet sheet, int startRow, ProcessFileDetail detail,
                                              CellStyle labelStyle, CellStyle dataStyle) {
        Row row1 = sheet.createRow(startRow++);
        Row row2 = sheet.createRow(startRow++);
        
        int col = 0;
        
        // 第一行：标签
        createLabelCell(row1, col++, "注射模式", labelStyle);
        createLabelCell(row1, col++, "进芯方式", labelStyle);
        createLabelCell(row1, col++, "抽芯方式", labelStyle);
        createLabelCell(row1, col++, "座台方式", labelStyle);
        createLabelCell(row1, col++, "顶针模式", labelStyle);
        createLabelCell(row1, col++, "顶针次数", labelStyle);
        createLabelCell(row1, col++, "螺杆转速", labelStyle);
        createLabelCell(row1, col++, "抽芯行程方式", labelStyle);
        
        // 第二行：数据
        col = 0;
        createDataCell(row2, col++, detail.getInjectionMode(), dataStyle);
        createDataCell(row2, col++, detail.getCorePullInMethod(), dataStyle);
        createDataCell(row2, col++, detail.getCorePullOutMethod(), dataStyle);
        createDataCell(row2, col++, detail.getNozzleContactMethod(), dataStyle);
        createDataCell(row2, col++, detail.getEjectionMode(), dataStyle);
        createDataCell(row2, col++, detail.getEjectionCount() != null ? detail.getEjectionCount().toString() : "", dataStyle);
        createDataCell(row2, col++, formatDecimal(detail.getScrewSpeed()), dataStyle);
        createDataCell(row2, col++, detail.getCorePullStrokeMethod(), dataStyle);
        
        return startRow;
    }
    
    /**
     * 创建原材料干燥处理和零件后处理
     */
    private static int createDryingAndPostTreatment(Sheet sheet, int startRow, ProcessFileDetail detail,
                                                      CellStyle labelStyle, CellStyle dataStyle) {
        Row row1 = sheet.createRow(startRow++);
        Row row2 = sheet.createRow(startRow++);
        Row row3 = sheet.createRow(startRow++);
        Row row4 = sheet.createRow(startRow++);
        
        // 左侧：原材料干燥处理
        int col = 0;
        createLabelCell(row1, col, "使用设备", labelStyle);
        createDataCell(row2, col++, detail.getDryingEquipment(), dataStyle);
        
        createLabelCell(row1, col, "盛料高度", labelStyle);
        createDataCell(row2, col++, formatDecimal(detail.getMaterialFillHeight()), dataStyle);
        
        createLabelCell(row1, col, "翻料时间", labelStyle);
        createDataCell(row2, col++, formatDecimal(detail.getMaterialTurningTime()), dataStyle);
        
        createLabelCell(row1, col, "干燥温度", labelStyle);
        createDataCell(row2, col++, formatDecimal(detail.getDryingTemp()), dataStyle);
        
        createLabelCell(row1, col, "前模冷却", labelStyle);
        createDataCell(row2, col++, detail.getFrontMoldCooling(), dataStyle);
        
        // 中间：零件后处理
        col = 6;
        createLabelCell(row1, col, "零件后处理", labelStyle);
        createDataCell(row2, col, detail.getPartPostTreatment(), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(row2.getRowNum(), row2.getRowNum(), col, col + 1));
        
        createLabelCell(row3, col, "产品后处理", labelStyle);
        createDataCell(row4, col, detail.getProductPostTreatment(), dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(row4.getRowNum(), row4.getRowNum(), col, col + 1));
        
        // 右侧：后处理参数
        col = 8;
        createLabelCell(row1, col, "加热温度", labelStyle);
        createDataCell(row2, col++, formatDecimal(detail.getHeatingTemp()), dataStyle);
        
        createLabelCell(row1, col, "保温温度", labelStyle);
        createDataCell(row2, col++, formatDecimal(detail.getHoldingTemp()), dataStyle);
        
        createLabelCell(row1, col, "干燥时间", labelStyle);
        createDataCell(row2, col++, formatDecimal(detail.getDryingTime()), dataStyle);
        
        createLabelCell(row1, col, "后模冷却", labelStyle);
        createDataCell(row2, col++, detail.getRearMoldCooling(), dataStyle);
        
        return startRow;
    }
    
    /**
     * 创建工序内容和品质检查
     */
    private static int createProcessContentAndQuality(Sheet sheet, int startRow, ProcessFileDetail detail,
                                                     CellStyle labelStyle, CellStyle dataStyle) {
        Row row1 = sheet.createRow(startRow++);
        Row row2 = sheet.createRow(startRow++);
        Row row3 = sheet.createRow(startRow++);
        Row row4 = sheet.createRow(startRow++);
        
        // 左侧：工序内容
        int col = 0;
        Cell labelCell1 = row1.createCell(col);
        labelCell1.setCellValue("工序内容");
        labelCell1.setCellStyle(labelStyle);
        // 标签行合并区域
        sheet.addMergedRegion(new CellRangeAddress(row1.getRowNum(), row1.getRowNum(), col, col + 5));
        
        String processContent = detail.getProcessContent();
        String[] contentLines;
        if (processContent != null && !processContent.isEmpty()) {
            contentLines = processContent.split("\n");
        } else {
            // 默认内容
            contentLines = new String[]{
                "按照要求将模具码调好并调整。",
                "按照要求配料(配色)并将其按标准将原料烘干。",
                "按照注塑工艺要求将加工工艺调整。",
                "检查注塑设备及模具运行的安全性。"
            };
        }
        
        // 第一行（row1）已经在标签行添加了合并区域，所以从第二行开始添加数据行的合并区域
        for (int i = 0; i < Math.min(contentLines.length, 4); i++) {
            Row currentRow = i == 0 ? row1 : (i == 1 ? row2 : (i == 2 ? row3 : row4));
            Cell dataCell = currentRow.createCell(col);
            dataCell.setCellValue((i + 1) + ". " + contentLines[i].trim());
            dataCell.setCellStyle(dataStyle);
            // 只有非第一行才添加合并区域（第一行已经在标签行添加过了）
            if (i > 0) {
                sheet.addMergedRegion(new CellRangeAddress(currentRow.getRowNum(), currentRow.getRowNum(), col, col + 5));
            }
        }
        
        // 右侧：品质检查
        col = 6;
        Cell labelCell2 = row1.createCell(col);
        labelCell2.setCellValue("品质检查");
        labelCell2.setCellStyle(labelStyle);
        // 标签行合并区域
        sheet.addMergedRegion(new CellRangeAddress(row1.getRowNum(), row1.getRowNum(), col, col + 8));
        
        String qualityInspection = detail.getQualityInspection();
        String[] qualityLines;
        if (qualityInspection != null && !qualityInspection.isEmpty()) {
            qualityLines = qualityInspection.split("\n");
        } else {
            // 默认内容
            qualityLines = new String[]{
                "产品的尺寸光洁度符合图样要求,尺寸稳定。",
                "形状完整,表面光滑,结合部位平滑。",
                "产品无明显收缩及明显溶解线。",
                "产品外观颜色一致,无色差,杂质,油垢等。"
            };
        }
        
        // 第一行（row1）已经在标签行添加了合并区域，所以从第二行开始添加数据行的合并区域
        for (int i = 0; i < Math.min(qualityLines.length, 4); i++) {
            Row currentRow = i == 0 ? row1 : (i == 1 ? row2 : (i == 2 ? row3 : row4));
            Cell dataCell = currentRow.createCell(col);
            dataCell.setCellValue((i + 1) + ". " + qualityLines[i].trim());
            dataCell.setCellStyle(dataStyle);
            // 只有非第一行才添加合并区域（第一行已经在标签行添加过了）
            if (i > 0) {
                sheet.addMergedRegion(new CellRangeAddress(currentRow.getRowNum(), currentRow.getRowNum(), col, col + 8));
            }
        }
        
        return startRow;
    }
    
    /**
     * 创建综合评估行
     */
    private static void createAssessmentRow(Row row, ProcessFileDetail detail, 
                                          CellStyle labelStyle, CellStyle dataStyle, Sheet sheet) {
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue("模具及注塑工艺综合评估:");
        labelCell.setCellStyle(labelStyle);
        
        String assessment = detail.getComprehensiveAssessment();
        if (assessment == null || assessment.isEmpty()) {
            assessment = "可以量产。";
        }
        
        Cell dataCell = row.createCell(1);
        dataCell.setCellValue(assessment);
        dataCell.setCellStyle(dataStyle);
        
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 14));
    }
    
    /**
     * 创建审批签名区域
     */
    private static void createSignatureArea(Sheet sheet, int startRow, CellStyle labelStyle, Sheet sheetParam) {
        // sheetParam参数用于兼容，实际使用sheet
        Row row1 = sheet.createRow(startRow++);
        Row row2 = sheet.createRow(startRow++);
        Row row3 = sheet.createRow(startRow++);
        Row row4 = sheet.createRow(startRow++);
        
        String[] labels = {"编制人", "审核人", "批准人", "会签"};
        
        for (int i = 0; i < labels.length; i++) {
            int col = i * 3;
            
            // 标签行
            Cell labelCell = row1.createCell(col);
            labelCell.setCellValue(labels[i]);
            labelCell.setCellStyle(labelStyle);
            sheet.addMergedRegion(new CellRangeAddress(row1.getRowNum(), row1.getRowNum(), col, col + 2));
            
            // 签名区域（空白，后续插入签名图片）
            Cell signatureCell = row2.createCell(col);
            signatureCell.setCellValue("");
            sheet.addMergedRegion(new CellRangeAddress(row2.getRowNum(), row4.getRowNum(), col, col + 2));
            
            // 设置行高，为签名图片预留空间
            row2.setHeightInPoints(60);
            row3.setHeightInPoints(60);
            row4.setHeightInPoints(60);
        }
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 创建参数行（左右各一个参数组）
     */
    private static int createParameterRow(Sheet sheet, int rowIndex, String leftLabel,
                                         BigDecimal leftPressure, BigDecimal leftFlow, BigDecimal leftPosition,
                                         String rightLabel, BigDecimal rightPressure, BigDecimal rightFlow, BigDecimal rightPosition,
                                         CellStyle labelStyle, CellStyle dataStyle) {
        Row row = sheet.createRow(rowIndex);
        
        // 左侧参数
        createLabelCell(row, 0, leftLabel, labelStyle);
        createDataCell(row, 1, formatDecimal(leftPressure) + " ±10", dataStyle);
        createDataCell(row, 2, formatDecimal(leftFlow) + " ±10", dataStyle);
        createDataCell(row, 3, formatDecimal(leftPosition) + " ±10", dataStyle);
        
        // 右侧参数
        if (rightLabel != null && !rightLabel.isEmpty()) {
            createLabelCell(row, 4, rightLabel, labelStyle);
            createDataCell(row, 5, formatDecimal(rightPressure) + " ±10", dataStyle);
            createDataCell(row, 6, formatDecimal(rightFlow) + " ±10", dataStyle);
            createDataCell(row, 7, formatDecimal(rightPosition) + " ±10", dataStyle);
        }
        
        return rowIndex + 1;
    }
    
    /**
     * 创建标签单元格
     */
    private static void createLabelCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
    
    /**
     * 创建数据单元格
     */
    private static void createDataCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
    
    /**
     * 创建标题单元格
     */
    private static void createTitleCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
    
    /**
     * 创建表头单元格
     */
    private static void createHeaderCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
    
    /**
     * 格式化小数
     */
    private static String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString();
    }
    
    /**
     * 获取射胶压力
     */
    private static BigDecimal getInjectionPressure(ProcessFileDetail detail, int stage) {
        return switch (stage) {
            case 1 -> detail.getInjection1Pressure();
            case 2 -> detail.getInjection2Pressure();
            case 3 -> detail.getInjection3Pressure();
            case 4 -> detail.getInjection4Pressure();
            case 5 -> detail.getInjection5Pressure();
            case 6 -> detail.getInjection6Pressure();
            default -> null;
        };
    }
    
    /**
     * 获取射胶流量
     */
    private static BigDecimal getInjectionFlow(ProcessFileDetail detail, int stage) {
        return switch (stage) {
            case 1 -> detail.getInjection1Flow();
            case 2 -> detail.getInjection2Flow();
            case 3 -> detail.getInjection3Flow();
            case 4 -> detail.getInjection4Flow();
            case 5 -> detail.getInjection5Flow();
            case 6 -> detail.getInjection6Flow();
            default -> null;
        };
    }
    
    /**
     * 获取射胶位置
     */
    private static BigDecimal getInjectionPosition(ProcessFileDetail detail, int stage) {
        return switch (stage) {
            case 1 -> detail.getInjection1Position();
            case 2 -> detail.getInjection2Position();
            case 3 -> detail.getInjection3Position();
            case 4 -> detail.getInjection4Position();
            case 5 -> detail.getInjection5Position();
            case 6 -> detail.getInjection6Position();
            default -> null;
        };
    }
    
    /**
     * 获取保压压力
     */
    private static BigDecimal getHoldingPressure(ProcessFileDetail detail, int stage) {
        return switch (stage) {
            case 1 -> detail.getHolding1Pressure();
            case 2 -> detail.getHolding2Pressure();
            case 3 -> detail.getHolding3Pressure();
            default -> null;
        };
    }
    
    /**
     * 获取保压流量
     */
    private static BigDecimal getHoldingFlow(ProcessFileDetail detail, int stage) {
        return switch (stage) {
            case 1 -> detail.getHolding1Flow();
            case 2 -> detail.getHolding2Flow();
            case 3 -> detail.getHolding3Flow();
            default -> null;
        };
    }
    
    /**
     * 获取保压位置
     */
    private static BigDecimal getHoldingPosition(ProcessFileDetail detail, int stage) {
        return switch (stage) {
            case 1 -> detail.getHolding1Position();
            case 2 -> detail.getHolding2Position();
            case 3 -> detail.getHolding3Position();
            default -> null;
        };
    }
    
    /**
     * 获取开模压力
     */
    private static BigDecimal getOpenMoldPressure(ProcessFileDetail detail, int stage) {
        return switch (stage) {
            case 1 -> detail.getOpenMold1Pressure();
            case 2 -> detail.getOpenMold2Pressure();
            case 3 -> detail.getOpenMold3Pressure();
            case 4 -> detail.getOpenMold4Pressure();
            default -> null;
        };
    }
    
    /**
     * 获取开模流量
     */
    private static BigDecimal getOpenMoldFlow(ProcessFileDetail detail, int stage) {
        return switch (stage) {
            case 1 -> detail.getOpenMold1Flow();
            case 2 -> detail.getOpenMold2Flow();
            case 3 -> detail.getOpenMold3Flow();
            case 4 -> detail.getOpenMold4Flow();
            default -> null;
        };
    }
    
    /**
     * 获取开模位置
     */
    private static BigDecimal getOpenMoldPosition(ProcessFileDetail detail, int stage) {
        return switch (stage) {
            case 1 -> detail.getOpenMold1Position();
            case 2 -> detail.getOpenMold2Position();
            case 3 -> detail.getOpenMold3Position();
            case 4 -> detail.getOpenMold4Position();
            default -> null;
        };
    }
    
    /**
     * 获取料筒温度
     */
    private static BigDecimal getBarrelTemp(ProcessFileDetail detail, int stage) {
        return switch (stage) {
            case 1 -> detail.getBarrelTemp1();
            case 2 -> detail.getBarrelTemp2();
            case 3 -> detail.getBarrelTemp3();
            case 4 -> detail.getBarrelTemp4();
            default -> null;
        };
    }
    
    /**
     * 创建样式
     */
    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
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
        style.setAlignment(HorizontalAlignment.LEFT);
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
    
    private static CellStyle createBorderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 在Excel中插入签名图片
     * 
     * @param excelFilePath Excel文件路径
     * @param signatureImagePath 签名图片路径
     * @param signatureType 签名类型：SUBMIT-编制人，APPROVE_LEVEL1-审核人，APPROVE_LEVEL2-会签，APPROVE_LEVEL3-批准人
     */
    public static void insertSignatureToGeneratedExcel(String excelFilePath, String signatureImagePath, String signatureType) {
        ProcessFileExcelUtil.insertSignatureToExcel(excelFilePath, signatureImagePath, signatureType);
    }
}
