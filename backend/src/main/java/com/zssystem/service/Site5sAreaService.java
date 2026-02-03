package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.Site5sAreaQueryDTO;
import com.zssystem.dto.Site5sAreaSaveDTO;
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

    /** 上传区域照片，返回拍照记录ID */
    Long uploadPhoto(Long areaId, Integer slotIndex, LocalDate photoDate, MultipartFile file,
                    Long uploaderId, String uploaderName);

    /** 获取拍照记录分页 */
    IPage<Site5sAreaPhotoVO> getPhotoRecords(Long areaId, LocalDate startDate, LocalDate endDate,
                                             Integer pageNum, Integer pageSize);

    /** 获取照片文件字节 */
    byte[] getPhotoBytes(Long photoId);
}
