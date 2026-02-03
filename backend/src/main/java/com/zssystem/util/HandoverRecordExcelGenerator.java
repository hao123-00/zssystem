package com.zssystem.util;

import com.zssystem.entity.Equipment;
import com.zssystem.entity.HandoverRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 注塑车间月份交接班记录表 Excel 导出
 * 布局：标题、设备编号、表头、28行数据、标记符号说明，打印时整表缩放到一页
 */
public class HandoverRecordExcelGenerator {

    private static final int DATA_ROWS_PER_SHEET = 28;
    private static final int TOTAL_COLS = 15;

    // 列索引：日期|班次|产品名称|材质|现场状况4列|生产情况4列|工艺|交接组长|接班组长
    private static final int COL_DATE = 0;
    private static final int COL_SHIFT = 1;
    private static final int COL_PRODUCT = 2;
    private static final int COL_MATERIAL = 3;
    private static final int COL_EQUIP_CLEAN = 4;
    private static final int COL_FLOOR_CLEAN = 5;
    private static final int COL_LEAKAGE = 6;
    private static final int COL_ITEM_PLACE = 7;
    private static final int COL_INJECTION = 8;
    private static final int COL_ROBOT = 9;
    private static final int COL_LINE = 10;
    private static final int COL_MOLD = 11;
    private static final int COL_PROCESS = 12;
    private static final int COL_HANDOVER_LEADER = 13;
    private static final int COL_RECEIVING_LEADER = 14;

    /**
     * 生成交接班记录 Excel，每张最多28条记录，超过则分多文件返回
     * 每张Excel布局相同（标题、设备编号、表头、28行数据、图例），仅数据不同
     */
    public static List<byte[]> generate(Equipment equipment, String recordMonth, List<HandoverRecord> records) throws IOException {
        List<byte[]> result = new ArrayList<>();
        String equipmentNo = equipment != null ? equipment.getEquipmentNo() : "";

        for (int start = 0; start < records.size(); start += DATA_ROWS_PER_SHEET) {
            int end = Math.min(start + DATA_ROWS_PER_SHEET, records.size());
            List<HandoverRecord> batch = records.subList(start, end);
            String handoverLeader = batch.stream().map(HandoverRecord::getHandoverLeader).filter(s -> s != null && !s.isBlank()).findFirst().orElse("");
            String receivingLeader = batch.stream().map(HandoverRecord::getReceivingLeader).filter(s -> s != null && !s.isBlank()).findFirst().orElse("");
            int pageIndex = result.size() + 1;
            int totalPages = (records.size() + DATA_ROWS_PER_SHEET - 1) / DATA_ROWS_PER_SHEET;
            String pageSuffix = totalPages > 1 ? "（第" + pageIndex + "页）" : "";
            byte[] bytes = createWorkbook(equipmentNo, recordMonth, handoverLeader, receivingLeader, batch, pageSuffix);
            result.add(bytes);
        }

        if (result.isEmpty()) {
            result.add(createWorkbook(equipmentNo, recordMonth, "", "", List.of(), ""));
        }
        return result;
    }

    /**
     * 生成单张 Excel（用于分页导出）
     */
    public static byte[] createWorkbookForPage(String equipmentNo, String recordMonth,
                                               List<HandoverRecord> records, String pageSuffix) throws IOException {
        String handoverLeader = records.stream().map(HandoverRecord::getHandoverLeader)
                .filter(s -> s != null && !s.isBlank()).findFirst().orElse("");
        String receivingLeader = records.stream().map(HandoverRecord::getReceivingLeader)
                .filter(s -> s != null && !s.isBlank()).findFirst().orElse("");
        return createWorkbook(equipmentNo, recordMonth, handoverLeader, receivingLeader, records, pageSuffix);
    }

    private static byte[] createWorkbook(String equipmentNo, String recordMonth,
                                         String handoverLeader, String receivingLeader,
                                         List<HandoverRecord> records, String pageSuffix) throws IOException {
        YearMonth ym = YearMonth.parse(recordMonth);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("交接班记录");

            // 列宽
            sheet.setColumnWidth(COL_DATE, 4200);   // 日期时间（yyyy-MM-dd HH:mm:ss）
            sheet.setColumnWidth(COL_SHIFT, 2000);  // 班次
            sheet.setColumnWidth(COL_PRODUCT, 4000);
            sheet.setColumnWidth(COL_MATERIAL, 2800);
            for (int c = COL_EQUIP_CLEAN; c <= COL_MOLD; c++) {
                sheet.setColumnWidth(c, 2800);
            }
            sheet.setColumnWidth(COL_PROCESS, 2800);
            sheet.setColumnWidth(COL_HANDOVER_LEADER, 2800);
            sheet.setColumnWidth(COL_RECEIVING_LEADER, 2800);

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle statusDataStyle = createStatusDataStyle(workbook);  // 状态列专用样式，确保√正确显示
            CellStyle legendStyle = createLegendStyle(workbook);

            int rowIdx = 0;

            // ========== 标题行：注塑车间 X 月份交接班记录表（X为导出时选择的月份，多页时加"第X页"） ==========
            int monthNum = ym.getMonthValue();
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("注塑车间 " + monthNum + " 月份交接班记录表" + (pageSuffix != null ? pageSuffix : ""));
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, TOTAL_COLS - 1));
            titleRow.setHeightInPoints(35);

            // ========== 第二行：合并为一个单元格，设备编号右对齐，无框线 ==========
            Row infoRow = sheet.createRow(rowIdx++);
            Cell infoCell = infoRow.createCell(0);
            infoCell.setCellValue("设备编号: " + equipmentNo);
            CellStyle rightAlignInfoStyle = createRightAlignInfoStyle(workbook);
            infoCell.setCellStyle(rightAlignInfoStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, TOTAL_COLS - 1));
            infoRow.setHeightInPoints(22);

            // ========== 表头行1+2：日期|班次|产品名称|材质|现场状况(4)|生产情况(4)|工艺|交接组长|接班组长（第三、四行加框线） ==========
            int h1Idx = rowIdx++;
            Row h1 = sheet.createRow(h1Idx);
            // A1:A2 日期
            createCell(h1, COL_DATE, "日期", centerStyle);
            sheet.addMergedRegion(new CellRangeAddress(h1Idx, h1Idx + 1, COL_DATE, COL_DATE));
            // B1:B2 班次
            createCell(h1, COL_SHIFT, "班次", centerStyle);
            sheet.addMergedRegion(new CellRangeAddress(h1Idx, h1Idx + 1, COL_SHIFT, COL_SHIFT));
            // C1:C2 产品名称
            createCell(h1, COL_PRODUCT, "产品名称", centerStyle);
            sheet.addMergedRegion(new CellRangeAddress(h1Idx, h1Idx + 1, COL_PRODUCT, COL_PRODUCT));
            // C1:C2 材质
            createCell(h1, COL_MATERIAL, "材质", centerStyle);
            sheet.addMergedRegion(new CellRangeAddress(h1Idx, h1Idx + 1, COL_MATERIAL, COL_MATERIAL));
            // D1:G1 现场状况
            createCell(h1, COL_EQUIP_CLEAN, "现场状况", centerStyle);
            sheet.addMergedRegion(new CellRangeAddress(h1Idx, h1Idx, COL_EQUIP_CLEAN, COL_ITEM_PLACE));
            // H1:L1 生产情况（合并工艺上方单元格，内容居中）
            createCell(h1, COL_INJECTION, "生产情况", centerStyle);
            sheet.addMergedRegion(new CellRangeAddress(h1Idx, h1Idx, COL_INJECTION, COL_PROCESS));
            // M1:M2 交接组长
            createCell(h1, COL_HANDOVER_LEADER, "交接组长", centerStyle);
            sheet.addMergedRegion(new CellRangeAddress(h1Idx, h1Idx + 1, COL_HANDOVER_LEADER, COL_HANDOVER_LEADER));
            // N1:N2 接班组长
            createCell(h1, COL_RECEIVING_LEADER, "接班组长", centerStyle);
            sheet.addMergedRegion(new CellRangeAddress(h1Idx, h1Idx + 1, COL_RECEIVING_LEADER, COL_RECEIVING_LEADER));
            h1.setHeightInPoints(22);

            // ========== 表头行2：现场状况下 设备清洁、地面清洁、有无漏油、物品摆放；生产情况下 注塑机、机械手、流水线、模具 ==========
            int h2Idx = rowIdx++;
            Row h2 = sheet.createRow(h2Idx);
            createCell(h2, COL_EQUIP_CLEAN, "设备清洁", centerStyle);
            createCell(h2, COL_FLOOR_CLEAN, "地面清洁", centerStyle);
            createCell(h2, COL_LEAKAGE, "有无漏油", centerStyle);
            createCell(h2, COL_ITEM_PLACE, "物品摆放", centerStyle);
            createCell(h2, COL_INJECTION, "注塑机", centerStyle);
            createCell(h2, COL_ROBOT, "机械手", centerStyle);
            createCell(h2, COL_LINE, "流水线", centerStyle);
            createCell(h2, COL_MOLD, "模具", centerStyle);
            // L2 工艺（在生产情况子标题同行、模具右侧）
            createCell(h2, COL_PROCESS, "工艺", centerStyle);
            h2.setHeightInPoints(22);

            // ========== 数据行：最多28行 ==========
            for (int i = 0; i < DATA_ROWS_PER_SHEET; i++) {
                Row row = sheet.createRow(rowIdx++);
                HandoverRecord rec = i < records.size() ? records.get(i) : null;
                if (rec != null) {
                    String dateStr = rec.getRecordDate() != null ? rec.getRecordDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
                    String shiftStr = nullToEmpty(rec.getShift());
                    createCell(row, COL_DATE, dateStr, dataStyle);
                    createCell(row, COL_SHIFT, shiftStr, dataStyle);
                    createCell(row, COL_PRODUCT, nullToEmpty(rec.getProductName()), dataStyle);
                    createCell(row, COL_MATERIAL, nullToEmpty(rec.getMaterial()), dataStyle);
                    createCell(row, COL_EQUIP_CLEAN, formatStatusForExcel(rec.getEquipmentCleaning()), statusDataStyle);
                    createCell(row, COL_FLOOR_CLEAN, formatStatusForExcel(rec.getFloorCleaning()), statusDataStyle);
                    createCell(row, COL_LEAKAGE, formatStatusForExcel(rec.getLeakage()), statusDataStyle);
                    createCell(row, COL_ITEM_PLACE, formatStatusForExcel(rec.getItemPlacement()), statusDataStyle);
                    createCell(row, COL_INJECTION, formatStatusForExcel(rec.getInjectionMachine()), statusDataStyle);
                    createCell(row, COL_ROBOT, formatStatusForExcel(rec.getRobot()), statusDataStyle);
                    createCell(row, COL_LINE, formatStatusForExcel(rec.getAssemblyLine()), statusDataStyle);
                    createCell(row, COL_MOLD, formatStatusForExcel(rec.getMold()), statusDataStyle);
                    createCell(row, COL_PROCESS, formatStatusForExcel(rec.getProcess()), statusDataStyle);
                    createCell(row, COL_HANDOVER_LEADER, nullToEmpty(rec.getHandoverLeader()), dataStyle);
                    createCell(row, COL_RECEIVING_LEADER, nullToEmpty(rec.getReceivingLeader()), dataStyle);
                } else {
                    for (int c = 0; c < TOTAL_COLS; c++) {
                        createCell(row, c, "", dataStyle);
                    }
                }
                row.setHeightInPoints(20);
            }

            // ========== 标记符号说明行 ==========
            int legendRowIdx = rowIdx++;
            Row legendRow = sheet.createRow(legendRowIdx);
            Cell legendCell = legendRow.createCell(0);
            legendCell.setCellValue("标记符号: 正常\"√\"  异常\"写异常原因\"  修理\"△\"  停机\"○\"  修复\"▲\"");
            legendCell.setCellStyle(legendStyle);
            sheet.addMergedRegion(new CellRangeAddress(legendRowIdx, legendRowIdx, 0, TOTAL_COLS - 1));
            legendRow.setHeightInPoints(20);

            // 第三行至第32行加框线（第32行为最后一数据行），第33行（标记符号说明）无框线
            int lastDataRowIdx = legendRowIdx - 1;  // Excel 第32行 = 最后一数据行
            applyBordersFromRow(sheet, 2, lastDataRowIdx, TOTAL_COLS);

            // 打印设置：整表缩放到一页内
            sheet.setFitToPage(true);
            PrintSetup ps = sheet.getPrintSetup();
            ps.setLandscape(true);
            ps.setFitWidth((short) 1);
            ps.setFitHeight((short) 1);
            sheet.setMargin(Sheet.LeftMargin, 0.5);
            sheet.setMargin(Sheet.RightMargin, 0.5);
            sheet.setMargin(Sheet.TopMargin, 0.5);
            sheet.setMargin(Sheet.BottomMargin, 0.5);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    /** 状态显示：正常->√ 黑色对号（U+221A 兼容性最好） */
    private static final String CHECK_MARK = "\u221A";  // √ SQUARE ROOT 作对号

    private static String formatStatusForExcel(String s) {
        if (s == null || s.isEmpty()) return "";
        String t = s.trim();
        if ("正常".equals(t)) return CHECK_MARK;
        if ("√".equals(t) || "\u2714".equals(t) || "\u2713".equals(t)) return CHECK_MARK;  // 兼容√✔✓
        return s;
    }

    private static void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.getCell(col);
        if (cell == null) cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private static CellStyle createTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        s.setFont(font);
        // 第一行无框线
        return s;
    }

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = wb.createFont();
        font.setBold(true);
        s.setFont(font);
        setBorder(s);
        return s;
    }

    /** 第二行用：右对齐，无框线（标题与表头之间的空白区域） */
    private static CellStyle createRightAlignInfoStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.RIGHT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = wb.createFont();
        font.setBold(true);
        s.setFont(font);
        // 不添加框线，与第一行标题一致
        return s;
    }

    private static CellStyle createCenterStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s);
        return s;
    }

    private static CellStyle createDataStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s);
        return s;
    }

    /** 状态列样式，使用 Arial 确保 √ 等符号正确显示 */
    private static CellStyle createStatusDataStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 11);
        s.setFont(font);
        setBorder(s);
        return s;
    }

    /** 第33行（标记符号说明行）无框线 */
    private static CellStyle createLegendStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        s.setFont(font);
        return s;
    }

    private static void setBorder(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    /** 从指定行起至末行，为所有单元格应用框线（含合并区域内部网格线） */
    private static void applyBordersFromRow(Sheet sheet, int startRow, int endRow, int totalCols) {
        for (int r = startRow; r <= endRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            for (int c = 0; c < totalCols; c++) {
                CellRangeAddress region = new CellRangeAddress(r, r, c, c);
                RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
                RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
                RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
                RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
            }
        }
    }
}
