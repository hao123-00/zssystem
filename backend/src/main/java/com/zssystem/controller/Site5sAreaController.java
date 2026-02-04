package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.Site5sAreaQueryDTO;
import com.zssystem.dto.Site5sAreaSaveDTO;
import com.zssystem.service.Site5sAreaService;
import com.zssystem.util.SecurityUtil;
import com.zssystem.vo.AreaDailyStatusVO;
import com.zssystem.vo.InjectionLeaderVO;
import com.zssystem.vo.LightingStatsVO;
import com.zssystem.vo.Site5sAreaVO;
import com.zssystem.vo.Site5sAreaPhotoVO;
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

    /**
     * 是否可管理区域（仅注塑部经理可新增/编辑/删除）
     */
    @GetMapping("/can-manage")
    public Result<Boolean> canManage() {
        boolean can = com.zssystem.util.SecurityUtil.hasRoleCode("INJECTION_MANAGER")
                || com.zssystem.util.SecurityUtil.hasRole("注塑部经理");
        return Result.success(can);
    }

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
     * 获取日期范围内的拍照任务（按日期每天一条记录）
     */
    @GetMapping("/tasks-range")
    public Result<List<AreaDailyStatusVO>> getTasksRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(areaService.getTasksRange(startDate, endDate));
    }

    /**
     * 灯光管理示意图统计：本月至今完成次数、拍照完成率
     */
    @GetMapping("/lighting-stats")
    public Result<LightingStatsVO> getLightingStats() {
        return Result.success(areaService.getLightingStats());
    }

    /**
     * 设置/取消区域某日放假（所有角色可操作）
     */
    @PostMapping("/set-day-off")
    public Result<Void> setDayOff(
            @RequestParam Long areaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate photoDate,
            @RequestParam Boolean dayOff) {
        areaService.setDayOff(areaId, photoDate, Boolean.TRUE.equals(dayOff));
        return Result.success();
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
     * 获取注塑组长列表（用于负责人选择）
     */
    @GetMapping("/injection-leaders")
    public Result<List<InjectionLeaderVO>> getInjectionLeaders() {
        return Result.success(areaService.getInjectionLeaders().stream()
                .map(u -> {
                    InjectionLeaderVO vo = new InjectionLeaderVO();
                    vo.setId(u.getId());
                    vo.setName(u.getName() != null ? u.getName() : u.getUsername());
                    vo.setUsername(u.getUsername());
                    return vo;
                })
                .toList());
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

    /**
     * 删除拍照记录（单条）
     */
    @DeleteMapping("/photo/{id}")
    public Result<Void> deletePhoto(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        areaService.deletePhoto(id, userId);
        return Result.success();
    }

    /**
     * 删除区域某日全部拍照记录（早间+晚间），删除后可重新上传
     */
    @DeleteMapping("/photo-by-day")
    public Result<Void> deletePhotosByAreaAndDate(
            @RequestParam Long areaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate photoDate) {
        Long userId = SecurityUtil.getCurrentUserId();
        areaService.deletePhotosByAreaAndDate(areaId, photoDate, userId);
        return Result.success();
    }
}
