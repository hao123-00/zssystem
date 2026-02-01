package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.ProcessFileApprovalDTO;
import com.zssystem.dto.ProcessFileQueryDTO;
import com.zssystem.dto.ProcessFileUploadDTO;
import com.zssystem.vo.ProcessFileVO;

import java.io.IOException;

/**
 * 工艺文件Service接口
 */
public interface ProcessFileService {
    /**
     * 分页查询工艺文件列表
     */
    IPage<ProcessFileVO> getProcessFileList(ProcessFileQueryDTO queryDTO);
    
    /**
     * 根据ID查询工艺文件详情
     */
    ProcessFileVO getProcessFileById(Long id);
    
    /**
     * 上传工艺文件（新建或修改）- Excel文件方式
     */
    void uploadProcessFile(ProcessFileUploadDTO uploadDTO, Long currentUserId, String currentUserName) throws IOException;
    
    /**
     * 保存工艺文件表单（新建或修改）- 表单方式
     * @return 工艺文件ID
     */
    Long saveProcessFileForm(com.zssystem.dto.ProcessFileFormDTO formDTO, Long currentUserId, String currentUserName);
    
    /**
     * 保存工艺文件表单（支持产品关键尺寸图片上传）
     * @return 工艺文件ID
     */
    Long saveProcessFileForm(com.zssystem.dto.ProcessFileFormDTO formDTO, Long currentUserId, String currentUserName,
                             org.springframework.web.multipart.MultipartFile keyDimensionImage1,
                             org.springframework.web.multipart.MultipartFile keyDimensionImage2);
    
    /**
     * 提交审批
     */
    void submitForApproval(Long fileId, Long currentUserId);
    
    /**
     * 审批工艺文件
     */
    void approveProcessFile(ProcessFileApprovalDTO approvalDTO, Long currentUserId, String currentUserName, String currentUserRole, Long signatureId);
    
    /**
     * 作废工艺文件（保留接口，状态改为已作废）
     */
    void invalidateProcessFile(Long fileId, Long currentUserId);
    
    /**
     * 物理删除工艺文件（删除数据库记录及关联文件）
     */
    void deleteProcessFilePhysical(Long fileId);
    
    /**
     * 按设备（机台号）批量物理删除工艺文件
     * @return 删除的数量
     */
    int batchDeleteByEquipmentIdPhysical(Long equipmentId);
    
    /**
     * 按设备（机台号）批量作废工艺文件
     * @return 作废的数量
     */
    int batchInvalidateByEquipmentId(Long equipmentId, Long currentUserId);
    
    /**
     * 启用工艺文件（同机台号其他工艺文件自动设为搁置）
     */
    void setEnabled(Long fileId);
    
    /**
     * 搁置工艺文件
     */
    void setArchived(Long fileId);
    
    /**
     * 下载工艺文件
     */
    byte[] downloadProcessFile(Long fileId) throws IOException;

    /**
     * 获取工艺文件 HTML 预览（与下载 Excel 效果一致）
     */
    String getPreviewHtml(Long fileId) throws IOException;

    /**
     * 获取工艺文件 HTML（PDF 友好格式，用于转 PDF）
     */
    String getPreviewHtmlForPdf(Long fileId) throws IOException;
    
    /**
     * 查询设备的工艺文件列表
     */
    IPage<ProcessFileVO> getProcessFilesByEquipment(Long equipmentId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取待审批的工艺文件列表（根据当前用户角色）
     */
    IPage<ProcessFileVO> getPendingApprovalFiles(String userRole, Integer pageNum, Integer pageSize);
}
