package com.zssystem.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备点检表 Excel 转 HTML 预览
 * 将点检表 Excel 转为 HTML，预览效果与下载 Excel 一致
 */
public class EquipmentCheckExcelToHtmlConverter {

    /** 与 Excel 默认字体一致（Calibri 11pt），使 √ × 符号显示效果与下载的 Excel 相同；列宽、行高与 Excel 一致便于扫码预览 */
    private static final String STYLE = """
        <style>
          .ec-embed-root { font-family: Calibri, Arial, "Microsoft YaHei", SimSun, sans-serif; font-size: 11pt; }
          .ec-embed-root .ec-preview-wrap { overflow-x: auto; -webkit-overflow-scrolling: touch; }
          .ec-embed-root table { border-collapse: collapse; table-layout: fixed; font-size: 11pt; width: 100%; }
          .ec-embed-root td { border: 1px solid #000; padding: 2px 4px; vertical-align: middle; overflow: hidden; box-sizing: border-box; }
          .ec-embed-root .wrap { word-wrap: break-word; white-space: pre-wrap; }
          .ec-embed-root .center { text-align: center; }
          .ec-embed-root .left { text-align: left; }
          .ec-embed-root .right { text-align: right; }
          .ec-embed-root tr.ec-row-title { height: 50px; }
          .ec-embed-root tr.ec-row-header { height: 32px; }
          .ec-embed-root tr.ec-row-data { height: 28px; }
          .ec-embed-root tr.ec-row-remark-label { height: 100px; }
          .ec-embed-root tr.ec-row-remark { height: 32px; }
        </style>
        """;

    /** PDF 用：每行等高铺满一页，A4 横向内容区约 575pt */
    private static final double PDF_TABLE_HEIGHT_PT = 575;

    private static final String STYLE_PDF = """
        <style>
          .ec-embed-root { font-size: 4pt; line-height: 1.1; width: 100%; }
          .ec-embed-root .ec-preview-wrap { overflow: visible; width: 100%; }
          .ec-embed-root table { border-collapse: collapse; table-layout: fixed; font-size: 4pt; width: 100%; }
          .ec-embed-root td { border: 1px solid #000; padding: 0; vertical-align: middle; overflow: hidden; line-height: 1.1; }
          .ec-embed-root .wrap { word-wrap: break-word; white-space: pre-wrap; }
          .ec-embed-root .center { text-align: center; }
          .ec-embed-root .left { text-align: left; }
          .ec-embed-root .right { text-align: right; }
        </style>
        """;

    /** 点检表列数：分类(0) + 项目(1-12合并) + 30日(13-42) = 43 */
    private static final int MAX_COLS = 43;

    public static String convertToHtml(InputStream inputStream) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            return convertWorkbookToHtml(workbook, false);
        }
    }

    /**
     * 转换为 PDF 用 HTML（√ × 使用内联 SVG，与下载 Excel 样式一致）
     */
    public static String convertToHtmlForPdf(InputStream inputStream) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            return convertWorkbookToHtml(workbook, true);
        }
    }

    /** 对号 √ 的 SVG（PDF 单页模式用较小尺寸） */
    private static final String SVG_CHECK = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"5\" height=\"5\" viewBox=\"0 0 12 12\" style=\"vertical-align:middle;display:inline-block\"><path d=\"M2 6 L5 9 L10 2\" stroke=\"#000\" stroke-width=\"1\" fill=\"none\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/></svg>";
    /** 叉号 × 的 SVG（PDF 单页模式用较小尺寸） */
    private static final String SVG_CROSS = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"5\" height=\"5\" viewBox=\"0 0 12 12\" style=\"vertical-align:middle;display:inline-block\"><path d=\"M2 2 L10 10 M10 2 L2 10\" stroke=\"#000\" stroke-width=\"1\" fill=\"none\" stroke-linecap=\"round\"/></svg>";

    /**
     * 转换为可嵌入页面的 HTML 片段（style + 内容），便于在 div 中渲染
     * @param forPdf true 时 √ × 用 SVG 替代，与 Excel 样式一致
     */
    private static String convertWorkbookToHtml(Workbook workbook, boolean forPdf) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"ec-embed-root\">").append(forPdf ? STYLE_PDF : STYLE);

        Sheet sheet = workbook.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();

        List<CellRangeAddress> mergedRegions = new ArrayList<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            mergedRegions.add(sheet.getMergedRegion(i));
        }

        html.append("<div class=\"ec-preview-wrap\"><table>");
        if (!forPdf) {
            try {
                appendColgroup(html, sheet);
            } catch (Exception e) {
                // 部分环境或旧版 Excel 列宽读取可能异常，跳过 colgroup 保证表格仍可显示
            }
        }

        int rowCount = lastRowNum + 1;
        double rowHeightPt = forPdf && rowCount > 0 ? PDF_TABLE_HEIGHT_PT / rowCount : 0;

        for (int r = 0; r <= lastRowNum; r++) {
            Row row = sheet.getRow(r);
            if (forPdf && rowHeightPt > 0) {
                html.append("<tr style=\"height:").append(String.format("%.1f", rowHeightPt)).append("pt\">");
            } else if (!forPdf) {
                String rowClass = getRowClass(r, lastRowNum);
                html.append("<tr class=\"").append(rowClass).append("\">");
            } else {
                html.append("<tr>");
            }
            for (int c = 0; c < MAX_COLS; c++) {
                CellRangeAddress merge = findMergedRegion(mergedRegions, r, c);
                if (merge != null) {
                    int fr = merge.getFirstRow();
                    int fc = merge.getFirstColumn();
                    if (r != fr || c != fc) {
                        continue;
                    }
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
                if (cell != null) {
                    CellStyle style = cell.getCellStyle();
                    HorizontalAlignment ha = style.getAlignment();
                    if (ha == HorizontalAlignment.CENTER) alignClass = "center";
                    else if (ha == HorizontalAlignment.RIGHT) alignClass = "right";
                }
                html.append("<td");
                if (colspan > 1) html.append(" colspan=\"").append(colspan).append("\"");
                if (rowspan > 1) html.append(" rowspan=\"").append(rowspan).append("\"");
                html.append(" class=\"").append(alignClass).append(" wrap\">");
                html.append(renderCellValue(cellValue, forPdf));
                html.append("</td>");
            }
            html.append("</tr>");
        }

        html.append("</table></div></div>");
        return html.toString();
    }

    /** Excel 列宽单位转 px：1 单位 = 1/256 字符宽，Calibri 11pt 约 (width/256)*7+12 px；单列最大 2000px 避免异常值 */
    private static int excelWidthToPx(int excelWidth) {
        int px = Math.max(10, (int) ((excelWidth / 256.0) * 7 + 12));
        return Math.min(px, 2000);
    }

    private static void appendColgroup(StringBuilder html, Sheet sheet) {
        html.append("<colgroup>");
        for (int c = 0; c < MAX_COLS; c++) {
            int px = 40;
            try {
                int w = sheet.getColumnWidth(c);
                px = excelWidthToPx(w);
            } catch (Exception ignored) {
                // 单列读取失败时使用默认宽度
            }
            html.append("<col style=\"width:").append(px).append("px\"/>");
        }
        html.append("</colgroup>");
    }

    /** 与 Excel 行高一致：标题 50pt、表头 32pt、数据 28pt、备注 32pt */
    private static String getRowClass(int rowIndex, int lastRowNum) {
        if (rowIndex == 0) return "ec-row-title";
        if (rowIndex >= 1 && rowIndex <= 3) return "ec-row-header";
        if (rowIndex >= 4 && rowIndex <= 19) return "ec-row-data";
        if (rowIndex == 20) return "ec-row-remark-label";
        return "ec-row-remark";
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

    private static String renderCellValue(String cellValue, boolean forPdf) {
        if (cellValue == null) return "";
        if (forPdf) {
            String t = cellValue.trim();
            if ("√".equals(t)) return SVG_CHECK;
            if ("×".equals(t)) return SVG_CROSS;
        }
        return escapeHtml(cellValue);
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
