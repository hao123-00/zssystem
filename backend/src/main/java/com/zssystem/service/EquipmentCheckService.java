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

    /**
     * 获取某设备某月30天点检表 HTML 预览（效果与下载 Excel 一致）
     */
    String getPreviewHtml(Long equipmentId, String checkMonth) throws java.io.IOException;

    /**
     * 获取点检表 HTML（PDF 用，√ × 使用 SVG 与 Excel 样式一致）
     */
    String getPreviewHtmlForPdf(Long equipmentId, String checkMonth) throws java.io.IOException;
}
