package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.HandoverRecordQueryDTO;
import com.zssystem.dto.HandoverRecordSaveDTO;
import com.zssystem.service.HandoverRecordService;
import com.zssystem.vo.HandoverRecordVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/handover")
@Validated
public class HandoverRecordController {

    @Autowired
    private HandoverRecordService handoverRecordService;

    @GetMapping("/list")
    public Result<PageResult<HandoverRecordVO>> getRecordList(@Validated HandoverRecordQueryDTO queryDTO) {
        IPage<HandoverRecordVO> page = handoverRecordService.getRecordList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<HandoverRecordVO> getRecordById(@PathVariable Long id) {
        HandoverRecordVO vo = handoverRecordService.getRecordById(id);
        return Result.success(vo);
    }

    /**
     * 获取交接班记录照片（新增时提交的拍照）
     */
    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getRecordPhoto(@PathVariable Long id) {
        try {
            byte[] bytes = handoverRecordService.getRecordPhoto(id);
            if (bytes == null || bytes.length == 0) {
                return ResponseEntity.notFound().build();
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "获取照片失败", e);
        }
    }

    @PostMapping
    public Result<Void> saveRecord(@Valid @RequestBody HandoverRecordSaveDTO saveDTO) {
        handoverRecordService.saveRecord(saveDTO);
        return Result.success();
    }

    /**
     * 上传交接班拍照照片（新增记录时必传，手机端调用摄像头，电脑端选择图片）
     * 返回照片路径，提交时传入 photoPath
     */
    @PostMapping("/upload-photo")
    public Result<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        try {
            String photoPath = handoverRecordService.uploadHandoverPhoto(file);
            return Result.success(photoPath);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "上传失败", e);
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        handoverRecordService.deleteRecord(id);
        return Result.success();
    }

    @GetMapping("/products")
    public Result<List<String>> getProductNames(@RequestParam Long equipmentId) {
        List<String> list = handoverRecordService.getProductNamesByEquipmentId(equipmentId);
        return Result.success(list);
    }

    /**
     * 预览交接班记录表（HTML，效果与下载 Excel 一致）
     */
    @GetMapping("/preview")
    public Result<String> previewExcel(
            @RequestParam Long equipmentId,
            @RequestParam String recordMonth) {
        try {
            String html = handoverRecordService.getPreviewHtml(equipmentId, recordMonth);
            return Result.success(html);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "预览失败", e);
        }
    }

    /**
     * 获取导出文件数量（每28条一张Excel）
     */
    @GetMapping("/export-count")
    public Result<Integer> getExportFileCount(
            @RequestParam Long equipmentId,
            @RequestParam String recordMonth) {
        int count = handoverRecordService.getExportFileCount(equipmentId, recordMonth);
        return Result.success(count);
    }

    /**
     * 导出单张 Excel（page 从 1 开始，用于分页导出多张）
     */
    @GetMapping("/export-page")
    public ResponseEntity<byte[]> exportExcelPage(
            @RequestParam Long equipmentId,
            @RequestParam String recordMonth,
            @RequestParam int page) {
        try {
            Object[] result = handoverRecordService.exportExcelPage(equipmentId, recordMonth, page);
            byte[] bytes = (byte[]) result[0];
            String fileName = (String) result[1];
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "导出失败", e);
        }
    }

    /**
     * 导出交接班记录 Excel，按设备+月份
     * 每张Excel最多28条记录，超过28条时打包为ZIP，每张Excel布局相同
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam Long equipmentId,
            @RequestParam String recordMonth) {
        try {
            Object[] result = handoverRecordService.exportExcel(equipmentId, recordMonth);
            byte[] bytes = (byte[]) result[0];
            String fileName = (String) result[1];
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            HttpHeaders headers = new HttpHeaders();
            String contentType = fileName.endsWith(".zip")
                    ? "application/zip"
                    : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "导出失败", e);
        }
    }
}
