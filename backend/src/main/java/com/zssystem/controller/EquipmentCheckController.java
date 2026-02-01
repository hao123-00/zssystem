package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.EquipmentCheckQueryDTO;
import com.zssystem.dto.EquipmentCheckSaveDTO;
import com.zssystem.service.EquipmentCheckService;
import com.zssystem.vo.EquipmentCheckVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/equipment/check")
@Validated
public class EquipmentCheckController {

    @Autowired
    private EquipmentCheckService checkService;

    @GetMapping("/list")
    public Result<PageResult<EquipmentCheckVO>> getCheckList(@Validated EquipmentCheckQueryDTO queryDTO) {
        IPage<EquipmentCheckVO> page = checkService.getCheckList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<EquipmentCheckVO> getCheckById(@PathVariable Long id) {
        EquipmentCheckVO vo = checkService.getCheckById(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Void> saveCheck(@Valid @RequestBody EquipmentCheckSaveDTO saveDTO) {
        checkService.saveCheck(saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCheck(@PathVariable Long id) {
        checkService.deleteCheck(id);
        return Result.success();
    }

    /**
     * 导出某设备某月30天点检记录为 Excel
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCheckExcel(
            @RequestParam Long equipmentId,
            @RequestParam String checkMonth) {
        try {
            byte[] bytes = checkService.exportCheckExcel(equipmentId, checkMonth);
            String fileName = "点检表_" + checkMonth + ".xlsx";
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", encoded);
            headers.setContentLength(bytes.length);
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "导出失败", e);
        }
    }

    /**
     * 预览某设备某月30天点检表（HTML，效果与下载 Excel 一致）
     */
    @GetMapping("/export/preview")
    public Result<String> previewCheckExcel(
            @RequestParam Long equipmentId,
            @RequestParam String checkMonth) {
        try {
            String html = checkService.getPreviewHtml(equipmentId, checkMonth);
            return Result.success(html);
        } catch (IOException e) {
            return Result.error(e.getMessage() != null ? e.getMessage() : "预览失败");
        }
    }
}
