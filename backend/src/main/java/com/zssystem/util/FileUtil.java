package com.zssystem.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件工具类
 */
public class FileUtil {

    /**
     * 创建目录（如果不存在）
     */
    public static void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 检查是否为Excel文件
     */
    public static boolean isExcelFile(String filename) {
        String ext = getFileExtension(filename);
        return "xls".equals(ext) || "xlsx".equals(ext);
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0 B";
        }
        
        final long k = 1024;
        final String[] sizes = {"B", "KB", "MB", "GB", "TB"};
        final int i = (int) (Math.log(bytes) / Math.log(k));
        
        return String.format("%.2f %s", bytes / Math.pow(k, i), sizes[i]);
    }

    /**
     * 检查文件是否存在
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 获取文件大小
     */
    public static long getFileSize(String filePath) throws IOException {
        return Files.size(Paths.get(filePath));
    }
}
