package com.zssystem.controller;

import com.zssystem.common.Result;
import com.zssystem.service.EquipmentQrService;
import com.zssystem.service.EquipmentCheckService;
import com.zssystem.service.ProcessFileService;
import com.zssystem.vo.EquipmentQrViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 设备扫码查看 - 公开接口（微信扫码无需登录）
 */
@RestController
@RequestMapping("/api/qr/equipment")
public class EquipmentQrController {

    @Autowired
    private EquipmentQrService equipmentQrService;

    @Autowired
    private EquipmentCheckService equipmentCheckService;

    @Autowired
    private ProcessFileService processFileService;

    /**
     * 获取设备扫码查看页数据（点检记录 + 启用工艺卡信息）
     */
    @GetMapping("/{equipmentId}/view")
    public Result<EquipmentQrViewVO> getViewData(@PathVariable Long equipmentId) {
        EquipmentQrViewVO data = equipmentQrService.getViewData(equipmentId);
        return Result.success(data);
    }

    /**
     * 获取设备当月点检表 HTML 预览（公开）
     */
    @GetMapping("/{equipmentId}/check/preview")
    public Result<String> getCheckPreview(@PathVariable Long equipmentId) {
        try {
            EquipmentQrViewVO data = equipmentQrService.getViewData(equipmentId);
            String html = equipmentCheckService.getPreviewHtml(equipmentId, data.getCheckMonth());
            return Result.success(html);
        } catch (IOException e) {
            return Result.error(e.getMessage() != null ? e.getMessage() : "预览失败");
        }
    }

    /**
     * 获取设备启用的工艺卡 HTML 预览（仅当该设备有启用工艺卡时）
     */
    @GetMapping("/{equipmentId}/process-file/preview")
    public Result<String> getProcessFilePreview(@PathVariable Long equipmentId) {
        EquipmentQrViewVO data = equipmentQrService.getViewData(equipmentId);
        if (data.getEnabledProcessFile() == null) {
            return Result.error("该机台暂无启用的工艺卡");
        }
        try {
            String html = processFileService.getPreviewHtml(data.getEnabledProcessFile().getId());
            return Result.success(html);
        } catch (IOException e) {
            return Result.error(e.getMessage() != null ? e.getMessage() : "预览失败");
        }
    }

    /**
     * 获取设备当月点检表 PDF（公开，扫码后查看）
     */
    @GetMapping("/{equipmentId}/check/pdf")
    public ResponseEntity<byte[]> getCheckPdf(@PathVariable Long equipmentId) {
        try {
            byte[] pdf = equipmentQrService.getCheckPdf(equipmentId);
            String fileName = "点检表_" + equipmentQrService.getViewData(equipmentId).getCheckMonth() + ".pdf";
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            headers.add("Content-Disposition", "inline; filename*=UTF-8''" + encoded);
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取设备启用的工艺卡 PDF（仅当该设备有启用工艺卡时，公开，扫码后查看）
     */
    @GetMapping("/{equipmentId}/process-file/pdf")
    public ResponseEntity<byte[]> getProcessFilePdf(@PathVariable Long equipmentId) {
        try {
            EquipmentQrViewVO data = equipmentQrService.getViewData(equipmentId);
            if (data.getEnabledProcessFile() == null) {
                return ResponseEntity.badRequest().build();
            }
            byte[] pdf = equipmentQrService.getProcessFilePdf(equipmentId);
            String fileName = (data.getEnabledProcessFile().getFileName() != null ? data.getEnabledProcessFile().getFileName() : "工艺卡")
                    .replaceAll("\\.(xls|xlsx)?$", "") + ".pdf";
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            headers.add("Content-Disposition", "inline; filename*=UTF-8''" + encoded);
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 下载设备启用的工艺卡 Excel（仅当该设备有启用工艺卡时）
     */
    @GetMapping("/{equipmentId}/process-file/download")
    public ResponseEntity<byte[]> downloadProcessFile(@PathVariable Long equipmentId) {
        EquipmentQrViewVO data = equipmentQrService.getViewData(equipmentId);
        if (data.getEnabledProcessFile() == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            byte[] content = processFileService.downloadProcessFile(data.getEnabledProcessFile().getId());
            String fileName = data.getEnabledProcessFile().getFileName();
            if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
                fileName = (fileName != null ? fileName.replaceAll("\\.(xls|xlsx)?$", "") : "工艺卡") + ".xlsx";
            }
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            return ResponseEntity.ok().headers(headers).body(content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
