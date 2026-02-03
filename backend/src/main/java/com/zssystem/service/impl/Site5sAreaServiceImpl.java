package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.Site5sAreaQueryDTO;
import com.zssystem.dto.Site5sAreaSaveDTO;
import com.zssystem.dto.Site5sAreaScheduleDTO;
import com.zssystem.entity.Site5sArea;
import com.zssystem.entity.Site5sAreaPhoto;
import com.zssystem.entity.Site5sAreaSchedule;
import com.zssystem.mapper.Site5sAreaMapper;
import com.zssystem.mapper.Site5sAreaPhotoMapper;
import com.zssystem.mapper.Site5sAreaScheduleMapper;
import com.zssystem.service.Site5sAreaService;
import com.zssystem.util.BeanUtil;
import com.zssystem.util.FileUtil;
import com.zssystem.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Site5sAreaServiceImpl implements Site5sAreaService {

    @Autowired
    private Site5sAreaMapper areaMapper;

    @Autowired
    private Site5sAreaScheduleMapper scheduleMapper;

    @Autowired
    private Site5sAreaPhotoMapper photoMapper;

    @Value("${file.upload.site5s-area-photos}")
    private String areaPhotosPath;

    @Override
    public IPage<Site5sAreaVO> getAreaList(Site5sAreaQueryDTO queryDTO) {
        Page<Site5sArea> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Site5sArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getAreaCode() != null && !queryDTO.getAreaCode().isBlank(),
                        Site5sArea::getAreaCode, queryDTO.getAreaCode())
                .like(queryDTO.getAreaName() != null && !queryDTO.getAreaName().isBlank(),
                        Site5sArea::getAreaName, queryDTO.getAreaName())
                .like(queryDTO.getDutyName() != null && !queryDTO.getDutyName().isBlank(),
                        Site5sArea::getDutyName, queryDTO.getDutyName())
                .eq(queryDTO.getStatus() != null, Site5sArea::getStatus, queryDTO.getStatus())
                .orderByAsc(Site5sArea::getSortOrder)
                .orderByAsc(Site5sArea::getId);

        IPage<Site5sArea> areaPage = areaMapper.selectPage(page, wrapper);
        return areaPage.convert(area -> toAreaVO(area));
    }

    @Override
    public Site5sAreaVO getAreaById(Long id) {
        Site5sArea area = areaMapper.selectById(id);
        if (area == null) {
            throw new RuntimeException("区域不存在");
        }
        return toAreaVO(area);
    }

    @Override
    @Transactional
    public void saveArea(Site5sAreaSaveDTO saveDTO) {
        Site5sArea area;
        if (saveDTO.getId() != null) {
            area = areaMapper.selectById(saveDTO.getId());
            if (area == null) {
                throw new RuntimeException("区域不存在");
            }
        } else {
            area = new Site5sArea();
            if (areaMapper.selectOne(new LambdaQueryWrapper<Site5sArea>()
                    .eq(Site5sArea::getAreaCode, saveDTO.getAreaCode())) != null) {
                throw new RuntimeException("区域编码已存在");
            }
        }

        area.setAreaCode(saveDTO.getAreaCode());
        area.setAreaName(saveDTO.getAreaName());
        area.setDutyName(saveDTO.getDutyName());
        area.setSortOrder(saveDTO.getSortOrder() != null ? saveDTO.getSortOrder() : 0);
        area.setStatus(saveDTO.getStatus() != null ? saveDTO.getStatus() : 1);
        area.setRemark(saveDTO.getRemark());

        if (saveDTO.getId() != null) {
            areaMapper.updateById(area);
            scheduleMapper.delete(new LambdaQueryWrapper<Site5sAreaSchedule>()
                    .eq(Site5sAreaSchedule::getAreaId, area.getId()));
        } else {
            areaMapper.insert(area);
        }

        for (Site5sAreaScheduleDTO sdto : saveDTO.getSchedules()) {
            Site5sAreaSchedule s = new Site5sAreaSchedule();
            s.setAreaId(area.getId());
            s.setSlotIndex(sdto.getSlotIndex());
            s.setScheduledTime(sdto.getScheduledTime());
            s.setToleranceMinutes(sdto.getToleranceMinutes() != null ? sdto.getToleranceMinutes() : 30);
            s.setRemark(sdto.getRemark());
            scheduleMapper.insert(s);
        }
    }

    @Override
    @Transactional
    public void deleteArea(Long id) {
        Site5sArea area = areaMapper.selectById(id);
        if (area == null) {
            throw new RuntimeException("区域不存在");
        }
        areaMapper.deleteById(id);
        scheduleMapper.delete(new LambdaQueryWrapper<Site5sAreaSchedule>().eq(Site5sAreaSchedule::getAreaId, id));
    }

    @Override
    public AreaDailyStatusVO getTasks(LocalDate photoDate) {
        List<Site5sArea> areas = areaMapper.selectList(
                new LambdaQueryWrapper<Site5sArea>()
                        .eq(Site5sArea::getStatus, 1)
                        .orderByAsc(Site5sArea::getSortOrder)
                        .orderByAsc(Site5sArea::getId));
        List<AreaTaskVO> taskList = new ArrayList<>();
        for (Site5sArea area : areas) {
            List<Site5sAreaSchedule> schedules = scheduleMapper.selectList(
                    new LambdaQueryWrapper<Site5sAreaSchedule>()
                            .eq(Site5sAreaSchedule::getAreaId, area.getId())
                            .orderByAsc(Site5sAreaSchedule::getSlotIndex));
            if (schedules.isEmpty()) continue;

            List<AreaTaskSlotVO> slots = new ArrayList<>();
            int completedOnTime = 0;
            for (Site5sAreaSchedule sch : schedules) {
                AreaTaskSlotVO slot = new AreaTaskSlotVO();
                slot.setSlotIndex(sch.getSlotIndex());
                slot.setScheduledTime(sch.getScheduledTime());
                slot.setToleranceMinutes(sch.getToleranceMinutes());
                Site5sAreaPhoto photo = photoMapper.selectOne(
                        new LambdaQueryWrapper<Site5sAreaPhoto>()
                                .eq(Site5sAreaPhoto::getAreaId, area.getId())
                                .eq(Site5sAreaPhoto::getPhotoDate, photoDate)
                                .eq(Site5sAreaPhoto::getSlotIndex, sch.getSlotIndex()));
                if (photo != null) {
                    slot.setCompleted(true);
                    slot.setOnTime(photo.getIsOnTime() != null && photo.getIsOnTime() == 1);
                    slot.setPhotoId(photo.getId());
                    slot.setUploaderName(photo.getUploaderName());
                    slot.setUploadTimeStr(photo.getUploadTime() != null
                            ? photo.getUploadTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
                    if (slot.getOnTime()) completedOnTime++;
                } else {
                    slot.setCompleted(false);
                    slot.setOnTime(null);
                }
                slots.add(slot);
            }
            AreaTaskVO task = new AreaTaskVO();
            task.setAreaId(area.getId());
            task.setAreaCode(area.getAreaCode());
            task.setAreaName(area.getAreaName());
            task.setDutyName(area.getDutyName());
            task.setTotalSlots(schedules.size());
            task.setCompletedSlots((int) slots.stream().filter(AreaTaskSlotVO::getCompleted).count());
            task.setStatus(completedOnTime >= schedules.size() ? 1 : 0);
            task.setSlots(slots);
            taskList.add(task);
        }
        AreaDailyStatusVO vo = new AreaDailyStatusVO();
        vo.setStatusDate(photoDate);
        vo.setAreas(taskList);
        return vo;
    }

    @Override
    @Transactional
    public Long uploadPhoto(Long areaId, Integer slotIndex, LocalDate photoDate, MultipartFile file,
                            Long uploaderId, String uploaderName) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择或拍摄照片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只能上传图片格式");
        }
        Site5sArea area = areaMapper.selectById(areaId);
        if (area == null) {
            throw new RuntimeException("区域不存在");
        }
        if (!Integer.valueOf(1).equals(area.getStatus())) {
            throw new RuntimeException("该区域已停用");
        }
        Site5sAreaSchedule schedule = scheduleMapper.selectOne(
                new LambdaQueryWrapper<Site5sAreaSchedule>()
                        .eq(Site5sAreaSchedule::getAreaId, areaId)
                        .eq(Site5sAreaSchedule::getSlotIndex, slotIndex));
        if (schedule == null) {
            throw new RuntimeException("该区域不存在该时段配置");
        }
        Site5sAreaPhoto existing = photoMapper.selectOne(
                new LambdaQueryWrapper<Site5sAreaPhoto>()
                        .eq(Site5sAreaPhoto::getAreaId, areaId)
                        .eq(Site5sAreaPhoto::getPhotoDate, photoDate)
                        .eq(Site5sAreaPhoto::getSlotIndex, slotIndex));
        if (existing != null) {
            throw new RuntimeException("该区域该时段已上传过照片");
        }

        String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String saveDir = areaPhotosPath + "/" + yearMonth;
        FileUtil.createDirectoryIfNotExists(saveDir);
        String ext = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                : ".jpg";
        String fileName = "AP_" + System.currentTimeMillis() + ext;
        String fullPath = saveDir + "/" + fileName;
        try {
            file.transferTo(new File(fullPath));
        } catch (Exception e) {
            throw new RuntimeException("照片保存失败: " + e.getMessage());
        }

        LocalDateTime uploadTime = LocalDateTime.now();
        LocalTime scheduledTime = schedule.getScheduledTime();
        int tolerance = schedule.getToleranceMinutes() != null ? schedule.getToleranceMinutes() : 30;
        LocalTime uploadTimeOnly = uploadTime.toLocalTime();
        LocalTime start = scheduledTime.minusMinutes(tolerance);
        LocalTime end = scheduledTime.plusMinutes(tolerance);
        boolean onTime = !uploadTimeOnly.isBefore(start) && !uploadTimeOnly.isAfter(end);

        Site5sAreaPhoto photo = new Site5sAreaPhoto();
        photo.setAreaId(areaId);
        photo.setPhotoDate(photoDate);
        photo.setSlotIndex(slotIndex);
        photo.setPhotoPath(fullPath);
        photo.setUploaderId(uploaderId);
        photo.setUploaderName(uploaderName);
        photo.setUploadTime(uploadTime);
        photo.setIsOnTime(onTime ? 1 : 0);
        photoMapper.insert(photo);
        return photo.getId();
    }

    @Override
    public IPage<Site5sAreaPhotoVO> getPhotoRecords(Long areaId, LocalDate startDate, LocalDate endDate,
                                                     Integer pageNum, Integer pageSize) {
        Page<Site5sAreaPhoto> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
        LambdaQueryWrapper<Site5sAreaPhoto> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(areaId != null, Site5sAreaPhoto::getAreaId, areaId)
                .ge(startDate != null, Site5sAreaPhoto::getPhotoDate, startDate)
                .le(endDate != null, Site5sAreaPhoto::getPhotoDate, endDate)
                .orderByDesc(Site5sAreaPhoto::getPhotoDate)
                .orderByAsc(Site5sAreaPhoto::getSlotIndex);

        IPage<Site5sAreaPhoto> photoPage = photoMapper.selectPage(page, wrapper);
        return photoPage.convert(p -> {
            Site5sAreaPhotoVO vo = BeanUtil.copyProperties(p, Site5sAreaPhotoVO.class);
            Site5sArea a = areaMapper.selectById(p.getAreaId());
            if (a != null) {
                vo.setAreaName(a.getAreaName());
                vo.setDutyName(a.getDutyName());
            }
            return vo;
        });
    }

    @Override
    public byte[] getPhotoBytes(Long photoId) {
        Site5sAreaPhoto photo = photoMapper.selectById(photoId);
        if (photo == null) {
            throw new RuntimeException("拍照记录不存在");
        }
        String path = photo.getPhotoPath();
        if (path == null || path.isBlank()) return null;
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) return null;
        try {
            return Files.readAllBytes(filePath);
        } catch (Exception e) {
            throw new RuntimeException("读取照片失败");
        }
    }

    private Site5sAreaVO toAreaVO(Site5sArea area) {
        Site5sAreaVO vo = BeanUtil.copyProperties(area, Site5sAreaVO.class);
        List<Site5sAreaSchedule> schedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<Site5sAreaSchedule>()
                        .eq(Site5sAreaSchedule::getAreaId, area.getId())
                        .orderByAsc(Site5sAreaSchedule::getSlotIndex));
        vo.setSchedules(schedules.stream()
                .map(s -> BeanUtil.copyProperties(s, Site5sAreaScheduleVO.class))
                .collect(Collectors.toList()));
        return vo;
    }
}
