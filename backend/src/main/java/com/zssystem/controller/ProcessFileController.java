package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.ProcessFileApprovalDTO;
import com.zssystem.dto.ProcessFileQueryDTO;
import com.zssystem.dto.ProcessFileUploadDTO;
import com.zssystem.dto.ProcessFileSignatureDTO;
import com.zssystem.service.ProcessFileService;
import com.zssystem.service.ProcessFileSignatureService;
import com.zssystem.vo.ProcessFileVO;
import com.zssystem.vo.ProcessFileSignatureVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.zssystem.util.SecurityUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 工艺文件Controller
 */
@RestController
@RequestMapping("/api/production/process-file")
@Validated
public class ProcessFileController {

    @Autowired
    private ProcessFileService processFileService;
    
    @Autowired
    private ProcessFileSignatureService signatureService;

    /**
     * 分页查询工艺文件列表
     */
    @GetMapping("/list")
    public Result<PageResult<ProcessFileVO>> getList(@Validated ProcessFileQueryDTO queryDTO) {
        IPage<ProcessFileVO> page = processFileService.getProcessFileList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询工艺文件详情
     */
    @GetMapping("/{id}")
    public Result<ProcessFileVO> getById(@PathVariable Long id) {
        ProcessFileVO vo = processFileService.getProcessFileById(id);
        return Result.success(vo);
    }

    /**
     * 上传工艺文件（新建或修改）- Excel文件方式
     */
    @PostMapping("/upload")
    public Result<Void> upload(@Validated @ModelAttribute ProcessFileUploadDTO uploadDTO) {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            String currentUserName = SecurityUtil.getCurrentUsername();
            
            if (currentUserId == null || currentUserName == null) {
                return Result.error("用户未登录");
            }
            
            processFileService.uploadProcessFile(uploadDTO, currentUserId, currentUserName);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 保存工艺文件表单（新建或修改）- 表单方式
     */
    @PostMapping("/save-form")
    public Result<Long> saveForm(@Validated @RequestBody com.zssystem.dto.ProcessFileFormDTO formDTO) {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            String currentUserName = SecurityUtil.getCurrentUsername();
            
            if (currentUserId == null || currentUserName == null) {
                return Result.error("用户未登录");
            }
            
            Long fileId = processFileService.saveProcessFileForm(formDTO, currentUserId, currentUserName);
            
            return Result.success(fileId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 提交审批（需要电子签名）
     */
    @PostMapping("/{id}/submit")
    public Result<Void> submit(
            @PathVariable Long id,
            @RequestParam("signatureImage") MultipartFile signatureImage,
            HttpServletRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String currentUserName = SecurityUtil.getCurrentUsername();
        String currentUserRole = SecurityUtil.getCurrentUserRole();
        
        if (currentUserId == null || currentUserName == null) {
            return Result.error("用户未登录");
        }
        
        try {
            // 1. 保存电子签名
            ProcessFileSignatureDTO signatureDTO = new ProcessFileSignatureDTO();
            signatureDTO.setFileId(id);
            signatureDTO.setSignatureType("SUBMIT");
            signatureDTO.setSignatureImage(signatureImage);
            signatureDTO.setIpAddress(getClientIpAddress(request));
            signatureDTO.setDeviceInfo(request.getHeader("User-Agent"));
            
            // 1. 保存电子签名
            signatureService.saveSignature(
                signatureDTO, 
                currentUserId, 
                currentUserName, 
                currentUserRole != null ? currentUserRole : "注塑组长"
            );
            
            // 2. 提交审批（Excel在下载时生成，签名也会在下载时插入）
            processFileService.submitForApproval(id, currentUserId);
            
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 审批工艺文件（需要电子签名）
     */
    @PostMapping("/approve")
    public Result<Void> approve(
            @RequestParam("fileId") Long fileId,
            @RequestParam("approvalResult") Integer approvalResult,
            @RequestParam(value = "approvalOpinion", required = false) String approvalOpinion,
            @RequestParam(value = "signatureImage", required = false) org.springframework.web.multipart.MultipartFile signatureImage,
            HttpServletRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String currentUserName = SecurityUtil.getCurrentUsername();
        String currentUserRole = SecurityUtil.getCurrentUserRole();
        
        if (currentUserId == null || currentUserName == null || currentUserRole == null) {
            return Result.error("用户未登录或角色信息缺失");
        }
        
        try {
            Long signatureId = null;
            
            // 1. 如果有签名数据，先保存电子签名
            if (signatureImage != null && !signatureImage.isEmpty()) {
                // 确定签名类型
                ProcessFileVO fileInfo = processFileService.getProcessFileById(fileId);
                String signatureType = getSignatureTypeByStatus(fileInfo.getStatus());
                
                ProcessFileSignatureDTO signatureDTO = new ProcessFileSignatureDTO();
                signatureDTO.setFileId(fileId);
                signatureDTO.setSignatureType(signatureType);
                signatureDTO.setSignatureImage(signatureImage);
                signatureDTO.setIpAddress(getClientIpAddress(request));
                signatureDTO.setDeviceInfo(request.getHeader("User-Agent"));
                
                signatureId = signatureService.saveSignature(
                    signatureDTO, 
                    currentUserId, 
                    currentUserName, 
                    currentUserRole
                );
            }
            
            // 2. 构建审批DTO
            ProcessFileApprovalDTO approvalDTO = new ProcessFileApprovalDTO();
            approvalDTO.setFileId(fileId);
            approvalDTO.setApprovalResult(approvalResult);
            approvalDTO.setApprovalOpinion(approvalOpinion);
            
            // 3. 执行审批（将签名ID传递给审批服务）
            processFileService.approveProcessFile(approvalDTO, currentUserId, currentUserName, currentUserRole, signatureId);
            
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据状态获取签名类型
     */
    private String getSignatureTypeByStatus(Integer status) {
        return switch (status) {
            case 1 -> "APPROVE_LEVEL1"; // 待车间主任审核
            case 2 -> "APPROVE_LEVEL2"; // 待注塑部经理会签
            case 3 -> "APPROVE_LEVEL3"; // 待生产技术部经理批准
            default -> throw new RuntimeException("无效的状态");
        };
    }

    /**
     * 作废工艺文件
     */
    @PostMapping("/{id}/invalidate")
    public Result<Void> invalidate(@PathVariable Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        
        processFileService.invalidateProcessFile(id, currentUserId);
        return Result.success();
    }

    /**
     * 下载工艺文件
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        try {
            System.out.println("收到下载请求，文件ID: " + id);
            
            ProcessFileVO fileInfo = processFileService.getProcessFileById(id);
            if (fileInfo == null) {
                System.err.println("文件信息不存在，ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("文件信息: " + fileInfo.getFileName());
            
            byte[] fileContent = processFileService.downloadProcessFile(id);
            
            if (fileContent == null || fileContent.length == 0) {
                System.err.println("文件内容为空");
                return ResponseEntity.internalServerError().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            // 处理中文文件名编码，使用RFC 5987格式支持中文文件名
            String fileName = fileInfo.getFileName();
            if (fileName == null || fileName.isEmpty()) {
                fileName = "工艺文件_" + id + ".xlsx";
            }
            
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8")
                    .replaceAll("\\+", "%20");
            // 使用RFC 5987格式，同时兼容旧浏览器
            headers.add("Content-Disposition", 
                    "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);
            
            System.out.println("文件下载成功，大小: " + fileContent.length + " 字节");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
        } catch (Exception e) {
            System.err.println("下载文件失败: " + e.getMessage());
            e.printStackTrace();
            // 不设置包含中文的错误头，避免编码问题
            return ResponseEntity.status(500)
                    .body(("下载失败: " + e.getMessage()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * 查询设备的工艺文件列表
     */
    @GetMapping("/equipment/{equipmentId}")
    public Result<PageResult<ProcessFileVO>> getByEquipment(
            @PathVariable Long equipmentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<ProcessFileVO> page = processFileService.getProcessFilesByEquipment(equipmentId, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取待审批的工艺文件列表
     */
    @GetMapping("/pending-approval")
    public Result<PageResult<ProcessFileVO>> getPendingApproval(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String currentUserRole = SecurityUtil.getCurrentUserRole();
        String currentUserRoleCode = SecurityUtil.getCurrentUserRoleCode();
        java.util.List<String> currentUserRoleCodes = SecurityUtil.getCurrentUserRoleCodes();
        
        System.out.println("待审批查询 - 用户ID: " + currentUserId);
        System.out.println("待审批查询 - 用户角色名称: " + currentUserRole);
        System.out.println("待审批查询 - 用户角色代码: " + currentUserRoleCode);
        System.out.println("待审批查询 - 用户所有角色代码: " + currentUserRoleCodes);
        
        if (currentUserRole == null && (currentUserRoleCodes == null || currentUserRoleCodes.isEmpty())) {
            return Result.error("用户角色信息缺失，请先为用户分配角色");
        }
        
        IPage<ProcessFileVO> page = processFileService.getPendingApprovalFiles(currentUserRole, pageNum, pageSize);
        System.out.println("待审批查询 - 查询结果数量: " + page.getTotal());
        return Result.success(PageResult.of(page));
    }

    /**
     * 删除工艺文件
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        
        processFileService.invalidateProcessFile(id, currentUserId);
        return Result.success();
    }
    
    /**
     * 上传电子签名（独立接口）
     */
    @PostMapping("/signature")
    public Result<Long> uploadSignature(
            @Validated @ModelAttribute ProcessFileSignatureDTO signatureDTO,
            HttpServletRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String currentUserName = SecurityUtil.getCurrentUsername();
        String currentUserRole = SecurityUtil.getCurrentUserRole();
        
        if (currentUserId == null || currentUserName == null) {
            return Result.error("用户未登录");
        }
        
        try {
            signatureDTO.setIpAddress(getClientIpAddress(request));
            signatureDTO.setDeviceInfo(request.getHeader("User-Agent"));
            
            Long signatureId = signatureService.saveSignature(
                signatureDTO, 
                currentUserId, 
                currentUserName, 
                currentUserRole != null ? currentUserRole : "未知"
            );
            
            return Result.success(signatureId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询文件的电子签名列表
     */
    @GetMapping("/{id}/signatures")
    public Result<List<ProcessFileSignatureVO>> getSignatures(@PathVariable Long id) {
        List<ProcessFileSignatureVO> signatures = signatureService.getSignaturesByFileId(id);
        return Result.success(signatures);
    }
    
    /**
     * 查看签名图片
     */
    @GetMapping("/signature/image/{signatureId}")
    public ResponseEntity<byte[]> getSignatureImage(@PathVariable Long signatureId) {
        try {
            ProcessFileSignatureVO signature = signatureService.getSignatureById(signatureId);
            if (signature == null) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] imageContent = java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get(signature.getSignatureImagePath())
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageContent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
