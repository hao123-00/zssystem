package com.zssystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zssystem.dto.ProcessFileSignatureDTO;
import com.zssystem.entity.ProcessFileSignature;
import com.zssystem.mapper.ProcessFileSignatureMapper;
import com.zssystem.service.ProcessFileSignatureService;
import com.zssystem.util.FileUtil;
import com.zssystem.util.ProcessFileExcelUtil;
import com.zssystem.vo.ProcessFileSignatureVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工艺文件电子签名Service实现类
 */
@Service
public class ProcessFileSignatureServiceImpl implements ProcessFileSignatureService {
    
    @Autowired
    private ProcessFileSignatureMapper signatureMapper;
    
    @Autowired
    private com.zssystem.mapper.ProcessFileMapper processFileMapper;
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    // 签名类型映射
    private static final Map<String, String> SIGNATURE_TYPE_MAP = new HashMap<>();
    
    static {
        SIGNATURE_TYPE_MAP.put("SUBMIT", "提交");
        SIGNATURE_TYPE_MAP.put("APPROVE_LEVEL1", "审核（车间主任）");
        SIGNATURE_TYPE_MAP.put("APPROVE_LEVEL2", "会签（注塑部经理）");
        SIGNATURE_TYPE_MAP.put("APPROVE_LEVEL3", "批准（生产技术部经理）");
    }
    
    @Override
    @Transactional
    public Long saveSignature(ProcessFileSignatureDTO signatureDTO, Long signerId, String signerName, String signerRole) {
        Long fileId = signatureDTO.getFileId();
        
        // 1. 查询工艺文件信息，获取fileNo
        com.zssystem.entity.ProcessFile processFile = processFileMapper.selectById(fileId);
        if (processFile == null) {
            throw new RuntimeException("工艺文件不存在");
        }
        
        // 2. 保存签名图片
        String signatureImagePath = saveSignatureImage(signatureDTO.getSignatureImage(), fileId, signatureDTO.getSignatureType());
        
        // 3. 创建签名记录
        ProcessFileSignature signature = new ProcessFileSignature();
        signature.setFileId(fileId);
        signature.setFileNo(processFile.getFileNo());
        signature.setSignatureType(signatureDTO.getSignatureType());
        signature.setSignerId(signerId);
        signature.setSignerName(signerName);
        signature.setSignerRole(signerRole);
        signature.setSignatureImagePath(signatureImagePath);
        signature.setSignatureTime(LocalDateTime.now());
        signature.setIpAddress(signatureDTO.getIpAddress());
        signature.setDeviceInfo(signatureDTO.getDeviceInfo());
        
        signatureMapper.insert(signature);
        
        // 4. 将签名图片插入到工艺文件Excel中
        try {
            System.out.println("准备将签名插入到Excel文件...");
            System.out.println("工艺文件路径: " + processFile.getFilePath());
            System.out.println("签名图片路径: " + signatureImagePath);
            System.out.println("签名类型: " + signatureDTO.getSignatureType());
            
            boolean success = ProcessFileExcelUtil.insertSignatureToExcel(
                processFile.getFilePath(),
                signatureImagePath,
                signatureDTO.getSignatureType()
            );
            
            if (success) {
                System.out.println("✓ 签名图片已成功插入到Excel文件");
            } else {
                System.err.println("✗ 警告: 签名图片插入Excel失败，但签名记录已保存");
                System.err.println("请检查:");
                System.err.println("  1. Excel文件是否存在: " + processFile.getFilePath());
                System.err.println("  2. Excel文件中是否包含相应的标签（编制人/审核人/会签/批准人）");
                System.err.println("  3. 签名图片是否存在: " + signatureImagePath);
            }
        } catch (Exception e) {
            System.err.println("✗ 插入签名到Excel时发生异常: " + e.getMessage());
            e.printStackTrace();
            // 不抛出异常，因为签名记录已保存，Excel更新失败不影响签名功能
        }
        
        return signature.getId();
    }
    
    /**
     * 保存签名图片
     */
    private String saveSignatureImage(MultipartFile imageFile, Long fileId, String signatureType) {
        try {
            // 创建存储目录：uploads/signatures/年份/月份/
            LocalDateTime now = LocalDateTime.now();
            String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String saveDir = uploadPath.replace("process-files", "signatures") + "/" + yearMonth;
            
            FileUtil.createDirectoryIfNotExists(saveDir);
            
            // 生成文件名：SIG_文件ID_类型_时间戳.png
            String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = String.format("SIG_%d_%s_%s.png", fileId, signatureType, timestamp);
            String filePath = saveDir + "/" + fileName;
            
            // 保存文件
            Path path = Paths.get(filePath);
            Files.write(path, imageFile.getBytes());
            
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("保存签名图片失败: " + e.getMessage(), e);
        }
    }
    
    
    @Override
    public List<ProcessFileSignatureVO> getSignaturesByFileId(Long fileId) {
        LambdaQueryWrapper<ProcessFileSignature> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessFileSignature::getFileId, fileId)
               .orderByAsc(ProcessFileSignature::getSignatureTime);
        
        List<ProcessFileSignature> signatures = signatureMapper.selectList(wrapper);
        return signatures.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public ProcessFileSignatureVO getSignatureByFileIdAndType(Long fileId, String signatureType) {
        ProcessFileSignature signature = signatureMapper.selectByFileIdAndType(fileId, signatureType);
        if (signature == null) {
            return null;
        }
        return convertToVO(signature);
    }
    
    @Override
    public ProcessFileSignatureVO getSignatureById(Long signatureId) {
        ProcessFileSignature signature = signatureMapper.selectById(signatureId);
        if (signature == null) {
            return null;
        }
        return convertToVO(signature);
    }
    
    /**
     * 转换为VO
     */
    private ProcessFileSignatureVO convertToVO(ProcessFileSignature signature) {
        ProcessFileSignatureVO vo = new ProcessFileSignatureVO();
        BeanUtil.copyProperties(signature, vo);
        
        // 设置签名类型文本
        vo.setSignatureTypeText(SIGNATURE_TYPE_MAP.getOrDefault(signature.getSignatureType(), "未知"));
        
        // 设置签名图片访问URL（相对路径，前端需要拼接baseURL）
        vo.setSignatureImageUrl("/api/production/process-file/signature/image/" + signature.getId());
        
        return vo;
    }
}
