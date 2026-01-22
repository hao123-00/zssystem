package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.EquipmentMaintenanceQueryDTO;
import com.zssystem.dto.EquipmentMaintenanceSaveDTO;
import com.zssystem.vo.EquipmentMaintenanceVO;

public interface EquipmentMaintenanceService {
    IPage<EquipmentMaintenanceVO> getMaintenanceList(EquipmentMaintenanceQueryDTO queryDTO);
    EquipmentMaintenanceVO getMaintenanceById(Long id);
    void createMaintenance(EquipmentMaintenanceSaveDTO saveDTO);
    void updateMaintenance(Long id, EquipmentMaintenanceSaveDTO saveDTO);
    void deleteMaintenance(Long id);
}
