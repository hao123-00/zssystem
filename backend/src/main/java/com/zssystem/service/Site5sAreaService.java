package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.Site5sAreaQueryDTO;
import com.zssystem.dto.Site5sAreaSaveDTO;
import com.zssystem.entity.SysUser;
import com.zssystem.vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface Site5sAreaService {

    IPage<Site5sAreaVO> getAreaList(Site5sAreaQueryDTO queryDTO);

    Site5sAreaVO getAreaById(Long id);

    void saveArea(Site5sAreaSaveDTO saveDTO);

    void deleteArea(Long id);

    /** 获取指定日期的拍照任务（各区域及各时段完成情况） */
    AreaDailyStatusVO getTasks(LocalDate photoDate);

    /** 获取日期范围内的拍照任务（按日期每天一条记录） */
    List<AreaDailyStatusVO> getTasksRange(LocalDate startDate, LocalDate endDate);

    /** 设置/取消区域某日放假（仅注塑部经理） */
    void setDayOff(Long areaId, LocalDate photoDate, boolean dayOff);

    /** 上传区域照片，返回拍照记录ID */
    Long uploadPhoto(Long areaId, Integer slotIndex, LocalDate photoDate, MultipartFile file,
                    Long uploaderId, String uploaderName);

    /** 获取拍照记录分页 */
    IPage<Site5sAreaPhotoVO> getPhotoRecords(Long areaId, LocalDate startDate, LocalDate endDate,
                                             Integer pageNum, Integer pageSize);

    /** 获取照片文件字节 */
    byte[] getPhotoBytes(Long photoId);

    /** 删除拍照记录（单条，按ID） */
    void deletePhoto(Long photoId, Long currentUserId);

    /** 删除区域某日全部拍照记录（早间+晚间），物理删除数据库与文件，删除后可重新上传 */
    void deletePhotosByAreaAndDate(Long areaId, LocalDate photoDate, Long currentUserId);

    /** 获取注塑组长列表（用于负责人选择） */
    List<SysUser> getInjectionLeaders();

    /** 灯光管理示意图：本月至今完成次数、拍照完成率（完成次数=按时拍照次数，完成率=完成次数/(天数*2)） */
    LightingStatsVO getLightingStats();
}
