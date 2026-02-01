package com.zssystem.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具
 */
public class QrCodeUtil {

    private static final int DEFAULT_SIZE = 300;

    /**
     * 生成二维码 PNG 图片字节数组
     * @param content 二维码内容（通常是 URL）
     * @param size 图片尺寸（宽高，像素）
     */
    public static byte[] generatePng(String content, int size) throws Exception {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("二维码内容不能为空");
        }
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
        return out.toByteArray();
    }

    /**
     * 生成二维码 PNG 图片（默认 300x300）
     */
    public static byte[] generatePng(String content) throws Exception {
        return generatePng(content, DEFAULT_SIZE);
    }
}
