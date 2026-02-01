package com.zssystem.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * HTML 转 PDF 工具
 * 通过紧凑样式使整个表格在一页内完整显示，不拆分
 */
public class HtmlToPdfUtil {

    private static final String FONT_FAMILY = "NotoSansSC";

    /**
     * 将 HTML 字符串转换为 PDF 字节数组
     * 使用紧凑样式使整表在一页内完整显示
     * @param html HTML 内容（可为片段，将自动包装为完整文档）
     */
    public static byte[] htmlToPdf(String html) throws IOException {
        String fullHtml = wrapForPdf(html);
        fullHtml = forceFontFamily(fullHtml);
        Document jsoupDoc = Jsoup.parse(fullHtml, "UTF-8");
        jsoupDoc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useSVGDrawer(new BatikSVGDrawer());
            if (HtmlToPdfUtil.class.getResource("/fonts/NotoSansSC-Regular.ttf") != null) {
                builder.useFont(() -> HtmlToPdfUtil.class.getResourceAsStream("/fonts/NotoSansSC-Regular.ttf"), FONT_FAMILY);
            }
            builder.withW3cDocument(w3cDoc, "file:///");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        }
    }

    /** 强制使用中文字体，避免乱码 */
    private static String forceFontFamily(String html) {
        return html.replaceAll("font-family:\\s*[^;}]+", "font-family: '" + FONT_FAMILY + "', sans-serif");
    }

    /**
     * 包装 HTML 片段为完整文档（PDF 渲染需要）
     * 已是完整文档则直接返回
     * 若片段含 body 内的 style，提取到 head，避免 openhtmltopdf 将 style 内容当文本渲染
     */
    private static String wrapForPdf(String html) {
        if (html == null) html = "";
        String trimmed = html.trim();
        if (trimmed.toLowerCase().startsWith("<!doctype") || trimmed.toLowerCase().startsWith("<html")) {
            return html;
        }
        String bodyContent = html;
        String extractedStyle = "";
        int styleStart = html.indexOf("<style>");
        if (styleStart < 0) styleStart = html.indexOf("<style ");
        if (styleStart >= 0) {
            int styleEnd = html.indexOf("</style>", styleStart);
            if (styleEnd > styleStart) {
                extractedStyle = html.substring(styleStart, styleEnd + 8);
                bodyContent = (html.substring(0, styleStart).trim() + " " + html.substring(styleEnd + 8).trim()).trim();
            }
        }
        String baseStyle = """
            @page { size: A4 landscape; margin: 1.5mm; }
            body { margin: 0; padding: 0; font-family: 'NotoSansSC', sans-serif; font-size: 4pt; line-height: 1.1; }
            table { page-break-inside: avoid; width: 100%; }
            .ec-embed-root, .pf-preview-wrap { page-break-inside: avoid; width: 100%; max-width: 100%; }
            """;
        return """
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
            <meta charset="UTF-8"/>
            <style>
            """ + baseStyle + extractedStyle.replace("<style>", "").replace("</style>", "").replaceFirst("<style[^>]*>", "") + """
            </style>
            </head>
            <body>
            <div class="pdf-single-page" style="width:100%;max-width:100%;">
            """ + bodyContent + """
            </div>
            </body>
            </html>
            """;
    }

}
