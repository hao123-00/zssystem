package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.Site5sAreaQueryDTO;
import com.zssystem.dto.Site5sAreaSaveDTO;
import com.zssystem.entity.Site5sArea;
import com.zssystem.entity.Site5sAreaDayOff;
import com.zssystem.entity.Site5sAreaPhoto;
import com.zssystem.entity.Site5sAreaSchedule;
import com.zssystem.entity.SysRole;
import com.zssystem.entity.SysUser;
import com.zssystem.mapper.Site5sAreaDayOffMapper;
import com.zssystem.mapper.Site5sAreaMapper;
import com.zssystem.mapper.Site5sAreaPhotoMapper;
import com.zssystem.mapper.Site5sAreaScheduleMapper;
import com.zssystem.mapper.SysRoleMapper;
import com.zssystem.mapper.SysUserMapper;
import com.zssystem.mapper.SysUserRoleMapper;
import com.zssystem.service.Site5sAreaService;
import com.zssystem.util.BeanUtil;
import com.zssystem.util.FileUtil;
import com.zssystem.util.SecurityUtil;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class Site5sAreaServiceImpl implements Site5sAreaService {

    private static final int SLOT_MORNING = 1;
    private static final int SLOT_EVENING = 2;
    private static final int TOLERANCE_MINUTES = 30;

    @Autowired
    private Site5sAreaMapper areaMapper;

    @Autowired
    private Site5sAreaScheduleMapper scheduleMapper;

    @Autowired
    private Site5sAreaPhotoMapper photoMapper;

    @Autowired
    private Site5sAreaDayOffMapper dayOffMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Value("${file.upload.site5s-area-photos}")
    private String areaPhotosPath;

    @Override
    public IPage<Site5sAreaVO> getAreaList(Site5sAreaQueryDTO queryDTO) {
        Page<Site5sArea> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Site5sArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getAreaName() != null && !queryDTO.getAreaName().isBlank(),
                        Site5sArea::getAreaName, queryDTO.getAreaName())
                .like(queryDTO.getCheckItem() != null && !queryDTO.getCheckItem().isBlank(),
                        Site5sArea::getCheckItem, queryDTO.getCheckItem())
                .eq(queryDTO.getStatus() != null, Site5sArea::getStatus, queryDTO.getStatus())
                .orderByAsc(Site5sArea::getSortOrder)
                .orderByAsc(Site5sArea::getId);

        IPage<Site5sArea> areaPage = areaMapper.selectPage(page, wrapper);
        return areaPage.convert(this::toAreaVO);
    }

    @Override
    public Site5sAreaVO getAreaById(Long id) {
        Site5sArea area = areaMapper.selectById(id);
        if (area == null) {
            throw new RuntimeException("区域不存在");
        }
        return toAreaVO(area);
    }

    private void requireManager() {
        boolean can = SecurityUtil.hasRoleCode("INJECTION_MANAGER") || SecurityUtil.hasRole("注塑部经理");
        if (!can) {
            throw new RuntimeException("仅注塑部经理可管理区域");
        }
    }

    @Override
    @Transactional
    public void saveArea(Site5sAreaSaveDTO saveDTO) {
        requireManager();
        Site5sArea area;
        if (saveDTO.getId() != null) {
            area = areaMapper.selectById(saveDTO.getId());
            if (area == null) {
                throw new RuntimeException("区域不存在");
            }
        } else {
            area = new Site5sArea();
            String areaCode = generateAreaCode();
            area.setAreaCode(areaCode);
        }

        area.setAreaName(saveDTO.getAreaName());
        area.setCheckItem(saveDTO.getCheckItem());
        area.setResponsibleUserId(saveDTO.getResponsibleUserId());
        area.setResponsibleUserId2(saveDTO.getResponsibleUserId2());
        area.setMorningPhotoTime(saveDTO.getMorningPhotoTime());
        area.setEveningPhotoTime(saveDTO.getEveningPhotoTime());
        area.setSortOrder(saveDTO.getSortOrder() != null ? saveDTO.getSortOrder() : 0);
        area.setStatus(saveDTO.getStatus() != null ? saveDTO.getStatus() : 1);
        area.setRemark(saveDTO.getRemark());

        if (saveDTO.getId() != null) {
            areaMapper.updateById(area);
        } else {
            areaMapper.insert(area);
        }
    }

    @Override
    @Transactional
    public void deleteArea(Long id) {
        requireManager();
        Site5sArea area = areaMapper.selectById(id);
        if (area == null) {
            throw new RuntimeException("区域不存在");
        }
        areaMapper.deleteById(id);
        scheduleMapper.delete(new LambdaQueryWrapper<Site5sAreaSchedule>().eq(Site5sAreaSchedule::getAreaId, id));
    }

    @Override
    public AreaDailyStatusVO getTasks(LocalDate photoDate) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean isManager = SecurityUtil.hasRoleCode("INJECTION_MANAGER") || SecurityUtil.hasRole("注塑部经理");

        List<Site5sArea> areas = areaMapper.selectList(
                new LambdaQueryWrapper<Site5sArea>()
                        .eq(Site5sArea::getStatus, 1)
                        .orderByAsc(Site5sArea::getSortOrder)
                        .orderByAsc(Site5sArea::getId));

        if (!isManager && currentUserId != null) {
            areas = areas.stream()
                    .filter(a -> currentUserId.equals(a.getResponsibleUserId()) || currentUserId.equals(a.getResponsibleUserId2()))
                    .collect(Collectors.toList());
        }

        Set<Long> dayOffAreaIds = dayOffMapper.selectList(
                        new LambdaQueryWrapper<Site5sAreaDayOff>()
                                .eq(Site5sAreaDayOff::getOffDate, photoDate))
                .stream()
                .map(Site5sAreaDayOff::getAreaId)
                .collect(Collectors.toSet());

        List<AreaTaskVO> taskList = new ArrayList<>();
        for (Site5sArea area : areas) {
            LocalTime morningTime = area.getMorningPhotoTime() != null ? area.getMorningPhotoTime() : LocalTime.of(8, 0);
            LocalTime eveningTime = area.getEveningPhotoTime() != null ? area.getEveningPhotoTime() : LocalTime.of(16, 0);

            List<AreaTaskSlotVO> slots = new ArrayList<>();
            int completedOnTime = 0;

            for (int slotIndex : new int[]{SLOT_MORNING, SLOT_EVENING}) {
                LocalTime scheduledTime = slotIndex == SLOT_MORNING ? morningTime : eveningTime;
                AreaTaskSlotVO slot = new AreaTaskSlotVO();
                slot.setSlotIndex(slotIndex);
                slot.setScheduledTime(scheduledTime);
                slot.setToleranceMinutes(TOLERANCE_MINUTES);
                Site5sAreaPhoto photo = photoMapper.selectOne(
                        new LambdaQueryWrapper<Site5sAreaPhoto>()
                                .eq(Site5sAreaPhoto::getAreaId, area.getId())
                                .eq(Site5sAreaPhoto::getPhotoDate, photoDate)
                                .eq(Site5sAreaPhoto::getSlotIndex, slotIndex));
                if (photo != null) {
                    slot.setCompleted(true);
                    slot.setOnTime(photo.getIsOnTime() != null && photo.getIsOnTime() == 1);
                    slot.setPhotoId(photo.getId());
                    slot.setUploaderName(photo.getUploaderName());
                    slot.setUploadTimeStr(photo.getUploadTime() != null
                            ? photo.getUploadTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
                    if (Boolean.TRUE.equals(slot.getOnTime())) completedOnTime++;
                } else {
                    slot.setCompleted(false);
                    slot.setOnTime(null);
                }
                slots.add(slot);
            }

            boolean dayOff = dayOffAreaIds.contains(area.getId());
            AreaTaskVO task = new AreaTaskVO();
            task.setAreaId(area.getId());
            task.setAreaCode(area.getAreaCode());
            task.setAreaName(area.getAreaName());
            task.setCheckItem(area.getCheckItem());
            task.setResponsibleUserId(area.getResponsibleUserId());
            task.setResponsibleUserName(getUserName(area.getResponsibleUserId()));
            task.setResponsibleUserId2(area.getResponsibleUserId2());
            task.setResponsibleUserName2(getUserName(area.getResponsibleUserId2()));
            task.setTotalSlots(2);
            task.setCompletedSlots((int) slots.stream().filter(AreaTaskSlotVO::getCompleted).count());
            task.setDayOff(dayOff);
            task.setStatus(dayOff ? 2 : (completedOnTime >= 2 ? 1 : 0));
            task.setSlots(slots);
            taskList.add(task);
        }

        AreaDailyStatusVO vo = new AreaDailyStatusVO();
        vo.setStatusDate(photoDate);
        vo.setAreas(taskList);
        return vo;
    }

    @Override
    public List<AreaDailyStatusVO> getTasksRange(LocalDate startDate, LocalDate endDate) {
        List<AreaDailyStatusVO> result = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            result.add(getTasks(d));
        }
        return result;
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
        if (slotIndex != SLOT_MORNING && slotIndex != SLOT_EVENING) {
            throw new RuntimeException("时段参数错误");
        }

        Site5sArea area = areaMapper.selectById(areaId);
        if (area == null) {
            throw new RuntimeException("区域不存在");
        }
        if (!Integer.valueOf(1).equals(area.getStatus())) {
            throw new RuntimeException("该区域已停用");
        }

        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean isManager = SecurityUtil.hasRoleCode("INJECTION_MANAGER") || SecurityUtil.hasRole("注塑部经理");
        boolean isResponsible = currentUserId != null
                && (currentUserId.equals(area.getResponsibleUserId()) || currentUserId.equals(area.getResponsibleUserId2()));
        if (!isManager && !isResponsible) {
            throw new RuntimeException("您不是该区域负责人，无法上传照片");
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

        LocalTime scheduledTime = slotIndex == SLOT_MORNING
                ? (area.getMorningPhotoTime() != null ? area.getMorningPhotoTime() : LocalTime.of(8, 0))
                : (area.getEveningPhotoTime() != null ? area.getEveningPhotoTime() : LocalTime.of(16, 0));
        LocalDateTime uploadTime = LocalDateTime.now();
        LocalTime uploadTimeOnly = uploadTime.toLocalTime();
        LocalTime start = scheduledTime.minusMinutes(TOLERANCE_MINUTES);
        LocalTime end = scheduledTime.plusMinutes(TOLERANCE_MINUTES);
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
        try {
            photoMapper.insert(photo);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("该区域该时段已上传过照片");
        }
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
                vo.setCheckItem(a.getCheckItem());
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

    @Override
    @Transactional
    public void deletePhoto(Long photoId, Long currentUserId) {
        Site5sAreaPhoto photo = photoMapper.selectById(photoId);
        if (photo == null) {
            throw new RuntimeException("拍照记录不存在");
        }
        Site5sArea area = areaMapper.selectById(photo.getAreaId());
        if (area == null) {
            throw new RuntimeException("区域不存在");
        }
        boolean isManager = SecurityUtil.hasRoleCode("INJECTION_MANAGER") || SecurityUtil.hasRole("注塑部经理");
        boolean isResponsible = currentUserId != null
                && (currentUserId.equals(area.getResponsibleUserId()) || currentUserId.equals(area.getResponsibleUserId2()));
        if (!isManager && !isResponsible) {
            throw new RuntimeException("您不是该区域负责人，无法删除拍照记录");
        }
        String path = photo.getPhotoPath();
        if (path != null && !path.isBlank()) {
            try {
                Files.deleteIfExists(Paths.get(path));
            } catch (Exception e) {
                throw new RuntimeException("删除照片文件失败");
            }
        }
        photoMapper.deleteByIdPhysical(photoId);
    }

    @Override
    @Transactional
    public void deletePhotosByAreaAndDate(Long areaId, LocalDate photoDate, Long currentUserId) {
        Site5sArea area = areaMapper.selectById(areaId);
        if (area == null) {
            throw new RuntimeException("区域不存在");
        }
        boolean isManager = SecurityUtil.hasRoleCode("INJECTION_MANAGER") || SecurityUtil.hasRole("注塑部经理");
        boolean isResponsible = currentUserId != null
                && (currentUserId.equals(area.getResponsibleUserId()) || currentUserId.equals(area.getResponsibleUserId2()));
        if (!isManager && !isResponsible) {
            throw new RuntimeException("您不是该区域负责人，无法删除拍照记录");
        }
        List<Site5sAreaPhoto> list = photoMapper.selectList(
                new LambdaQueryWrapper<Site5sAreaPhoto>()
                        .eq(Site5sAreaPhoto::getAreaId, areaId)
                        .eq(Site5sAreaPhoto::getPhotoDate, photoDate));
        for (Site5sAreaPhoto photo : list) {
            String path = photo.getPhotoPath();
            if (path != null && !path.isBlank()) {
                try {
                    Files.deleteIfExists(Paths.get(path));
                } catch (Exception ignored) {
                }
            }
            photoMapper.deleteByIdPhysical(photo.getId());
        }
    }

    @Override
    @Transactional
    public void setDayOff(Long areaId, LocalDate photoDate, boolean dayOff) {
        Site5sArea area = areaMapper.selectById(areaId);
        if (area == null) {
            throw new RuntimeException("区域不存在");
        }
        if (dayOff) {
            long cnt = dayOffMapper.selectCount(
                    new LambdaQueryWrapper<Site5sAreaDayOff>()
                            .eq(Site5sAreaDayOff::getAreaId, areaId)
                            .eq(Site5sAreaDayOff::getOffDate, photoDate));
            if (cnt == 0) {
                Site5sAreaDayOff record = new Site5sAreaDayOff();
                record.setAreaId(areaId);
                record.setOffDate(photoDate);
                dayOffMapper.insert(record);
            }
        } else {
            dayOffMapper.delete(
                    new LambdaQueryWrapper<Site5sAreaDayOff>()
                            .eq(Site5sAreaDayOff::getAreaId, areaId)
                            .eq(Site5sAreaDayOff::getOffDate, photoDate));
        }
    }

    @Override
    public LightingStatsVO getLightingStats() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        int days = (int) ChronoUnit.DAYS.between(monthStart, today) + 1;
        long completionCount = photoMapper.selectCount(
                new LambdaQueryWrapper<Site5sAreaPhoto>()
                        .ge(Site5sAreaPhoto::getPhotoDate, monthStart)
                        .le(Site5sAreaPhoto::getPhotoDate, today)
                        .eq(Site5sAreaPhoto::getIsOnTime, 1));
        long areaCount = areaMapper.selectCount(
                new LambdaQueryWrapper<Site5sArea>().eq(Site5sArea::getStatus, 1));
        long dayOffCount = dayOffMapper.selectCount(
                new LambdaQueryWrapper<Site5sAreaDayOff>()
                        .ge(Site5sAreaDayOff::getOffDate, monthStart)
                        .le(Site5sAreaDayOff::getOffDate, today));
        double totalSlots = 2.0 * (areaCount * days - dayOffCount);
        double completionRate = totalSlots > 0 ? (completionCount / totalSlots) : 0.0;
        LightingStatsVO vo = new LightingStatsVO();
        vo.setCompletionCount((int) completionCount);
        vo.setDays(days);
        vo.setCompletionRate(completionRate);
        return vo;
    }

    @Override
    public List<SysUser> getInjectionLeaders() {
        SysRole role = roleMapper.selectByRoleCode("INJECTION_LEADER");
        if (role == null) return List.of();
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(role.getId());
        if (userIds == null || userIds.isEmpty()) return List.of();
        return userMapper.selectBatchIds(userIds).stream()
                .filter(u -> u.getStatus() != null && u.getStatus() == 1)
                .collect(Collectors.toList());
    }

    private String generateAreaCode() {
        long count = areaMapper.selectCount(null);
        return String.format("AREA%03d", count + 1);
    }

    private String getUserName(Long userId) {
        if (userId == null) return null;
        SysUser u = userMapper.selectById(userId);
        return u != null ? u.getName() : null;
    }

    private Site5sAreaVO toAreaVO(Site5sArea area) {
        Site5sAreaVO vo = BeanUtil.copyProperties(area, Site5sAreaVO.class);
        vo.setResponsibleUserName(getUserName(area.getResponsibleUserId()));
        vo.setResponsibleUserName2(getUserName(area.getResponsibleUserId2()));
        return vo;
    }
}
