package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.EquipmentFaultQueryDTO;
import com.zssystem.dto.EquipmentFaultSaveDTO;
import com.zssystem.vo.EquipmentFaultVO;

public interface EquipmentFaultService {
    IPage<EquipmentFaultVO> getFaultList(EquipmentFaultQueryDTO queryDTO);
    EquipmentFaultVO getFaultById(Long id);
    void createFault(EquipmentFaultSaveDTO saveDTO);
    void updateFault(Long id, EquipmentFaultSaveDTO saveDTO);
    void deleteFault(Long id);
}
