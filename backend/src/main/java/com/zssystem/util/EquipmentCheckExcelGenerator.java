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
 * 样式：标题、设备名称/编号/点检人/年月、日期1-30列、16项点检项目行，正常✅异常❌
 */
public class EquipmentCheckExcelGenerator {

    private static final String NORMAL = "✅";
    private static final String ABNORMAL = "❌";

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

    /**
     * 生成30天点检表 Excel，返回字节数组
     */
    public static byte[] generate(Equipment equipment, String checkMonth, List<EquipmentCheck> records) throws IOException {
        YearMonth ym = YearMonth.parse(checkMonth);
        int year = ym.getYear();
        int month = ym.getMonthValue();
        int daysInMonth = ym.lengthOfMonth();

        // 按日期映射：dayOfMonth -> record
        Map<Integer, EquipmentCheck> dayMap = records.stream()
            .filter(r -> r.getCheckDate() != null)
            .collect(Collectors.toMap(r -> r.getCheckDate().getDayOfMonth(), r -> r, (a, b) -> b));

        String equipmentName = equipment != null ? equipment.getEquipmentName() : "";
        String equipmentNo = equipment != null ? equipment.getEquipmentNo() : "";
        String checkerName = records.isEmpty() ? "" : records.get(0).getCheckerName();
        String remarkAll = records.stream()
            .filter(r -> r.getRemark() != null && !r.getRemark().isBlank())
            .map(r -> r.getCheckDate() + ": " + r.getRemark())
            .collect(Collectors.joining("\n"));

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("点检表");
            int colCount = 1 + 30; // 项目列 + 1..30 日
            for (int i = 0; i < colCount; i++) {
                sheet.setColumnWidth(i, 3200);
            }
            sheet.setColumnWidth(0, 12000); // 项目列加宽

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int rowIdx = 0;

            // 标题行
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("注塑成型设备点检表");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colCount - 1));
            titleRow.setHeightInPoints(28);

            // 设备名称、设备编号、点检人、年、月
            Row infoRow = sheet.createRow(rowIdx++);
            createCell(infoRow, 0, "设备名称:", headerStyle);
            createCell(infoRow, 1, equipmentName, dataStyle);
            createCell(infoRow, 3, "设备编号:", headerStyle);
            createCell(infoRow, 4, equipmentNo, dataStyle);
            createCell(infoRow, 6, "点检人:", headerStyle);
            createCell(infoRow, 7, checkerName, dataStyle);
            createCell(infoRow, 9, "年", headerStyle);
            createCell(infoRow, 10, String.valueOf(year), dataStyle);
            createCell(infoRow, 11, "月", headerStyle);
            createCell(infoRow, 12, String.valueOf(month), dataStyle);

            // 日期行：项目 | 1 | 2 | ... | 30
            Row dateRow = sheet.createRow(rowIdx++);
            createCell(dateRow, 0, "日期", headerStyle);
            for (int d = 1; d <= 30; d++) {
                createCell(dateRow, d, d <= daysInMonth ? String.valueOf(d) : "", centerStyle);
            }

            // 16 项点检项目行
            for (int item = 0; item < 16; item++) {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, ITEM_NAMES[item], dataStyle);
                for (int d = 1; d <= 30; d++) {
                    EquipmentCheck rec = dayMap.get(d);
                    String symbol = getItemSymbol(rec, item);
                    createCell(row, d, symbol, centerStyle);
                }
            }

            // 备注
            int remarkLabelRowIdx = rowIdx++;
            Row remarkLabelRow = sheet.createRow(remarkLabelRowIdx);
            createCell(remarkLabelRow, 0, "备注:", headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(remarkLabelRowIdx, remarkLabelRowIdx, 1, colCount - 1));
            int remarkRowIdx = rowIdx++;
            Row remarkRow = sheet.createRow(remarkRowIdx);
            Cell remarkCell = remarkRow.createCell(0);
            remarkCell.setCellValue(remarkAll);
            remarkCell.setCellStyle(dataStyle);
            sheet.addMergedRegion(new CellRangeAddress(remarkRowIdx, remarkRowIdx, 0, colCount - 1));
            remarkRow.setHeightInPoints(60);

            // 说明
            int descRowIdx = rowIdx++;
            Row descRow = sheet.createRow(descRowIdx);
            Cell descCell = descRow.createCell(0);
            descCell.setCellValue("说明：以上点检项目正常的用\"√\"表示，不正常的用\"×\"表示并在备注栏内注明不正常原因。本表导出中正常用✅、异常用❌表示。");
            descCell.setCellStyle(dataStyle);
            sheet.addMergedRegion(new CellRangeAddress(descRowIdx, descRowIdx, 0, colCount - 1));

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
        Cell cell = row.createCell(col);
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
}
