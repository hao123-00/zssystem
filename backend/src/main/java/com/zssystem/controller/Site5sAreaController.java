package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.Site5sAreaQueryDTO;
import com.zssystem.dto.Site5sAreaSaveDTO;
import com.zssystem.service.Site5sAreaService;
import com.zssystem.util.SecurityUtil;
import com.zssystem.vo.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/site5s/area")
@Validated
public class Site5sAreaController {

    @Autowired
    private Site5sAreaService areaService;

    @GetMapping("/list")
    public Result<PageResult<Site5sAreaVO>> getAreaList(@Validated Site5sAreaQueryDTO queryDTO) {
        IPage<Site5sAreaVO> page = areaService.getAreaList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<Site5sAreaVO> getAreaById(@PathVariable Long id) {
        Site5sAreaVO vo = areaService.getAreaById(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Void> saveArea(@Valid @RequestBody Site5sAreaSaveDTO saveDTO) {
        areaService.saveArea(saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteArea(@PathVariable Long id) {
        areaService.deleteArea(id);
        return Result.success();
    }

    /**
     * 获取指定日期的拍照任务（各区域及各时段完成情况）
     */
    @GetMapping("/tasks")
    public Result<AreaDailyStatusVO> getTasks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate photoDate) {
        AreaDailyStatusVO vo = areaService.getTasks(photoDate);
        return Result.success(vo);
    }

    /**
     * 上传区域照片
     */
    @PostMapping("/upload-photo")
    public Result<Long> uploadPhoto(
            @RequestParam Long areaId,
            @RequestParam Integer slotIndex,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate photoDate,
            @RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtil.getCurrentUserId();
        String userName = SecurityUtil.getCurrentUser() != null
                ? SecurityUtil.getCurrentUser().getName() : SecurityUtil.getCurrentUsername();
        Long photoId = areaService.uploadPhoto(areaId, slotIndex, photoDate, file, userId, userName);
        return Result.success(photoId);
    }

    /**
     * 拍照记录分页查询
     */
    @GetMapping("/photo-records")
    public Result<PageResult<Site5sAreaPhotoVO>> getPhotoRecords(
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<Site5sAreaPhotoVO> page = areaService.getPhotoRecords(areaId, startDate, endDate, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取照片
     */
    @GetMapping("/photo/{id}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        byte[] bytes = areaService.getPhotoBytes(id);
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
