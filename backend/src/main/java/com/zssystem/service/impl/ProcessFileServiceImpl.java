package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.ProcessFileApprovalDTO;
import com.zssystem.dto.ProcessFileQueryDTO;
import com.zssystem.dto.ProcessFileUploadDTO;
import com.zssystem.entity.*;
import com.zssystem.mapper.*;
import com.zssystem.service.ProcessFileService;
import com.zssystem.vo.ProcessFileApprovalVO;
import com.zssystem.vo.ProcessFileSealVO;
import com.zssystem.vo.ProcessFileVO;
import com.zssystem.vo.ProcessFileSignatureVO;
import org.springframework.beans.BeanUtils;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工艺文件Service实现类
 */
@Service
public class ProcessFileServiceImpl implements ProcessFileService {

    @Autowired
    private ProcessFileMapper processFileMapper;
    
    @Autowired
    private ProcessFileApprovalMapper approvalMapper;
    
    @Autowired
    private ProcessFileSealMapper sealMapper;
    
    @Autowired
    private EquipmentMapper equipmentMapper;
    
    @Autowired
    private com.zssystem.mapper.ProcessFileDetailMapper processFileDetailMapper;
    
    @Autowired(required = false)
    private com.zssystem.service.ProcessFileSignatureService signatureService;
    
    @Value("${file.upload.path:/data/uploads/process-files}")
    private String uploadPath;
    
    @Value("${file.upload.max-size:10485760}")
    private Long maxFileSize;
    
    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    private static final Map<Integer, String> APPROVAL_LEVEL_MAP = new HashMap<>();
    
    // 审批流程：车间主任审核(1) → 生产技术部经理批准(2) → 注塑部经理会签(3)
    static {
        STATUS_MAP.put(0, "草稿");
        STATUS_MAP.put(1, "待车间主任审核");
        STATUS_MAP.put(2, "待生产技术部经理批准");
        STATUS_MAP.put(3, "待注塑部经理会签");
        STATUS_MAP.put(5, "已批准（生效中）");
        STATUS_MAP.put(-1, "已驳回");
        STATUS_MAP.put(-2, "已作废");
        
        APPROVAL_LEVEL_MAP.put(1, "车间主任审核");
        APPROVAL_LEVEL_MAP.put(2, "生产技术部经理批准");
        APPROVAL_LEVEL_MAP.put(3, "注塑部经理会签");
    }

    @Override
    public IPage<ProcessFileVO> getProcessFileList(ProcessFileQueryDTO queryDTO) {
        Page<ProcessFile> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<ProcessFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getFileNo() != null, ProcessFile::getFileNo, queryDTO.getFileNo())
               .like(queryDTO.getEquipmentNo() != null, ProcessFile::getEquipmentNo, queryDTO.getEquipmentNo())
               .like(queryDTO.getMachineNo() != null, ProcessFile::getMachineNo, queryDTO.getMachineNo())
               .like(queryDTO.getFileName() != null, ProcessFile::getFileName, queryDTO.getFileName())
               .eq(queryDTO.getStatus() != null, ProcessFile::getStatus, queryDTO.getStatus())
               .like(queryDTO.getCreatorName() != null, ProcessFile::getCreatorName, queryDTO.getCreatorName())
               .eq(queryDTO.getVersion() != null, ProcessFile::getVersion, queryDTO.getVersion())
               .eq(queryDTO.getIsCurrent() != null, ProcessFile::getIsCurrent, queryDTO.getIsCurrent())
               .orderByDesc(ProcessFile::getCreateTime);
        
        IPage<ProcessFile> filePage = processFileMapper.selectPage(page, wrapper);
        return filePage.convert(this::convertToVO);
    }

    @Override
    public ProcessFileVO getProcessFileById(Long id) {
        ProcessFile file = processFileMapper.selectById(id);
        if (file == null) {
            throw new RuntimeException("工艺文件不存在");
        }
        
        ProcessFileVO vo = convertToVO(file);
        
        // 查询审批历史
        LambdaQueryWrapper<ProcessFileApproval> approvalWrapper = new LambdaQueryWrapper<>();
        approvalWrapper.eq(ProcessFileApproval::getFileId, id)
                      .orderByAsc(ProcessFileApproval::getApprovalLevel);
        List<ProcessFileApproval> approvalList = approvalMapper.selectList(approvalWrapper);
        vo.setApprovalHistory(approvalList.stream().map(this::convertToApprovalVO).collect(Collectors.toList()));
        
        // 查询受控章信息
        if (file.getStatus() == 5) {
            LambdaQueryWrapper<ProcessFileSeal> sealWrapper = new LambdaQueryWrapper<>();
            sealWrapper.eq(ProcessFileSeal::getFileId, id);
            ProcessFileSeal seal = sealMapper.selectOne(sealWrapper);
            if (seal != null) {
                vo.setSealInfo(convertToSealVO(seal));
            }
        }
        
        return vo;
    }

    @Override
    @Transactional
    public void uploadProcessFile(ProcessFileUploadDTO uploadDTO, Long currentUserId, String currentUserName) throws IOException {
        // 1. 验证文件
        MultipartFile file = uploadDTO.getFile();
        validateFile(file);
        
        // 2. 查询设备信息
        Equipment equipment = equipmentMapper.selectById(uploadDTO.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        
        ProcessFile processFile;
        boolean isUpdate = uploadDTO.getId() != null;
        
        if (isUpdate) {
            // 修改：创建新版本
            ProcessFile oldFile = processFileMapper.selectById(uploadDTO.getId());
            if (oldFile == null) {
                throw new RuntimeException("原工艺文件不存在");
            }
            
            // 验证变更原因
            if (uploadDTO.getChangeReason() == null || uploadDTO.getChangeReason().trim().isEmpty()) {
                throw new RuntimeException("修改工艺文件必须填写变更原因");
            }
            
            // 将原文件标记为历史版本
            oldFile.setIsCurrent(0);
            processFileMapper.updateById(oldFile);
            
            // 创建新版本
            processFile = new ProcessFile();
            processFile.setParentFileId(oldFile.getId());
            processFile.setVersion(oldFile.getVersion() + 1);
            processFile.setChangeReason(uploadDTO.getChangeReason());
        } else {
            // 新建
            processFile = new ProcessFile();
            processFile.setVersion(1);
        }
        
        // 3. 生成文件编号
        String fileNo = generateFileNo();
        
        // 4. 保存文件到磁盘
        String filePath = saveFile(file, fileNo);
        
        // 5. 填充数据
        processFile.setFileNo(fileNo);
        processFile.setEquipmentId(equipment.getId());
        processFile.setEquipmentNo(equipment.getEquipmentNo());
        processFile.setMachineNo(equipment.getMachineNo());
        processFile.setFileName(file.getOriginalFilename());
        processFile.setFilePath(filePath);
        processFile.setFileSize(file.getSize());
        processFile.setFileType(getFileExtension(file.getOriginalFilename()));
        processFile.setStatus(0); // 草稿状态
        processFile.setCreatorId(currentUserId);
        processFile.setCreatorName(currentUserName);
        processFile.setIsCurrent(1);
        processFile.setRemark(uploadDTO.getRemark());
        
        processFileMapper.insert(processFile);
    }
    
    @Override
    @Transactional
    public Long saveProcessFileForm(com.zssystem.dto.ProcessFileFormDTO formDTO, Long currentUserId, String currentUserName) {
        // 1. 查询设备信息
        Equipment equipment = equipmentMapper.selectById(formDTO.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        
        ProcessFile processFile;
        ProcessFileDetail processFileDetail;
        boolean isUpdate = formDTO.getId() != null;
        
        if (isUpdate) {
            // 修改：创建新版本
            ProcessFile oldFile = processFileMapper.selectById(formDTO.getId());
            if (oldFile == null) {
                throw new RuntimeException("原工艺文件不存在");
            }
            
            // 验证变更原因
            if (formDTO.getChangeReason() == null || formDTO.getChangeReason().trim().isEmpty()) {
                throw new RuntimeException("修改工艺文件必须填写变更原因");
            }
            
            // 将原文件标记为历史版本
            oldFile.setIsCurrent(0);
            processFileMapper.updateById(oldFile);
            
            // 创建新版本
            processFile = new ProcessFile();
            processFile.setParentFileId(oldFile.getId());
            processFile.setVersion(oldFile.getVersion() + 1);
            processFile.setChangeReason(formDTO.getChangeReason());
            
            // 查询原详细内容
            com.zssystem.entity.ProcessFileDetail oldDetail = processFileDetailMapper.selectOne(
                new LambdaQueryWrapper<com.zssystem.entity.ProcessFileDetail>()
                    .eq(com.zssystem.entity.ProcessFileDetail::getFileId, oldFile.getId())
                    .last("LIMIT 1")
            );
            
            processFileDetail = new com.zssystem.entity.ProcessFileDetail();
            if (oldDetail != null) {
                BeanUtils.copyProperties(oldDetail, processFileDetail);
                processFileDetail.setId(null);
            }
        } else {
            // 新建
            processFile = new ProcessFile();
            processFile.setVersion(1);
            processFileDetail = new com.zssystem.entity.ProcessFileDetail();
        }
        
        // 2. 文件编号与文件名称
        String fileNo;
        String fileName;
        if (isUpdate) {
            fileNo = generateFileNo();
            fileName = "注塑工艺卡片_" + fileNo + ".xlsx";
        } else {
            // 新建：使用表单手动填写的工艺文件编号和文件名称
            String rawNo = formDTO.getFileNo();
            String rawName = formDTO.getFileName();
            if (rawNo == null || rawNo.trim().isEmpty()) {
                throw new RuntimeException("请输入工艺文件编号");
            }
            if (rawName == null || rawName.trim().isEmpty()) {
                throw new RuntimeException("请输入文件名称");
            }
            fileNo = rawNo.trim();
            fileName = rawName.trim();
            if (fileNo.contains("/") || fileNo.contains("\\") || fileNo.contains("..")) {
                throw new RuntimeException("工艺文件编号不能包含 / \\ 或 ..");
            }
        }
        
        // 3. 填充工艺文件主表数据
        processFile.setFileNo(fileNo);
        processFile.setEquipmentId(equipment.getId());
        processFile.setEquipmentNo(equipment.getEquipmentNo());
        processFile.setMachineNo(equipment.getMachineNo());
        processFile.setFileName(fileName);
        processFile.setFilePath(""); // 表单方式不需要文件路径
        processFile.setFileSize(0L);
        processFile.setFileType("form"); // 标识为表单方式
        processFile.setStatus(0); // 草稿状态
        processFile.setCreatorId(currentUserId);
        processFile.setCreatorName(currentUserName);
        processFile.setIsCurrent(1);
        processFile.setRemark(formDTO.getRemark());
        
        processFileMapper.insert(processFile);
        
        // 4. 填充详细内容数据
        BeanUtils.copyProperties(formDTO, processFileDetail);
        processFileDetail.setFileId(processFile.getId());
        processFileDetail.setFileNo(fileNo);
        processFileDetail.setEquipmentId(equipment.getId());
        processFileDetail.setEquipmentNo(equipment.getEquipmentNo());
        processFileDetail.setMachineNo(equipment.getMachineNo());
        processFileDetail.setEquipmentName(equipment.getEquipmentName());
        
        if (isUpdate && processFileDetail.getId() != null) {
            processFileDetailMapper.updateById(processFileDetail);
        } else {
            processFileDetailMapper.insert(processFileDetail);
        }
        
        return processFile.getId();
    }
    
    @Override
    @Transactional
    public Long saveProcessFileForm(com.zssystem.dto.ProcessFileFormDTO formDTO, Long currentUserId, String currentUserName,
                                   org.springframework.web.multipart.MultipartFile keyDimensionImage1,
                                   org.springframework.web.multipart.MultipartFile keyDimensionImage2) {
        // 1. 处理产品关键尺寸图片上传
        String image1Path = null;
        String image2Path = null;
        
        if (keyDimensionImage1 != null && !keyDimensionImage1.isEmpty()) {
            image1Path = saveKeyDimensionImage(keyDimensionImage1, "image1");
            formDTO.setProductKeyDimensionImage1(image1Path);
        }
        
        if (keyDimensionImage2 != null && !keyDimensionImage2.isEmpty()) {
            image2Path = saveKeyDimensionImage(keyDimensionImage2, "image2");
            formDTO.setProductKeyDimensionImage2(image2Path);
        }
        
        // 2. 调用原有的保存方法
        return saveProcessFileForm(formDTO, currentUserId, currentUserName);
    }
    
    /**
     * 保存产品关键尺寸图片
     */
    private String saveKeyDimensionImage(org.springframework.web.multipart.MultipartFile file, String imageType) {
        try {
            // 生成保存路径
            String yearMonth = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM"));
            String saveDir = uploadPath + "/key-dimensions/" + yearMonth;
            com.zssystem.util.FileUtil.createDirectoryIfNotExists(saveDir);
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".png";
            String fileName = "KD_" + imageType + "_" + System.currentTimeMillis() + extension;
            String filePath = saveDir + "/" + fileName;
            
            // 保存文件
            file.transferTo(new java.io.File(filePath));
            
            System.out.println("产品关键尺寸图片保存成功: " + filePath);
            return filePath;
        } catch (Exception e) {
            System.err.println("保存产品关键尺寸图片失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("保存图片失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void submitForApproval(Long fileId, Long currentUserId) {
        ProcessFile processFile = processFileMapper.selectById(fileId);
        if (processFile == null) {
            throw new RuntimeException("工艺文件不存在");
        }
        
        if (!processFile.getCreatorId().equals(currentUserId)) {
            throw new RuntimeException("只有创建人才能提交审批");
        }
        
        if (processFile.getStatus() != 0) {
            throw new RuntimeException("只有草稿状态才能提交审批");
        }
        
        // Excel文件在下载时生成，提交时只更新状态
        processFile.setStatus(1); // 待车间主任审核
        processFile.setSubmitTime(LocalDateTime.now());
        processFileMapper.updateById(processFile);
    }
    
    @Override
    @Transactional
    public void approveProcessFile(ProcessFileApprovalDTO approvalDTO, Long currentUserId, 
                                   String currentUserName, String currentUserRole, Long signatureId) {
        ProcessFile processFile = processFileMapper.selectById(approvalDTO.getFileId());
        if (processFile == null) {
            throw new RuntimeException("工艺文件不存在");
        }
        
        // 验证审批权限
        int currentLevel = getCurrentApprovalLevel(processFile.getStatus());
        validateApprovalPermission(currentLevel, currentUserRole);
        
        // 保存审批记录
        ProcessFileApproval approval = new ProcessFileApproval();
        approval.setFileId(processFile.getId());
        approval.setFileNo(processFile.getFileNo());
        approval.setApprovalLevel(currentLevel);
        approval.setApproverId(currentUserId);
        approval.setApproverName(currentUserName);
        approval.setApproverRole(currentUserRole);
        approval.setApprovalResult(approvalDTO.getApprovalResult());
        approval.setApprovalOpinion(approvalDTO.getApprovalOpinion());
        approval.setApprovalTime(LocalDateTime.now());
        approval.setSignatureId(signatureId); // 关联电子签名
        approvalMapper.insert(approval);
        
        // 更新工艺文件状态
        if (approvalDTO.getApprovalResult() == 1) {
            // 通过
            int nextStatus = getNextStatus(processFile.getStatus());
            processFile.setStatus(nextStatus);
            
            // 如果是最终批准，生成电子受控章
            if (nextStatus == 5) {
                processFile.setApprovalTime(LocalDateTime.now());
                processFile.setEffectiveTime(LocalDateTime.now());
                generateSeal(processFile, currentUserId, currentUserName);
            }
            
            // 签名会在下载Excel时统一插入，这里只保存审批记录
        } else {
            // 驳回
            processFile.setStatus(-1);
        }
        
        processFileMapper.updateById(processFile);
    }
    
    @Override
    @Transactional
    public void invalidateProcessFile(Long fileId, Long currentUserId) {
        ProcessFile processFile = processFileMapper.selectById(fileId);
        if (processFile == null) {
            throw new RuntimeException("工艺文件不存在");
        }
        
        processFile.setStatus(-2); // 已作废
        processFile.setInvalidTime(LocalDateTime.now());
        processFileMapper.updateById(processFile);
    }
    
    @Override
    @Transactional
    public int batchInvalidateByEquipmentId(Long equipmentId, Long currentUserId) {
        if (equipmentId == null) {
            throw new RuntimeException("请选择设备（机台）");
        }
        List<ProcessFile> list = processFileMapper.selectList(
            new LambdaQueryWrapper<ProcessFile>()
                .eq(ProcessFile::getEquipmentId, equipmentId)
                .notIn(ProcessFile::getStatus, -2) // 排除已作废的
        );
        LocalDateTime now = LocalDateTime.now();
        for (ProcessFile pf : list) {
            pf.setStatus(-2);
            pf.setInvalidTime(now);
            processFileMapper.updateById(pf);
        }
        return list.size();
    }
    
    @Override
    public byte[] downloadProcessFile(Long fileId) throws IOException {
        System.out.println("开始下载工艺文件，ID: " + fileId);
        
        ProcessFile file = processFileMapper.selectById(fileId);
        if (file == null) {
            System.err.println("工艺文件不存在，ID: " + fileId);
            throw new RuntimeException("文件不存在");
        }
        
        System.out.println("工艺文件信息 - 文件类型: " + file.getFileType() + ", 文件路径: " + file.getFilePath());
        
        String excelFilePath = file.getFilePath();
        boolean needGenerate = false;
        
        // 判断是否需要生成Excel
        if ("form".equals(file.getFileType())) {
            System.out.println("检测到表单方式，需要生成Excel");
            needGenerate = true;
        } else if (excelFilePath == null || excelFilePath.isEmpty()) {
            System.out.println("文件路径为空，需要生成Excel");
            needGenerate = true;
        } else {
            Path existingPath = Paths.get(excelFilePath);
            if (!Files.exists(existingPath)) {
                System.out.println("文件不存在: " + excelFilePath + "，需要生成Excel");
                needGenerate = true;
            } else {
                System.out.println("Excel文件已存在: " + excelFilePath);
            }
        }
        
        // 如果需要生成Excel
        if (needGenerate) {
            try {
                // 查询详细内容
                System.out.println("查询工艺文件详细内容...");
                com.zssystem.entity.ProcessFileDetail detail = processFileDetailMapper.selectOne(
                    new LambdaQueryWrapper<com.zssystem.entity.ProcessFileDetail>()
                        .eq(com.zssystem.entity.ProcessFileDetail::getFileId, fileId)
                        .last("LIMIT 1")
                );
                
                if (detail == null) {
                    System.err.println("工艺文件详细内容不存在，文件ID: " + fileId);
                    throw new RuntimeException("工艺文件详细内容不存在");
                }
                
                System.out.println("工艺文件详细内容查询成功");
                
                // 查询设备信息
                System.out.println("查询设备信息，设备ID: " + file.getEquipmentId());
                Equipment equipment = equipmentMapper.selectById(file.getEquipmentId());
                if (equipment == null) {
                    System.err.println("设备信息不存在，设备ID: " + file.getEquipmentId());
                    throw new RuntimeException("设备信息不存在");
                }
                
                System.out.println("设备信息查询成功: " + equipment.getEquipmentName());
                
                // 生成Excel文件
                System.out.println("开始生成Excel文件，保存路径: " + uploadPath);
                excelFilePath = com.zssystem.util.ProcessFileExcelGenerator.generateProcessFileExcel(
                    file,
                    detail,
                    equipment,
                    uploadPath
                );
                
                System.out.println("Excel文件生成成功: " + excelFilePath);
                
                // 更新文件路径
                file.setFilePath(excelFilePath);
                java.io.File excelFile = new java.io.File(excelFilePath);
                if (!excelFile.exists()) {
                    System.err.println("生成的Excel文件不存在: " + excelFilePath);
                    throw new RuntimeException("Excel文件生成失败");
                }
                
                file.setFileSize(excelFile.length());
                file.setFileType("xlsx");
                processFileMapper.updateById(file);
                System.out.println("文件信息已更新到数据库");
                
                // 插入所有已保存的签名
                if (signatureService != null) {
                    try {
                        System.out.println("开始插入签名到Excel...");
                        // 查询所有签名
                        List<com.zssystem.vo.ProcessFileSignatureVO> signatures = signatureService.getSignaturesByFileId(fileId);
                        System.out.println("查询到 " + (signatures != null ? signatures.size() : 0) + " 个签名");
                        
                        if (signatures != null && !signatures.isEmpty()) {
                            System.out.println("========== 开始处理签名列表 ==========");
                            for (int i = 0; i < signatures.size(); i++) {
                                com.zssystem.vo.ProcessFileSignatureVO signature = signatures.get(i);
                                String signatureType = signature.getSignatureType();
                                String signatureImagePath = signature.getSignatureImagePath();
                                
                                System.out.println("签名[" + (i+1) + "/" + signatures.size() + "] - 类型: " + signatureType + ", 路径: " + signatureImagePath);
                                
                                if (signatureImagePath != null && !signatureImagePath.isEmpty()) {
                                    // 检查签名图片文件是否存在
                                    java.io.File signatureFile = new java.io.File(signatureImagePath);
                                    if (!signatureFile.exists()) {
                                        System.err.println("签名图片文件不存在: " + signatureImagePath);
                                        continue;
                                    }
                                    
                                    // 插入签名到Excel
                                    com.zssystem.util.ProcessFileExcelGenerator.insertSignatureToGeneratedExcel(
                                        excelFilePath,
                                        signatureImagePath,
                                        signatureType
                                    );
                                    System.out.println("签名插入成功: " + signatureType);
                                }
                            }
                        }
                        
                        System.out.println("所有签名已插入到Excel");
                    } catch (Exception e) {
                        System.err.println("插入签名到Excel失败: " + e.getMessage());
                        e.printStackTrace();
                        // 不阻止下载，但记录错误
                    }
                } else {
                    System.out.println("签名服务未注入，跳过签名插入");
                }
            } catch (Exception e) {
                System.err.println("生成Excel文件失败: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("生成Excel文件失败: " + e.getMessage(), e);
            }
        } else {
            // Excel文件已存在，检查是否需要插入签名
            if (signatureService != null) {
                try {
                    System.out.println("Excel文件已存在，检查是否需要插入签名...");
                    List<com.zssystem.vo.ProcessFileSignatureVO> signatures = signatureService.getSignaturesByFileId(fileId);
                    
                    if (signatures != null && !signatures.isEmpty()) {
                        System.out.println("发现 " + signatures.size() + " 个签名，检查是否已插入...");
                        
                        // 检查Excel中是否已有签名（简单检查：如果文件最近被修改过，可能已插入）
                        // 为了确保签名总是最新的，每次都重新插入
                        for (com.zssystem.vo.ProcessFileSignatureVO signature : signatures) {
                            String signatureType = signature.getSignatureType();
                            String signatureImagePath = signature.getSignatureImagePath();
                            
                            if (signatureImagePath != null && !signatureImagePath.isEmpty()) {
                                java.io.File signatureFile = new java.io.File(signatureImagePath);
                                if (signatureFile.exists()) {
                                    System.out.println("插入签名到已存在的Excel: " + signatureType);
                                    com.zssystem.util.ProcessFileExcelGenerator.insertSignatureToGeneratedExcel(
                                        excelFilePath,
                                        signatureImagePath,
                                        signatureType
                                    );
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("插入签名到已存在的Excel失败: " + e.getMessage());
                    e.printStackTrace();
                    // 不阻止下载，但记录错误
                }
            }
        }
        
        // 会签完成（状态5）时在 Excel 区域 L24:P27 加盖受控章
        if (file.getStatus() != null && file.getStatus() == 5) {
            try {
                com.zssystem.util.ProcessFileExcelUtil.insertControlledSealToExcel(excelFilePath);
            } catch (Exception e) {
                System.err.println("插入受控章失败: " + e.getMessage());
            }
        }
        
        // 读取Excel文件
        System.out.println("读取Excel文件: " + excelFilePath);
        Path path = Paths.get(excelFilePath);
        if (!Files.exists(path)) {
            System.err.println("Excel文件不存在: " + excelFilePath);
            throw new RuntimeException("Excel文件生成失败或不存在: " + excelFilePath);
        }
        
        byte[] fileContent = Files.readAllBytes(path);
        System.out.println("文件读取成功，大小: " + fileContent.length + " 字节");
        
        return fileContent;
    }
    
    @Override
    public IPage<ProcessFileVO> getProcessFilesByEquipment(Long equipmentId, Integer pageNum, Integer pageSize) {
        Page<ProcessFile> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<ProcessFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessFile::getEquipmentId, equipmentId)
               .orderByDesc(ProcessFile::getCreateTime);
        
        IPage<ProcessFile> filePage = processFileMapper.selectPage(page, wrapper);
        return filePage.convert(this::convertToVO);
    }
    
    @Override
    public IPage<ProcessFileVO> getPendingApprovalFiles(String userRole, Integer pageNum, Integer pageSize) {
        Page<ProcessFile> page = new Page<>(pageNum, pageSize);
        
        // 根据角色确定状态
        Integer status = getStatusByRole(userRole);
        System.out.println("getPendingApprovalFiles - 用户角色: " + userRole + ", 查询状态: " + status);
        
        if (status == null) {
            System.out.println("getPendingApprovalFiles - 用户角色不匹配，返回空列表");
            return new Page<>(pageNum, pageSize); // 返回空列表
        }
        
        LambdaQueryWrapper<ProcessFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessFile::getStatus, status)
               .orderByAsc(ProcessFile::getSubmitTime);
        
        IPage<ProcessFile> filePage = processFileMapper.selectPage(page, wrapper);
        System.out.println("getPendingApprovalFiles - 查询到 " + filePage.getTotal() + " 条待审批文件");
        return filePage.convert(this::convertToVO);
    }
    
    /**
     * 生成电子受控章
     */
    private void generateSeal(ProcessFile processFile, Long sealById, String sealByName) {
        String sealNo = "SEAL" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String sealImagePath = "/seals/" + processFile.getFileNo() + "_seal.png";
        
        // TODO: 实际生成受控章图片的逻辑
        
        ProcessFileSeal seal = new ProcessFileSeal();
        seal.setFileId(processFile.getId());
        seal.setFileNo(processFile.getFileNo());
        seal.setSealNo(sealNo);
        seal.setSealType("受控章");
        seal.setSealContent("已批准生效");
        seal.setSealImagePath(sealImagePath);
        seal.setSealTime(LocalDateTime.now());
        seal.setSealById(sealById);
        seal.setSealByName(sealByName);
        
        sealMapper.insert(seal);
        
        processFile.setSealImagePath(sealImagePath);
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("文件大小超过限制：" + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xls") && !filename.endsWith(".xlsx"))) {
            throw new RuntimeException("只支持Excel文件格式（.xls, .xlsx）");
        }
    }
    
    /**
     * 生成文件编号
     */
    private synchronized String generateFileNo() {
        String prefix = "PF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 查询当天最大序号
        String maxFileNo = processFileMapper.getMaxFileNoByPrefix(prefix);
        int sequence = 1;
        if (maxFileNo != null && maxFileNo.length() > prefix.length()) {
            try {
                sequence = Integer.parseInt(maxFileNo.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        return prefix + String.format("%03d", sequence);
    }
    
    /**
     * 保存文件到磁盘
     */
    private String saveFile(MultipartFile file, String fileNo) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = now.format(DateTimeFormatter.ofPattern("MM"));
        
        String dirPath = uploadPath + File.separator + year + File.separator + month;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = fileNo + "." + extension;
        String filePath = dirPath + File.separator + filename;
        
        file.transferTo(new File(filePath));
        
        return filePath;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    /**
     * 获取当前审批级别
     * 审批流程：车间主任审核(状态1) → 生产技术部经理批准(状态2) → 注塑部经理会签(状态3)
     */
    private int getCurrentApprovalLevel(int status) {
        return switch (status) {
            case 1 -> 1; // 车间主任审核
            case 2 -> 2; // 生产技术部经理批准
            case 3 -> 3; // 注塑部经理会签
            default -> throw new RuntimeException("当前状态不需要审批");
        };
    }
    
    /**
     * 根据审批级别获取签名类型
     * APPROVE_LEVEL1=审核人(车间主任), APPROVE_LEVEL2=批准人(生产技术部经理), APPROVE_LEVEL3=会签(注塑部经理)
     */
    private String getSignatureTypeByApprovalLevel(int approvalLevel) {
        return switch (approvalLevel) {
            case 1 -> "APPROVE_LEVEL1"; // 审核人（车间主任）
            case 2 -> "APPROVE_LEVEL2"; // 批准人（生产技术部经理）
            case 3 -> "APPROVE_LEVEL3"; // 会签（注塑部经理）
            default -> null;
        };
    }
    
    /**
     * 验证审批权限
     * 支持角色代码和角色名称两种方式
     */
    private void validateApprovalPermission(int approvalLevel, String userRole) {
        // 使用角色常量类获取对应审批级别的角色代码和名称
        String requiredRoleCode = com.zssystem.constant.ProcessFileRoleConstant.getRoleCodeByApprovalLevel(approvalLevel);
        String requiredRoleName = com.zssystem.constant.ProcessFileRoleConstant.getRoleNameByApprovalLevel(approvalLevel);
        
        // 检查用户角色代码或角色名称是否匹配
        boolean hasPermission = false;
        
        // 方式1：通过角色代码检查（优先）
        List<String> userRoleCodes = com.zssystem.util.SecurityUtil.getCurrentUserRoleCodes();
        if (userRoleCodes != null && userRoleCodes.contains(requiredRoleCode)) {
            hasPermission = true;
        }
        
        // 方式2：通过角色名称检查（兼容旧代码）
        if (!hasPermission && userRole != null) {
            if (requiredRoleName.equals(userRole)) {
                hasPermission = true;
            }
        }
        
        if (!hasPermission) {
            throw new RuntimeException("您没有权限进行此审批操作，需要" + requiredRoleName + "角色");
        }
    }
    
    /**
     * 获取下一个状态
     * 审批流程：车间主任审核(1) → 生产技术部经理批准(2) → 注塑部经理会签(3) → 已批准(5)
     */
    private int getNextStatus(int currentStatus) {
        return switch (currentStatus) {
            case 1 -> 2; // 车间主任审核通过 → 待生产技术部经理批准
            case 2 -> 3; // 生产技术部经理批准通过 → 待注塑部经理会签
            case 3 -> 5; // 注塑部经理会签通过 → 已批准（生效中）
            default -> throw new RuntimeException("状态流转异常");
        };
    }
    
    /**
     * 根据角色获取待审批状态
     * 支持角色代码和角色名称两种方式
     */
    private Integer getStatusByRole(String userRole) {
        // 优先通过角色代码判断
        List<String> userRoleCodes = com.zssystem.util.SecurityUtil.getCurrentUserRoleCodes();
        System.out.println("getStatusByRole - 用户角色代码列表: " + userRoleCodes);
        System.out.println("getStatusByRole - 用户角色名称: " + userRole);
        
        if (userRoleCodes != null && !userRoleCodes.isEmpty()) {
            if (userRoleCodes.contains(com.zssystem.constant.ProcessFileRoleConstant.ROLE_CODE_WORKSHOP_DIRECTOR)) {
                System.out.println("getStatusByRole - 匹配到车间主任角色，返回状态1");
                return 1; // 车间主任审核
            }
            if (userRoleCodes.contains(com.zssystem.constant.ProcessFileRoleConstant.ROLE_CODE_PRODUCTION_TECH_MANAGER)) {
                System.out.println("getStatusByRole - 匹配到生产技术部经理角色，返回状态2");
                return 2; // 生产技术部经理批准
            }
            if (userRoleCodes.contains(com.zssystem.constant.ProcessFileRoleConstant.ROLE_CODE_INJECTION_MANAGER)) {
                System.out.println("getStatusByRole - 匹配到注塑部经理角色，返回状态3");
                return 3; // 注塑部经理会签
            }
        }
        
        // 兼容旧代码：通过角色名称判断
        // 审批流程：车间主任审核(1) → 生产技术部经理批准(2) → 注塑部经理会签(3)
        if (userRole != null) {
            Integer status = switch (userRole) {
                case "车间主任" -> 1;
                case "生产技术部经理" -> 2;
                case "注塑部经理" -> 3;
                default -> null;
            };
            if (status != null) {
                System.out.println("getStatusByRole - 通过角色名称匹配，返回状态" + status);
            } else {
                System.out.println("getStatusByRole - 角色名称不匹配: " + userRole);
            }
            return status;
        }
        
        System.out.println("getStatusByRole - 无法确定状态，返回null");
        return null;
    }
    
    /**
     * 转换为VO
     */
    private ProcessFileVO convertToVO(ProcessFile file) {
        ProcessFileVO vo = new ProcessFileVO();
        BeanUtils.copyProperties(file, vo);
        
        // 格式化文件大小
        vo.setFileSizeText(formatFileSize(file.getFileSize()));
        
        // 格式化版本号
        vo.setVersionText("V" + file.getVersion() + ".0");
        
        // 状态文本
        vo.setStatusText(STATUS_MAP.getOrDefault(file.getStatus(), "未知"));
        
        return vo;
    }
    
    /**
     * 转换审批记录为VO
     */
    private ProcessFileApprovalVO convertToApprovalVO(ProcessFileApproval approval) {
        ProcessFileApprovalVO vo = new ProcessFileApprovalVO();
        BeanUtils.copyProperties(approval, vo);
        
        vo.setApprovalLevelText(APPROVAL_LEVEL_MAP.getOrDefault(approval.getApprovalLevel(), "未知"));
        vo.setApprovalResultText(approval.getApprovalResult() == 1 ? "通过" : "驳回");
        
        // 查询关联的电子签名
        if (approval.getSignatureId() != null && signatureService != null) {
            try {
                com.zssystem.vo.ProcessFileSignatureVO signature = signatureService.getSignatureById(approval.getSignatureId());
                vo.setSignatureInfo(signature);
            } catch (Exception e) {
                // 签名不存在或查询失败，不影响审批记录显示
                System.out.println("查询签名失败: " + e.getMessage());
            }
        }
        
        return vo;
    }
    
    /**
     * 转换受控章为VO
     */
    private ProcessFileSealVO convertToSealVO(ProcessFileSeal seal) {
        ProcessFileSealVO vo = new ProcessFileSealVO();
        BeanUtils.copyProperties(seal, vo);
        return vo;
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        final long k = 1024;
        final String[] sizes = {"B", "KB", "MB", "GB"};
        final int i = (int) (Math.log(bytes) / Math.log(k));
        return String.format("%.2f %s", bytes / Math.pow(k, i), sizes[i]);
    }
}
