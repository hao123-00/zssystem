package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.EquipmentCheckQueryDTO;
import com.zssystem.dto.EquipmentCheckSaveDTO;
import com.zssystem.vo.EquipmentCheckVO;

public interface EquipmentCheckService {
    IPage<EquipmentCheckVO> getCheckList(EquipmentCheckQueryDTO queryDTO);
    EquipmentCheckVO getCheckById(Long id);
    void saveCheck(EquipmentCheckSaveDTO saveDTO);
    void deleteCheck(Long id);
}
