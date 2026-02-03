package com.zssystem.util;

import java.nio.charset.StandardCharsets;

/**
 * HTML 转 SVG 工具
 * 将 HTML 表格内容包装为 SVG（使用 foreignObject 嵌入 HTML），便于手机端扫码查看
 */
public class HtmlToSvgUtil {

    /** A4 横向尺寸（像素，96dpi）：297mm × 210mm */
    private static final int SVG_WIDTH = 1123;
    private static final int SVG_HEIGHT = 794;

    /**
     * 将 HTML 字符串转换为 SVG 字节数组
     * 使用 SVG foreignObject 嵌入 HTML，支持表格、样式、图片等
     *
     * @param html HTML 内容（可为完整文档或片段）
     * @return SVG 字节数组（UTF-8）
     */
    public static byte[] htmlToSvg(String html) {
        if (html == null) html = "";
        String content = extractContentForSvg(html);
        String svg = buildSvgWithForeignObject(content);
        return svg.getBytes(StandardCharsets.UTF_8);
    }

    private static String extractContentForSvg(String html) {
        String trimmed = html.trim();
        String extractedStyle = "";
        String bodyContent;

        if (trimmed.toLowerCase().startsWith("<!doctype") || trimmed.toLowerCase().startsWith("<html")) {
            int styleStart = html.indexOf("<style>");
            if (styleStart < 0) styleStart = html.indexOf("<style ");
            if (styleStart >= 0) {
                int styleEnd = html.indexOf("</style>", styleStart);
                if (styleEnd > styleStart) {
                    extractedStyle = html.substring(styleStart, styleEnd + 8);
                }
            }
            int bodyStart = html.toLowerCase().indexOf("<body");
            if (bodyStart >= 0) {
                int contentStart = html.indexOf(">", bodyStart) + 1;
                int bodyEnd = html.toLowerCase().indexOf("</body>");
                bodyContent = contentStart > 0 && bodyEnd > contentStart
                        ? html.substring(contentStart, bodyEnd).trim()
                        : html;
            } else {
                bodyContent = html;
            }
        } else {
            int styleStart = html.indexOf("<style>");
            if (styleStart < 0) styleStart = html.indexOf("<style ");
            if (styleStart >= 0) {
                int styleEnd = html.indexOf("</style>", styleStart);
                if (styleEnd > styleStart) {
                    extractedStyle = html.substring(styleStart, styleEnd + 8);
                    bodyContent = (html.substring(0, styleStart).trim() + html.substring(styleEnd + 8).trim()).trim();
                } else {
                    bodyContent = html;
                }
            } else {
                bodyContent = html;
            }
        }

        String baseStyle = """
            body, html { margin: 0; padding: 0; font-family: 'NotoSansSC', 'Microsoft YaHei', SimSun, sans-serif; }
            table { width: 100%; border-collapse: collapse; }
            """;
        String styleBlock = extractedStyle.isEmpty() ? baseStyle
                : baseStyle + extractedStyle.replace("<style>", "").replace("</style>", "").replaceFirst("<style[^>]*>", "");

        return "<style>" + styleBlock + "</style><div xmlns=\"http://www.w3.org/1999/xhtml\" style=\"width:100%;height:100%;overflow:auto;box-sizing:border-box;\">" + bodyContent + "</div>";
    }

    private static String buildSvgWithForeignObject(String xhtmlContent) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" " +
                "width=\"" + SVG_WIDTH + "\" height=\"" + SVG_HEIGHT + "\" " +
                "viewBox=\"0 0 " + SVG_WIDTH + " " + SVG_HEIGHT + "\">\n" +
                "  <foreignObject x=\"0\" y=\"0\" width=\"100%\" height=\"100%\">\n" +
                "    <div xmlns=\"http://www.w3.org/1999/xhtml\" style=\"width:" + SVG_WIDTH + "px;height:" + SVG_HEIGHT + "px;margin:0;padding:4px;box-sizing:border-box;\">\n" +
                "      " + xhtmlContent + "\n" +
                "    </div>\n" +
                "  </foreignObject>\n" +
                "</svg>";
    }
}
