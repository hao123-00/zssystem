package com.zssystem.service;

import com.zssystem.vo.EquipmentQrViewVO;

/**
 * 设备扫码查看服务
 */
public interface EquipmentQrService {

    /**
     * 获取设备扫码查看页数据（点检记录 + 启用工艺卡）
     */
    EquipmentQrViewVO getViewData(Long equipmentId);

    /**
     * 生成设备二维码图片（PNG 字节）
     * 二维码内容为扫码查看页的前端 URL
     */
    byte[] generateQrCodePng(Long equipmentId) throws Exception;

    /**
     * 获取设备当月点检表 PDF
     */
    byte[] getCheckPdf(Long equipmentId) throws Exception;

    /**
     * 获取设备启用工艺卡 PDF
     */
    byte[] getProcessFilePdf(Long equipmentId) throws Exception;
}
