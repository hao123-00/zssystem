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
    /**
     * 导出某设备某月30天点检记录为 Excel
     * @param equipmentId 设备ID
     * @param checkMonth 月份 yyyy-MM
     * @return Excel 文件字节
     */
    byte[] exportCheckExcel(Long equipmentId, String checkMonth) throws java.io.IOException;
}
