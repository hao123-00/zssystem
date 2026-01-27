package com.zssystem.service;

import com.zssystem.dto.ProcessFileSignatureDTO;
import com.zssystem.vo.ProcessFileSignatureVO;

import java.util.List;

/**
 * 工艺文件电子签名Service接口
 */
public interface ProcessFileSignatureService {
    
    /**
     * 保存电子签名
     * @param signatureDTO 签名DTO
     * @param signerId 签名人ID
     * @param signerName 签名人姓名
     * @param signerRole 签名人角色
     * @return 签名ID
     */
    Long saveSignature(ProcessFileSignatureDTO signatureDTO, Long signerId, String signerName, String signerRole);
    
    /**
     * 根据文件ID查询所有签名
     * @param fileId 文件ID
     * @return 签名列表
     */
    List<ProcessFileSignatureVO> getSignaturesByFileId(Long fileId);
    
    /**
     * 根据文件ID和签名类型查询签名
     * @param fileId 文件ID
     * @param signatureType 签名类型
     * @return 签名VO
     */
    ProcessFileSignatureVO getSignatureByFileIdAndType(Long fileId, String signatureType);
    
    /**
     * 根据签名ID查询签名
     * @param signatureId 签名ID
     * @return 签名VO
     */
    ProcessFileSignatureVO getSignatureById(Long signatureId);
}
