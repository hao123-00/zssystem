package com.zssystem.service;

import com.zssystem.dto.EquipmentProductionProductSaveDTO;
import com.zssystem.vo.EquipmentProductionProductVO;

import java.util.List;

public interface EquipmentProductionProductService {
    List<EquipmentProductionProductVO> getProductListByEquipmentId(Long equipmentId);
    EquipmentProductionProductVO getById(Long id);
    void create(EquipmentProductionProductSaveDTO saveDTO);
    void update(Long id, EquipmentProductionProductSaveDTO saveDTO);
    void delete(Long id);
}
