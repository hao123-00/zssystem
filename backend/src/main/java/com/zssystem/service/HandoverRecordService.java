package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.HandoverRecordQueryDTO;
import com.zssystem.dto.HandoverRecordSaveDTO;
import com.zssystem.vo.HandoverRecordVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HandoverRecordService {

    IPage<HandoverRecordVO> getRecordList(HandoverRecordQueryDTO queryDTO);

    HandoverRecordVO getRecordById(Long id);

    void saveRecord(HandoverRecordSaveDTO saveDTO);

    void deleteRecord(Long id);

    /** 根据设备ID获取产品名称下拉选项（来自基本排模、备用排模1-3、基本排模4） */
    List<String> getProductNamesByEquipmentId(Long equipmentId);

    /** 导出交接班记录 Excel，按设备+月份，每张最多28条。返回 [内容字节, 文件名]，超过28条时打包ZIP */
    Object[] exportExcel(Long equipmentId, String recordMonth) throws Exception;

    /** 获取导出文件数量（每28条一张Excel） */
    int getExportFileCount(Long equipmentId, String recordMonth);

    /** 导出单张 Excel（page 从 1 开始） */
    Object[] exportExcelPage(Long equipmentId, String recordMonth, int page) throws Exception;

    /** 获取交接班记录表 HTML 预览（与下载 Excel 效果一致） */
    String getPreviewHtml(Long equipmentId, String recordMonth) throws Exception;

    /** 获取交接班记录表整月 HTML（多页拼接，用于扫码 SVG 展示） */
    String getPreviewHtmlForSvg(Long equipmentId, String recordMonth) throws Exception;

    /** 上传交接班拍照照片，返回存储路径。照片不显示，15天后自动删除 */
    String uploadHandoverPhoto(MultipartFile file) throws Exception;

    /** 获取交接班记录照片，无照片时返回 null */
    byte[] getRecordPhoto(Long id) throws Exception;
}
