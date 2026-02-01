package com.zssystem.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
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

    private static final String STYLE_WEB = """
        <style>
          @page { size: A4 landscape; margin: 5mm; }
          body { font-family: "Microsoft YaHei", SimSun, sans-serif; font-size: 11pt; margin: 12px; }
          .pf-preview-wrap { position: relative; }
          table { border-collapse: collapse; table-layout: fixed; }
          td { border: 1px solid #000; padding: 2px 4px; vertical-align: middle; overflow: hidden; }
          .wrap { word-wrap: break-word; white-space: pre-wrap; }
          .center { text-align: center; }
          .left { text-align: left; }
          .right { text-align: right; }
          .vertical { writing-mode: vertical-rl; text-orientation: mixed; transform: rotate(-90deg); white-space: nowrap; }
          td img { max-width: 100%; height: auto; display: block; }
          .pf-seal-cell { position: relative; }
          .pf-seal-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; pointer-events: none; z-index: 5; }
          .pf-seal-overlay img { max-width: 80%; max-height: 80%; object-fit: contain; }
        </style>
        """;
    /** PDF 用样式：每行等高铺满一页，A4 横向内容区约 575pt */
    private static final double PDF_TABLE_HEIGHT_PT = 575;

    private static final String STYLE_PDF = """
        <style>
          @page { size: A4 landscape; margin: 1mm; }
          body { font-family: 'NotoSansSC', sans-serif; font-size: 4pt; margin: 0; padding: 0; line-height: 1.1; }
          table { border-collapse: collapse; table-layout: fixed; page-break-inside: avoid; font-size: 4pt; width: 100%; }
          td { border: 1px solid #000; padding: 0; vertical-align: middle; line-height: 1.1; }
          .wrap { word-wrap: break-word; white-space: pre-wrap; }
          .center { text-align: center; }
          .left { text-align: left; }
          .right { text-align: right; }
          .pf-preview-wrap { width: 100%; max-width: 100%; }
          td img { max-width: 100%; max-height: 16px; }
          td img.pf-seal-img { max-height: 40px; max-width: 80px; }
          .pf-seal-cell { position: relative; }
          .pf-seal-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; pointer-events: none; z-index: 5; }
          .pf-seal-overlay img { max-width: 80%; max-height: 80%; object-fit: contain; }
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
        try (Workbook workbook = WorkbookFactory.create(file)) {
            return convertWorkbookToHtml(workbook, false);
        }
    }

    /**
     * 将 Excel 文件转换为 PDF 友好的 HTML
     */
    public static String convertToHtmlForPdf(String excelFilePath) throws IOException {
        File file = new File(excelFilePath);
        if (!file.exists()) {
            throw new IOException("Excel 文件不存在: " + excelFilePath);
        }
        try (Workbook workbook = WorkbookFactory.create(file)) {
            return convertWorkbookToHtml(workbook, true);
        }
    }

    /**
     * 将 Excel 输入流转换为 HTML
     */
    public static String convertToHtml(InputStream inputStream) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            return convertWorkbookToHtml(workbook, false);
        }
    }

    /**
     * 转换为 PDF 友好的 HTML（无 position:absolute 等复杂布局，便于 openhtmltopdf 渲染）
     */
    public static String convertToHtmlForPdf(InputStream inputStream) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            return convertWorkbookToHtml(workbook, true);
        }
    }

    private static String convertWorkbookToHtml(Workbook workbook, boolean forPdf) throws IOException {
        StringBuilder html = new StringBuilder();
        String style = forPdf ? STYLE_PDF : STYLE_WEB;
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">").append(style).append("</head><body>");

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

        int rowCount = lastRowNum + 1;
        double rowHeightPt = forPdf && rowCount > 0 ? PDF_TABLE_HEIGHT_PT / rowCount : 0;

        for (int r = 0; r <= lastRowNum; r++) {
            Row row = sheet.getRow(r);
            if (forPdf && rowHeightPt > 0) {
                html.append("<tr style=\"height:").append(String.format("%.1f", rowHeightPt)).append("pt\">");
            } else {
                html.append("<tr>");
            }
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
                    CellStyle cellStyle = cell.getCellStyle();
                    HorizontalAlignment ha = cellStyle.getAlignment();
                    if (ha == HorizontalAlignment.CENTER) alignClass = "center";
                    else if (ha == HorizontalAlignment.RIGHT) alignClass = "right";
                    if (!forPdf && cellStyle.getRotation() != 0) extraClass = " vertical";
                }

                String imgKey = r + "_" + c;
                boolean isSealCell = sealImageData != null && r == SEAL_ROW1 && c == SEAL_COL1;
                String cellClasses = alignClass + " " + extraClass + " wrap" + (isSealCell ? " pf-seal-cell" : "");

                html.append("<td");
                if (colspan > 1) html.append(" colspan=\"").append(colspan).append("\"");
                if (rowspan > 1) html.append(" rowspan=\"").append(rowspan).append("\"");
                html.append(" class=\"").append(cellClasses.trim()).append("\">");

                if (isSealCell) {
                    html.append("<div class=\"pf-seal-overlay\"><img class=\"pf-seal-img\" src=\"")
                        .append(sealImageData).append("\" alt=\"受控章\" /></div>");
                }
                if (cellImages.containsKey(imgKey)) {
                    html.append("<img src=\"").append(cellImages.get(imgKey)).append("\" alt=\"\" />");
                }
                html.append(escapeHtml(cellValue));
                html.append("</td>");
            }
            html.append("</tr>");
        }

        html.append("</table></div></body></html>");
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
