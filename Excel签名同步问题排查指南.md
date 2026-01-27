# Excel签名同步问题排查指南

## 问题描述
Excel表格中没有同步对应审批人的电子签名。

## 已完成的优化

### 1. 增强日志输出
- 添加详细的执行日志，包括文件路径、文件大小、查找过程等
- 在关键步骤输出成功/失败信息
- 添加错误提示和排查建议

### 2. 改进标签查找逻辑
- 支持精确匹配和包含匹配
- 自动去除空格进行比较
- 添加所有标签的搜索功能（用于调试）

### 3. 改进错误处理
- 验证Excel文件和签名图片是否存在
- 验证文件大小
- 提供详细的错误信息

## 排查步骤

### 步骤1: 检查后端日志

重启后端服务后，进行签名操作，查看控制台输出：

```bash
# 查看后端日志
tail -f /path/to/backend/logs/application.log
# 或者直接查看控制台输出
```

**正常日志示例**:
```
========== 开始插入签名到Excel ==========
Excel文件路径: /Users/czd/zssystem/uploads/process-files/2026/01/PF20260125001.xlsx
签名图片路径: /Users/czd/zssystem/uploads/signatures/2026/01/SIG_1_SUBMIT_20260125120000.png
签名类型: SUBMIT
Excel文件存在，大小: 12345 字节
签名图片存在，大小: 5678 字节
Sheet名称: Sheet1
Sheet总行数: 50
查找标签: 编制人
精确匹配找到标签 '编制人' 在: 行10, 列2
找到标签位置: 行11, 列2
签名图片大小: 5678 字节
图片已插入到位置: 行11, 列2
Excel文件已保存
========== 签名图片已成功插入到Excel ==========
```

**错误日志示例**:
```
========== 开始插入签名到Excel ==========
Excel文件路径: /Users/czd/zssystem/uploads/process-files/2026/01/PF20260125001.xlsx
错误: Excel文件不存在: /Users/czd/zssystem/uploads/process-files/2026/01/PF20260125001.xlsx
```

### 步骤2: 检查Excel文件

#### 2.1 验证文件路径
- 确认Excel文件确实存在于指定路径
- 检查文件权限（是否有读写权限）

#### 2.2 验证Excel格式
- 确认文件是XLSX格式（.xlsx），不是XLS格式（.xls）
- 尝试用Excel打开文件，确认文件没有损坏

#### 2.3 验证标签文本
打开Excel文件，检查是否包含以下标签之一：
- **编制人** - 注塑组长签名位置
- **审核人** - 车间主任签名位置
- **会签** - 注塑部经理签名位置
- **批准人** - 生产技术部经理签名位置

**重要**: 标签文本必须完全匹配（区分大小写），例如：
- ✅ "编制人" - 正确
- ❌ "编制" - 错误
- ❌ "编制人：" - 错误（多了冒号）
- ❌ "编制 人" - 错误（多了空格）

### 步骤3: 检查签名图片

#### 3.1 验证图片路径
- 确认签名图片文件存在于指定路径
- 检查文件权限

#### 3.2 验证图片格式
- 确认图片是PNG格式
- 尝试用图片查看器打开，确认图片没有损坏

### 步骤4: 常见问题及解决方案

#### 问题1: "未找到标签"

**原因**: Excel文件中没有相应的标签文本

**解决方案**:
1. 打开Excel文件，查找是否有"编制人"、"审核人"、"会签"、"批准人"等文本
2. 如果标签文本不同，需要修改Excel文件中的文本，使其匹配
3. 或者修改代码中的标签常量（在`ProcessFileExcelUtil.java`中）

**检查方法**:
查看日志中的"搜索所有标签"部分：
```
搜索所有标签:
  ✓ 找到 '编制人' 在位置: 行10, 列2
  ✗ 未找到 '审核人'
  ✗ 未找到 '会签'
  ✓ 找到 '批准人' 在位置: 行15, 列2
```

#### 问题2: "Excel文件不存在"

**原因**: 文件路径不正确或文件已被删除

**解决方案**:
1. 检查`process_file`表中的`file_path`字段，确认路径是否正确
2. 确认文件确实存在于该路径
3. 检查文件权限

#### 问题3: "签名图片不存在"

**原因**: 签名图片保存失败或路径不正确

**解决方案**:
1. 检查签名图片是否成功保存
2. 查看`process_file_signature`表中的`signature_image_path`字段
3. 确认文件确实存在于该路径

#### 问题4: "仅支持XLSX格式的Excel文件"

**原因**: Excel文件是XLS格式（旧格式）

**解决方案**:
1. 用Excel打开文件
2. 另存为XLSX格式
3. 重新上传文件

#### 问题5: 文件权限问题

**原因**: 系统没有读写权限

**解决方案**:
```bash
# 检查文件权限
ls -l /Users/czd/zssystem/uploads/process-files/2026/01/

# 修改文件权限（如果需要）
chmod 644 /Users/czd/zssystem/uploads/process-files/2026/01/*.xlsx
```

#### 问题6: Excel文件被锁定

**原因**: 文件被其他程序打开（如Excel）

**解决方案**:
1. 关闭所有打开该文件的程序
2. 重新尝试签名操作

## 调试方法

### 方法1: 查看详细日志

重启后端服务，进行签名操作，查看控制台输出的详细日志。

### 方法2: 手动测试Excel更新

创建一个测试方法，手动调用`ProcessFileExcelUtil.insertSignatureToExcel()`：

```java
// 在Controller或Service中添加测试方法
public void testInsertSignature() {
    String excelPath = "/Users/czd/zssystem/uploads/process-files/2026/01/PF20260125001.xlsx";
    String signaturePath = "/Users/czd/zssystem/uploads/signatures/2026/01/SIG_1_SUBMIT_20260125120000.png";
    String signatureType = "SUBMIT";
    
    boolean success = ProcessFileExcelUtil.insertSignatureToExcel(
        excelPath, 
        signaturePath, 
        signatureType
    );
    
    System.out.println("测试结果: " + (success ? "成功" : "失败"));
}
```

### 方法3: 检查数据库

```sql
-- 查看工艺文件路径
SELECT id, file_no, file_path, file_name 
FROM process_file 
WHERE deleted = 0 
ORDER BY create_time DESC 
LIMIT 10;

-- 查看签名记录
SELECT id, file_id, file_no, signature_type, signature_image_path, signature_time
FROM process_file_signature
WHERE deleted = 0
ORDER BY signature_time DESC
LIMIT 10;
```

## 验证签名是否成功插入

### 方法1: 直接打开Excel文件
1. 下载或打开工艺文件Excel
2. 查找"编制人"、"审核人"、"会签"、"批准人"标签
3. 检查标签下方是否有签名图片

### 方法2: 检查文件修改时间
```bash
# 查看Excel文件的修改时间
ls -l /Users/czd/zssystem/uploads/process-files/2026/01/*.xlsx

# 如果文件在签名后修改时间更新了，说明文件已被更新
```

## 下一步操作

1. **重启后端服务**（如果还没有重启）
   ```bash
   cd /Users/czd/zssystem/backend
   ./start.sh
   ```

2. **进行签名操作**，查看控制台日志

3. **根据日志信息**，按照上述排查步骤解决问题

4. **如果问题仍然存在**，请提供：
   - 后端控制台的完整日志
   - Excel文件中的标签文本（截图或文本）
   - 签名图片的路径和文件信息

## 相关文档

- `工艺文件Excel签名同步功能实现总结.md` - 功能实现文档
- `工艺文件审批流程-电子签名功能实现总结.md` - 电子签名功能文档

---

**提示**: 如果问题仍然存在，请查看后端控制台的详细日志，日志会显示具体的错误原因和位置。
