package com.zssystem.service;

import com.zssystem.dto.EquipmentProductSaveDTO;
import com.zssystem.vo.EquipmentProductVO;

import java.util.List;

public interface EquipmentProductService {
    List<EquipmentProductVO> getProductListByEquipmentId(Long equipmentId);
    List<EquipmentProductVO> getEquipmentListByProductCode(String productCode);
    void bindProduct(EquipmentProductSaveDTO saveDTO);
    void unbindProduct(Long id);
}
