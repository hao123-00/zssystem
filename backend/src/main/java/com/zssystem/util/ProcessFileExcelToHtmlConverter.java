package com.zssystem.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 工艺文件 Excel 转 HTML 预览
 * 将 Excel 转为 HTML 以在浏览器中实现与下载 Excel 一致的预览效果
 */
public class ProcessFileExcelToHtmlConverter {

    /** 受控章在 Excel 中的区域：L24:P27（列11-15，行23-26，0-based） */
    private static final int SEAL_COL1 = 11;
    private static final int SEAL_ROW1 = 23;
    private static final int SEAL_COL2 = 15;
    private static final int SEAL_ROW2 = 26;

    private static final String STYLE = """
        <style>
          body { font-family: "Microsoft YaHei", SimSun, sans-serif; font-size: 11pt; margin: 12px; }
          .pf-preview-wrap { position: relative; }
          table { border-collapse: collapse; table-layout: fixed; }
          td { border: 1px solid #000; padding: 2px 4px; vertical-align: middle; overflow: hidden; }
          .wrap { word-wrap: break-word; white-space: pre-wrap; }
          .center { text-align: center; }
          .left { text-align: left; }
          .right { text-align: right; }
          .vertical { writing-mode: vertical-rl; text-orientation: mixed; transform: rotate(-90deg); white-space: nowrap; }
          td img:not(.pf-seal-img) { max-width: 100%; height: auto; display: block; }
          .pf-seal-overlay { position: absolute; pointer-events: none; z-index: 10; }
          .pf-seal-overlay img { max-width: 100%; height: auto; display: block; }
        </style>
        """;

    /**
     * 将 Excel 文件转换为 HTML
     */
    public static String convertToHtml(String excelFilePath) throws IOException {
        File file = new File(excelFilePath);
        if (!file.exists()) {
            throw new IOException("Excel 文件不存在: " + excelFilePath);
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return convertToHtml(fis);
        }
    }

    /**
     * 将 Excel 输入流转换为 HTML
     */
    public static String convertToHtml(InputStream inputStream) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            return convertWorkbookToHtml(workbook);
        }
    }

    private static String convertWorkbookToHtml(Workbook workbook) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">").append(STYLE).append("</head><body>");

        Sheet sheet = workbook.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();
        int maxCols = 16; // 工艺文件 A-P 列

        // 收集合并区域
        List<CellRangeAddress> mergedRegions = new ArrayList<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            mergedRegions.add(sheet.getMergedRegion(i));
        }

        // 收集图片：位置 -> base64；受控章单独收集，预览时浮于文字上方
        Map<String, String> cellImages = new HashMap<>();
        String sealImageData = null;
        if (sheet instanceof XSSFSheet xssfSheet) {
            XSSFDrawing drawing = xssfSheet.getDrawingPatriarch();
            if (drawing != null) {
                for (org.apache.poi.ss.usermodel.Shape shape : drawing.getShapes()) {
                    if (shape instanceof XSSFPicture picture) {
                        XSSFClientAnchor anchor = (XSSFClientAnchor) picture.getAnchor();
                        if (anchor != null) {
                            int row1 = anchor.getRow1();
                            int col1 = anchor.getCol1();
                            int row2 = anchor.getRow2();
                            int col2 = anchor.getCol2();
                            byte[] data = picture.getPictureData().getData();
                            String mime = picture.getPictureData().getMimeType();
                            String base64 = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(data);
                            if (row1 == SEAL_ROW1 && col1 == SEAL_COL1 && row2 == SEAL_ROW2 && col2 == SEAL_COL2) {
                                sealImageData = base64;
                            } else {
                                cellImages.put(row1 + "_" + col1, base64);
                            }
                        }
                    }
                }
            }
        }

        html.append("<div class=\"pf-preview-wrap\"><table>");

        for (int r = 0; r <= lastRowNum; r++) {
            Row row = sheet.getRow(r);
            html.append("<tr>");
            for (int c = 0; c < maxCols; c++) {
                CellRangeAddress merge = findMergedRegion(mergedRegions, r, c);
                if (merge != null) {
                    int fr = merge.getFirstRow();
                    int fc = merge.getFirstColumn();
                    if (r != fr || c != fc) {
                        continue; // 合并区域的非左上角单元格，由左上角 cell 的 rowspan/colspan 覆盖
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
                String extraClass = "";
                if (cell != null) {
                    CellStyle style = cell.getCellStyle();
                    HorizontalAlignment ha = style.getAlignment();
                    if (ha == HorizontalAlignment.CENTER) alignClass = "center";
                    else if (ha == HorizontalAlignment.RIGHT) alignClass = "right";
                    if (style.getRotation() != 0) extraClass = " vertical";
                }

                html.append("<td");
                if (colspan > 1) html.append(" colspan=\"").append(colspan).append("\"");
                if (rowspan > 1) html.append(" rowspan=\"").append(rowspan).append("\"");
                html.append(" class=\"").append(alignClass).append(extraClass).append(" wrap\">");

                String imgKey = r + "_" + c;
                if (cellImages.containsKey(imgKey)) {
                    html.append("<img src=\"").append(cellImages.get(imgKey)).append("\" alt=\"\" />");
                }
                html.append(escapeHtml(cellValue));
                html.append("</td>");
            }
            html.append("</tr>");
        }

        html.append("</table>");
        if (sealImageData != null) {
            double leftPct = 100.0 * SEAL_COL1 / maxCols;
            double topPct = lastRowNum > 0 ? 100.0 * SEAL_ROW1 / (lastRowNum + 1) : 0;
            double widthPct = 100.0 * (SEAL_COL2 - SEAL_COL1 + 1) / maxCols;
            double heightPct = lastRowNum > 0 ? 100.0 * (SEAL_ROW2 - SEAL_ROW1 + 1) / (lastRowNum + 1) : 10;
            html.append("<div class=\"pf-seal-overlay\" style=\"left:")
                .append(String.format("%.1f", leftPct)).append("%;top:")
                .append(String.format("%.1f", topPct)).append("%;width:")
                .append(String.format("%.1f", widthPct)).append("%;height:")
                .append(String.format("%.1f", heightPct)).append("%;\">")
                .append("<img class=\"pf-seal-img\" src=\"").append(sealImageData).append("\" alt=\"受控章\" /></div>");
        }
        html.append("</div></body></html>");
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
