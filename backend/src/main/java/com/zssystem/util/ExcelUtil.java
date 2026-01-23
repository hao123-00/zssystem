package com.zssystem.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel导出工具类
 */
public class ExcelUtil {
    
    /**
     * 导出Excel（通用方法）
     * @param data 数据列表
     * @param fileName 文件名（不含扩展名）
     * @param sheetName Sheet名称
     * @param clazz 数据类（用于EasyExcel自动生成表头）
     */
    public static <T> void exportExcel(List<T> data, String fileName, String sheetName, Class<T> clazz) {
        try {
            HttpServletResponse response = getResponse();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
            
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(sheetName)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 自动列宽
                    .doWrite(data);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }
    
    /**
     * 导出Excel（支持自定义WriteHandler）
     * @param data 数据列表
     * @param fileName 文件名（不含扩展名）
     * @param sheetName Sheet名称
     * @param clazz 数据类（用于EasyExcel自动生成表头）
     * @param writeHandlers 自定义WriteHandler列表
     */
    @SafeVarargs
    public static <T> void exportExcel(List<T> data, String fileName, String sheetName, 
                                      Class<T> clazz, com.alibaba.excel.write.handler.WriteHandler... writeHandlers) {
        try {
            HttpServletResponse response = getResponse();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
            
            com.alibaba.excel.write.builder.ExcelWriterBuilder builder = EasyExcel.write(response.getOutputStream(), clazz)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()); // 自动列宽
            
            // 注册自定义WriteHandler
            for (com.alibaba.excel.write.handler.WriteHandler handler : writeHandlers) {
                builder.registerWriteHandler(handler);
            }
            
            builder.sheet(sheetName).doWrite(data);
        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }
    
    private static HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("无法获取HttpServletResponse");
        }
        return attributes.getResponse();
    }
    
    /**
     * 生成导出文件名
     */
    public static String generateFileName(String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return prefix + "_" + date;
    }
}
