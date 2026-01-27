# 工艺文件Excel生成功能实现总结

## 完成时间
**日期**: 2026-01-25  
**状态**: ✅ 已完成

## 功能概述

在编制人提交工艺文件信息后，自动生成"塑料零件注塑卡片"格式的Excel表格。后续审批各节点可以查看和下载该Excel表格，并且各审批节点的电子签名会自动同步到Excel的相应位置。

## 实现内容

### 1. Excel生成工具类

**文件**: `backend/src/main/java/com/zssystem/util/ProcessFileExcelGenerator.java`

**核心功能**:
- `generateProcessFileExcel()`: 根据表单数据生成Excel文件
- 完全按照图片格式布局
- 包含所有工艺参数字段
- 预留签名区域

**Excel结构**:
1. **表头**: 公司名称、标题、基本信息（产品型号、产品名称等）
2. **材料信息**: 材料名称、牌号、颜色、颜料、重量等
3. **模具信息**: 模具编号、型腔数量、锁模力、设备信息、产品关键尺寸
4. **注塑成型工艺参数**:
   - 合模参数（合模1、合模2、模保、高压）
   - 进芯参数（进芯一、进芯二）
   - 射胶参数（6段）
   - 保压参数（3段）
   - 开模参数（开模1-4）
   - 抽芯参数（抽芯一、抽芯二）
   - 熔胶参数（熔胶1、熔前松退、熔后松退）
   - 顶出参数（顶出一速、顶出二速、顶退一速、顶退二速）
5. **温度参数**: 料筒温度（4段）、模具温度
6. **时间参数**: 合模、模保、进芯、注射、保压、冷却、抽芯、开模、取件时间、总时间
7. **特殊模式参数**: 注射模式、进芯方式、抽芯方式、座台方式、顶针模式、顶针次数、螺杆转速、抽芯行程方式
8. **原材料干燥处理**: 使用设备、盛料高度、翻料时间、干燥温度、前模冷却
9. **零件后处理**: 零件后处理、产品后处理、加热温度、保温温度、干燥时间、后模冷却
10. **工序内容**: 4条标准工序内容（可自定义）
11. **品质检查**: 4条品质检查标准（可自定义）
12. **综合评估**: 模具及注塑工艺综合评估
13. **审批签名区域**: 编制人、审核人、批准人、会签（预留空白区域，后续插入签名）

### 2. 提交审批时生成Excel

**文件**: `backend/src/main/java/com/zssystem/service/impl/ProcessFileServiceImpl.java`

**实现逻辑**:
1. 检查是否为表单方式（`fileType == "form"`）
2. 查询详细内容数据
3. 查询设备信息
4. 调用`ProcessFileExcelGenerator.generateProcessFileExcel()`生成Excel
5. 更新文件路径和大小
6. 保存到数据库

**代码位置**:
```java
// 在 submitForApproval() 方法中
if ("form".equals(processFile.getFileType())) {
    // 查询详细内容
    ProcessFileDetail detail = processFileDetailMapper.selectOne(...);
    
    if (detail != null) {
        // 查询设备信息
        Equipment equipment = equipmentMapper.selectById(...);
        
        // 生成Excel文件
        String excelPath = ProcessFileExcelGenerator.generateProcessFileExcel(
            processFile, detail, equipment, uploadPath
        );
        
        // 更新文件路径和大小
        processFile.setFilePath(excelPath);
        processFile.setFileSize(excelFile.length());
        processFile.setFileType("xlsx");
    }
}
```

### 3. 审批时插入签名

**文件**: `backend/src/main/java/com/zssystem/service/impl/ProcessFileServiceImpl.java`

**实现逻辑**:
1. 在审批通过后，如果有签名ID
2. 查询签名信息
3. 根据审批级别确定签名类型
4. 调用`ProcessFileExcelGenerator.insertSignatureToGeneratedExcel()`插入签名

**签名类型映射**:
- 审批级别1（车间主任审核）→ `APPROVE_LEVEL1` → "审核人"
- 审批级别2（注塑部经理会签）→ `APPROVE_LEVEL2` → "会签"
- 审批级别3（生产技术部经理批准）→ `APPROVE_LEVEL3` → "批准人"

**代码位置**:
```java
// 在 approveProcessFile() 方法中
if (signatureId != null && processFile.getFilePath() != null && !processFile.getFilePath().isEmpty()) {
    // 查询签名信息
    ProcessFileSignatureVO signature = signatureService.getSignatureById(signatureId);
    if (signature != null) {
        // 确定签名类型
        String signatureType = getSignatureTypeByApprovalLevel(currentLevel);
        
        // 插入签名到Excel
        ProcessFileExcelGenerator.insertSignatureToGeneratedExcel(
            processFile.getFilePath(),
            signature.getSignatureImagePath(),
            signatureType
        );
    }
}
```

### 4. 提交时插入编制人签名

**文件**: `backend/src/main/java/com/zssystem/controller/ProcessFileController.java`

**实现逻辑**:
1. 保存编制人签名
2. 提交审批（生成Excel）
3. 重新查询文件信息（获取Excel路径）
4. 插入编制人签名到Excel

**代码位置**:
```java
// 在 submit() 方法中
// 1. 保存签名
Long signatureId = signatureService.saveSignature(...);

// 2. 提交审批（生成Excel）
processFileService.submitForApproval(id, currentUserId);

// 3. 插入编制人签名
ProcessFileVO fileVO = processFileService.getProcessFileById(id);
if (fileVO != null && fileVO.getFilePath() != null && !fileVO.getFilePath().isEmpty()) {
    ProcessFileSignatureVO signature = signatureService.getSignatureById(signatureId);
    if (signature != null) {
        ProcessFileExcelGenerator.insertSignatureToGeneratedExcel(
            fileVO.getFilePath(),
            signature.getSignatureImagePath(),
            "SUBMIT"
        );
    }
}
```

### 5. 查看/下载Excel

**已有接口**: `GET /api/production/process-file/{id}/download`

该接口已经存在，可以下载生成的Excel文件。前端可以通过该接口查看和下载Excel。

## 文件清单

### 新增文件
- `backend/src/main/java/com/zssystem/util/ProcessFileExcelGenerator.java` - Excel生成工具类

### 修改文件
- `backend/src/main/java/com/zssystem/service/impl/ProcessFileServiceImpl.java` - 添加Excel生成逻辑和签名插入逻辑
- `backend/src/main/java/com/zssystem/controller/ProcessFileController.java` - 添加提交时插入编制人签名逻辑

## 使用流程

### 1. 编制人提交工艺文件

1. **填写表单**: 编制人在表单页面填写所有工艺参数
2. **保存表单**: 点击"保存"按钮，保存表单数据
3. **提交审批**: 点击"提交"按钮
4. **电子签名**: 在签名弹窗中进行签名
5. **自动生成Excel**: 系统自动生成Excel文件
6. **插入编制人签名**: 系统自动将编制人签名插入到Excel的"编制人"位置

### 2. 审批流程

1. **车间主任审核**:
   - 查看Excel文件（通过下载接口）
   - 填写审批意见并签名
   - 系统自动将签名插入到Excel的"审核人"位置

2. **注塑部经理会签**:
   - 查看Excel文件（包含编制人和审核人签名）
   - 填写审批意见并签名
   - 系统自动将签名插入到Excel的"会签"位置

3. **生产技术部经理批准**:
   - 查看Excel文件（包含编制人、审核人、会签签名）
   - 填写审批意见并签名
   - 系统自动将签名插入到Excel的"批准人"位置
   - 自动生成电子受控章

### 3. 查看Excel

所有审批节点都可以通过以下方式查看Excel：
- **下载接口**: `GET /api/production/process-file/{id}/download`
- **前端**: 在详情页点击"下载"按钮

## 技术要点

### 1. Excel格式

- **格式**: XLSX格式（.xlsx）
- **布局**: 完全按照图片格式，包括合并单元格、边框、样式等
- **签名区域**: 预留空白区域，后续插入签名图片

### 2. 文件存储

- **存储路径**: `/Users/czd/zssystem/uploads/process-files/年份/月份/文件编号.xlsx`
- **文件命名**: 使用文件编号（如：`PF20260125001.xlsx`）

### 3. 签名插入

- **插入时机**: 
  - 提交审批时：插入编制人签名
  - 审批通过时：插入对应审批人的签名
- **插入位置**: 通过搜索"编制人"、"审核人"、"批准人"、"会签"标签，在标签下方插入签名图片
- **图片格式**: PNG格式
- **图片大小**: 自动调整以适应单元格

### 4. 数据映射

- 所有表单字段自动映射到Excel对应位置
- 数字字段格式化显示（去除尾随零）
- 空值处理：显示为空字符串

## 注意事项

1. **Excel生成时机**: 仅在提交审批时生成，保存草稿时不生成
2. **文件路径**: Excel文件路径保存在`process_file.file_path`字段中
3. **文件类型**: 提交后`file_type`从"form"改为"xlsx"
4. **签名顺序**: 按照审批流程顺序插入签名
5. **文件更新**: 每次插入签名都会更新Excel文件

## 验证清单

- [x] Excel生成工具类创建完成
- [x] 提交审批时生成Excel功能完成
- [x] 审批时插入签名功能完成
- [x] 提交时插入编制人签名功能完成
- [x] 下载接口可用
- [ ] 功能测试（需要手动完成）
- [ ] Excel格式验证（需要手动完成）
- [ ] 签名插入验证（需要手动完成）

## 相关文档

- `工艺文件表单功能实现总结.md` - 表单功能详细文档
- `工艺文件审批流程-电子签名功能实现总结.md` - 电子签名功能文档
- `工艺文件Excel签名同步功能实现总结.md` - Excel签名同步功能文档
- `12-注塑工艺文件管理功能开发文档.md` - 工艺文件管理完整功能设计

---

**完成状态**: ✅ 所有代码已完成并编译通过  
**下一步**: 重启后端服务，进行功能测试
