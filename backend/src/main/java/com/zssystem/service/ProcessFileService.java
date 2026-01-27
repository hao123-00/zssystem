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
     * 提交审批
     */
    void submitForApproval(Long fileId, Long currentUserId);
    
    /**
     * 审批工艺文件
     */
    void approveProcessFile(ProcessFileApprovalDTO approvalDTO, Long currentUserId, String currentUserName, String currentUserRole, Long signatureId);
    
    /**
     * 作废工艺文件
     */
    void invalidateProcessFile(Long fileId, Long currentUserId);
    
    /**
     * 下载工艺文件
     */
    byte[] downloadProcessFile(Long fileId) throws IOException;
    
    /**
     * 查询设备的工艺文件列表
     */
    IPage<ProcessFileVO> getProcessFilesByEquipment(Long equipmentId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取待审批的工艺文件列表（根据当前用户角色）
     */
    IPage<ProcessFileVO> getPendingApprovalFiles(String userRole, Integer pageNum, Integer pageSize);
}
