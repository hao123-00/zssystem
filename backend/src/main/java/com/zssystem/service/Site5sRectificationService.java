package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.Site5sRectificationQueryDTO;
import com.zssystem.dto.Site5sRectificationSaveDTO;
import com.zssystem.vo.Site5sRectificationVO;

public interface Site5sRectificationService {
    IPage<Site5sRectificationVO> getRectificationList(Site5sRectificationQueryDTO queryDTO);
    Site5sRectificationVO getRectificationById(Long id);
    void saveRectification(Site5sRectificationSaveDTO saveDTO);
    void deleteRectification(Long id);
    void createFromCheck(Long checkId);
}
