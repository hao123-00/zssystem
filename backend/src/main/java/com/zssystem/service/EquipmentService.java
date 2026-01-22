package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.EquipmentQueryDTO;
import com.zssystem.dto.EquipmentSaveDTO;
import com.zssystem.vo.EquipmentVO;

public interface EquipmentService {
    IPage<EquipmentVO> getEquipmentList(EquipmentQueryDTO queryDTO);
    EquipmentVO getEquipmentById(Long id);
    EquipmentVO getEquipmentByNo(String equipmentNo);
    void createEquipment(EquipmentSaveDTO saveDTO);
    void updateEquipment(Long id, EquipmentSaveDTO saveDTO);
    void deleteEquipment(Long id);
}
