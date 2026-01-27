package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.Site5sCheckQueryDTO;
import com.zssystem.dto.Site5sCheckSaveDTO;
import com.zssystem.vo.Site5sCheckVO;

public interface Site5sCheckService {
    IPage<Site5sCheckVO> getCheckList(Site5sCheckQueryDTO queryDTO);
    Site5sCheckVO getCheckById(Long id);
    void saveCheck(Site5sCheckSaveDTO saveDTO);
    void deleteCheck(Long id);
}
