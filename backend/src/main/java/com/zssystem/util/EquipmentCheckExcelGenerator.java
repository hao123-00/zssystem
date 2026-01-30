package com.zssystem.util;

import com.zssystem.entity.Equipment;
import com.zssystem.entity.EquipmentCheck;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 注塑成型设备点检表 Excel 导出
 * 布局：标题、设备名称(固定)/编号/点检人/年月、最左列分类(电路/机架/油路/周边)、项目列、日期1-30，备注；单页打印。
 */
public class EquipmentCheckExcelGenerator {

    private static final String NORMAL = "✅";
    private static final String ABNORMAL = "❌";

    /** 固定设备名称 */
    private static final String FIXED_EQUIPMENT_NAME = "设备名称：注塑机、模温机、冻水机";

    /** 最左侧分类：电路部分3行、机架部分3行、油路部分5行、周边设备5行 */
    private static final String[] CATEGORY_NAMES = {
        "电路部分", "电路部分", "电路部分",
        "机架部分", "机架部分", "机架部分",
        "油路部分", "油路部分", "油路部分", "油路部分", "油路部分",
        "周边设备", "周边设备", "周边设备", "周边设备", "周边设备",
    };

    /** 16个点检项目名称（与 entity 字段顺序一致） */
    private static final String[] ITEM_NAMES = {
        "发热圈/感温线/交流接触器温度控制器是否正常",
        "电箱排气扇/安全门开关/烘料斗温度是否正常",
        "行程开关是否正常",
        "哥林柱、机架螺母是否松动",
        "安全挡板/射咀/低压保护是否正常",
        "调模牙盘变形及余音是否正常",
        "油泵压力/动作是否正常",
        "油泵/熔胶马达是否有杂音",
        "油温/冷却器是否正常",
        "自动加油润滑油管是否正常",
        "机台油管是否漏油",
        "模温机、冻水机是否有异响",
        "模温机冷却水、冷却水过滤网是否堵塞",
        "油温机是否缺油、温度是否在正常",
        "冻水机过滤器、运水是否正常",
        "冻水机制冷系统、交流接触器是否正常",
    };

    /** 列数：分类(0) + 项目(1) + 30日(2-31) = 32 */
    private static final int COL_CATEGORY = 0;
    private static final int COL_ITEM = 1;
    private static final int COL_DAY_START = 2;
    private static final int COL_DAY_END = 31;
    private static final int TOTAL_COLS = 32;

    /** 第22行固定说明文字（Excel 1-based 第22行 = 0-based 第21行） */
    private static final String REMARK_ROW_FIXED_TEXT = "说明：以上点检项目正常的用\"√\"表示，不正常的用\"×\"表示并在备注栏内注明不正常原因。";

    /**
     * 生成30天点检表 Excel，返回字节数组
     */
    public static byte[] generate(Equipment equipment, String checkMonth, List<EquipmentCheck> records) throws IOException {
        YearMonth ym = YearMonth.parse(checkMonth);
        int year = ym.getYear();
        int month = ym.getMonthValue();
        int daysInMonth = ym.lengthOfMonth();

        Map<Integer, EquipmentCheck> dayMap = records.stream()
            .filter(r -> r.getCheckDate() != null)
            .collect(Collectors.toMap(r -> r.getCheckDate().getDayOfMonth(), r -> r, (a, b) -> b));

        String equipmentNo = equipment != null ? equipment.getEquipmentNo() : "";
        String checkerName = records.isEmpty() ? "" : records.get(0).getCheckerName();
        String remarkAll = records.stream()
            .filter(r -> r.getRemark() != null && !r.getRemark().isBlank())
            .map(r -> r.getCheckDate() + ": " + r.getRemark())
            .collect(Collectors.joining("\n"));

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("点检表");

            // 列宽：分类列、项目列较宽，日期列窄（便于一页打印）
            sheet.setColumnWidth(COL_CATEGORY, 2800);   // 分类
            sheet.setColumnWidth(COL_ITEM, 12000);      // 项目
            for (int c = COL_DAY_START; c <= COL_DAY_END; c++) {
                sheet.setColumnWidth(c, 1200);           // 每日列窄
            }

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle categoryStyle = createCategoryStyle(workbook);
            CellStyle borderOnlyStyle = createBorderOnlyStyle(workbook);

            int rowIdx = 0;

            // ========== 标题行（合并整行） ==========
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("注塑成型设备点检表");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, TOTAL_COLS - 1));
            titleRow.setHeightInPoints(24);

            // ========== 第二行：四格等大（每格占8列）设备名称 | 设备编号 | 点检人 | 年 月 ==========
            Row infoRow = sheet.createRow(rowIdx++);
            createCell(infoRow, 0, FIXED_EQUIPMENT_NAME, headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));
            createCell(infoRow, 8, "设备编号: " + equipmentNo, headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 8, 15));
            createCell(infoRow, 16, "点检人: " + checkerName, headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 16, 23));
            createCell(infoRow, 24, "年 " + year + " 月 " + month, headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 24, TOTAL_COLS - 1));

            // ========== 第三行：A3-B4 合并为“项目”居中；日期(合并30列) ==========
            Row dateRow = sheet.createRow(rowIdx++);
            createCell(dateRow, COL_CATEGORY, "项目", centerStyle);
            createCell(dateRow, COL_ITEM, "", centerStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 3, COL_CATEGORY, COL_ITEM));
            dateRow.getCell(COL_CATEGORY).setCellValue("项目");
            dateRow.getCell(COL_CATEGORY).setCellStyle(centerStyle);
            Cell dateCell = dateRow.createCell(COL_DAY_START);
            dateCell.setCellValue("日期");
            dateCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, COL_DAY_START, COL_DAY_END));
            // ========== 第四行：0、1 列属 A3:B4 合并区不单独填；2-31 列为 1..30 ==========
            Row dayNumRow = sheet.createRow(rowIdx++);
            for (int d = 1; d <= 30; d++) {
                createCell(dayNumRow, COL_DAY_START + d - 1, d <= daysInMonth ? String.valueOf(d) : "", centerStyle);
            }

            // ========== 16 项点检行：最左列分类（合并）、项目名、30 日单元格 ==========
            int itemStartRow = rowIdx;
            for (int item = 0; item < 16; item++) {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, COL_CATEGORY, CATEGORY_NAMES[item], categoryStyle);
                createCell(row, COL_ITEM, ITEM_NAMES[item], dataStyle);
                for (int d = 1; d <= 30; d++) {
                    EquipmentCheck rec = dayMap.get(d);
                    String symbol = getItemSymbol(rec, item);
                    createCell(row, COL_DAY_START + d - 1, symbol, centerStyle);
                }
            }
            int itemEndRow = rowIdx - 1;

            // 最左侧分类列纵向合并：电路 3、机架 3、油路 5、周边 5
            sheet.addMergedRegion(new CellRangeAddress(itemStartRow, itemStartRow + 2, COL_CATEGORY, COL_CATEGORY));
            sheet.addMergedRegion(new CellRangeAddress(itemStartRow + 3, itemStartRow + 5, COL_CATEGORY, COL_CATEGORY));
            sheet.addMergedRegion(new CellRangeAddress(itemStartRow + 6, itemStartRow + 10, COL_CATEGORY, COL_CATEGORY));
            sheet.addMergedRegion(new CellRangeAddress(itemStartRow + 11, itemStartRow + 15, COL_CATEGORY, COL_CATEGORY));

            // 合并后只在每组第一格写文字，其余已通过 createCell 写入相同文字，合并后保留第一个单元格内容
            sheet.getRow(itemStartRow).getCell(COL_CATEGORY).setCellValue("电路部分");
            sheet.getRow(itemStartRow + 3).getCell(COL_CATEGORY).setCellValue("机架部分");
            sheet.getRow(itemStartRow + 6).getCell(COL_CATEGORY).setCellValue("油路部分");
            sheet.getRow(itemStartRow + 11).getCell(COL_CATEGORY).setCellValue("周边设备");

            // ========== 备注（合并整行，多行高度） ==========
            int remarkLabelRowIdx = rowIdx++;
            Row remarkLabelRow = sheet.createRow(remarkLabelRowIdx);
            createCell(remarkLabelRow, COL_CATEGORY, "备注:", headerStyle);
            createCell(remarkLabelRow, COL_ITEM, "", headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(remarkLabelRowIdx, remarkLabelRowIdx, COL_ITEM, COL_DAY_END));
            int remarkRowIdx = rowIdx++;
            Row remarkRow = sheet.createRow(remarkRowIdx);
            Cell remarkCell = remarkRow.createCell(COL_CATEGORY);
            remarkCell.setCellValue(REMARK_ROW_FIXED_TEXT);
            remarkCell.setCellStyle(centerStyle);
            sheet.addMergedRegion(new CellRangeAddress(remarkRowIdx, remarkRowIdx, COL_CATEGORY, COL_DAY_END));
            remarkRow.setHeightInPoints(45);

            // ========== 为所有单元格加上框线（未创建的单元格补上带框线样式） ==========
            applyBorderToAllCells(sheet, remarkRowIdx, TOTAL_COLS - 1, borderOnlyStyle);

            // ========== 打印设置：适应一页纸 ==========
            sheet.setFitToPage(true);
            PrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setFitWidth((short) 1);
            printSetup.setFitHeight((short) 1);
            sheet.setAutobreaks(true);
            sheet.getRow(0).setHeightInPoints(22);
            for (int r = itemStartRow; r <= itemEndRow; r++) {
                Row row = sheet.getRow(r);
                if (row != null) row.setHeightInPoints(18);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static String getItemSymbol(EquipmentCheck rec, int itemIndex) {
        if (rec == null) return "";
        Integer val = null;
        switch (itemIndex) {
            case 0: val = rec.getCircuitItem1(); break;
            case 1: val = rec.getCircuitItem2(); break;
            case 2: val = rec.getCircuitItem3(); break;
            case 3: val = rec.getFrameItem1(); break;
            case 4: val = rec.getFrameItem2(); break;
            case 5: val = rec.getFrameItem3(); break;
            case 6: val = rec.getOilItem1(); break;
            case 7: val = rec.getOilItem2(); break;
            case 8: val = rec.getOilItem3(); break;
            case 9: val = rec.getOilItem4(); break;
            case 10: val = rec.getOilItem5(); break;
            case 11: val = rec.getPeripheralItem1(); break;
            case 12: val = rec.getPeripheralItem2(); break;
            case 13: val = rec.getPeripheralItem3(); break;
            case 14: val = rec.getPeripheralItem4(); break;
            case 15: val = rec.getPeripheralItem5(); break;
            default: return "";
        }
        if (val == null) return "";
        return Integer.valueOf(1).equals(val) ? NORMAL : ABNORMAL;
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
        font.setFontHeightInPoints((short) 16);
        s.setFont(font);
        setBorder(s);
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

    private static CellStyle createCategoryStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = wb.createFont();
        font.setBold(true);
        s.setFont(font);
        setBorder(s);
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
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s);
        return s;
    }

    private static void setBorder(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    /** 仅框线的样式（用于补全空单元格的框线） */
    private static CellStyle createBorderOnlyStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s);
        return s;
    }

    /** 为范围内未创建的单元格补上带框线样式，使整表所有格子都有框线 */
    private static void applyBorderToAllCells(Sheet sheet, int lastRowIndex, int lastColIndex, CellStyle borderStyle) {
        for (int r = 0; r <= lastRowIndex; r++) {
            Row row = sheet.getRow(r);
            if (row == null) row = sheet.createRow(r);
            for (int c = 0; c <= lastColIndex; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) {
                    cell = row.createCell(c);
                    cell.setCellStyle(borderStyle);
                }
            }
        }
    }
}
