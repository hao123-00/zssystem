package com.zssystem.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 交接班记录表 Excel 转 HTML 预览
 * 预览效果与下载 Excel 一致
 */
public class HandoverRecordExcelToHtmlConverter {

    private static final String STYLE = """
        <style>
          .hr-embed-root { font-family: Calibri, Arial, "Microsoft YaHei", SimSun, sans-serif; font-size: 11pt; }
          .hr-embed-root .hr-preview-wrap { overflow-x: auto; -webkit-overflow-scrolling: touch; }
          .hr-embed-root table { border-collapse: collapse; table-layout: fixed; font-size: 11pt; }
          .hr-embed-root tr { height: 28px; }
          .hr-embed-root td { border: 1px solid #000; padding: 2px 4px; vertical-align: middle; overflow: hidden; height: 28px; box-sizing: border-box; }
          .hr-embed-root .no-border { border: none !important; }
          .hr-embed-root .wrap { word-wrap: break-word; white-space: pre-wrap; }
          .hr-embed-root .center { text-align: center; }
          .hr-embed-root .left { text-align: left; }
          .hr-embed-root .right { text-align: right; }
        </style>
        """;

    /** 供多页拼接时复用，仅样式块（含 <style> 标签） */
    public static String getStyleBlock() {
        return STYLE;
    }

    private static final int MAX_COLS = 15;

    public static String convertToHtml(InputStream inputStream) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            return convertWorkbookToHtml(workbook);
        }
    }

    /**
     * 将单张 Excel 转为 HTML 片段（仅表格部分，无外层样式），用于多页拼接
     */
    public static String convertToHtmlFragment(InputStream inputStream) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            return convertWorkbookToHtmlFragment(workbook);
        }
    }

    private static String convertWorkbookToHtml(Workbook workbook) throws IOException {
        return "<div class=\"hr-embed-root\">" + STYLE + convertWorkbookToHtmlFragment(workbook) + "</div>";
    }

    private static String convertWorkbookToHtmlFragment(Workbook workbook) throws IOException {
        StringBuilder html = new StringBuilder();

        Sheet sheet = workbook.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();

        List<CellRangeAddress> mergedRegions = new ArrayList<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            mergedRegions.add(sheet.getMergedRegion(i));
        }

        html.append("<div class=\"hr-preview-wrap\"><table>");

        for (int r = 0; r <= lastRowNum; r++) {
            Row row = sheet.getRow(r);
            html.append("<tr>");
            for (int c = 0; c < MAX_COLS; c++) {
                CellRangeAddress merge = findMergedRegion(mergedRegions, r, c);
                if (merge != null) {
                    int fr = merge.getFirstRow();
                    int fc = merge.getFirstColumn();
                    if (r != fr || c != fc) continue;
                }

                int colspan = 1;
                int rowspan = 1;
                if (merge != null) {
                    colspan = merge.getLastColumn() - merge.getFirstColumn() + 1;
                    rowspan = merge.getLastRow() - merge.getFirstRow() + 1;
                }

                Cell cell = row != null ? row.getCell(c) : null;
                String cellValue = getCellValueAsString(cell);
                String alignClass = "left";
                String extraClass = "";
                if (cell != null) {
                    CellStyle style = cell.getCellStyle();
                    HorizontalAlignment ha = style.getAlignment();
                    if (ha == HorizontalAlignment.CENTER) alignClass = "center";
                    else if (ha == HorizontalAlignment.RIGHT) alignClass = "right";
                    // 第一行无框线
                    if (r == 0) extraClass = " no-border";
                }
                html.append("<td");
                if (colspan > 1) html.append(" colspan=\"").append(colspan).append("\"");
                if (rowspan > 1) html.append(" rowspan=\"").append(rowspan).append("\"");
                html.append(" class=\"").append(alignClass).append(" wrap").append(extraClass).append("\">");
                html.append(escapeHtml(cellValue != null ? cellValue : ""));
                html.append("</td>");
            }
            html.append("</tr>");
        }

        html.append("</table></div>");
        return html.toString();
    }

    private static CellRangeAddress findMergedRegion(List<CellRangeAddress> regions, int row, int col) {
        for (CellRangeAddress r : regions) {
            if (row >= r.getFirstRow() && row <= r.getLastRow()
                    && col >= r.getFirstColumn() && col <= r.getLastColumn()) {
                return r;
            }
        }
        return null;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        yield cell.getLocalDateTimeCellValue().toString();
                    } catch (Exception e) {
                        yield String.valueOf(cell.getNumericCellValue());
                    }
                }
                double n = cell.getNumericCellValue();
                yield n == (long) n ? String.valueOf((long) n) : String.valueOf(n);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    try {
                        yield cell.getStringCellValue();
                    } catch (Exception e2) {
                        yield "";
                    }
                }
            }
            default -> "";
        };
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
