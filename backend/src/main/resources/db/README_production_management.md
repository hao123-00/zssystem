# 生产管理模块数据库脚本说明

## 文件说明

本目录包含三个版本的SQL脚本文件，用于创建生产管理模块所需的数据库表和字段：

1. **production_management.sql** - 完整版（推荐）
   - 包含条件判断逻辑，自动检查字段是否存在
   - 适用于所有MySQL版本
   - 安全可靠，不会因为字段已存在而报错

2. **production_management_simple.sql** - 简化版
   - 使用 `IF NOT EXISTS` 语法（MySQL 8.0.19+）
   - 代码简洁，易于阅读
   - 需要 MySQL 8.0.19 及以上版本

3. **production_management_compatible.sql** - 兼容版
   - 直接执行 ALTER TABLE 语句
   - 适用于 MySQL 5.7+
   - 如果字段已存在会报错，但可以忽略

## 执行方式

### 方式一：使用 MySQL 客户端命令行

```bash
# 登录MySQL
mysql -u root -p

# 选择数据库
USE zssystem;

# 执行SQL脚本（推荐使用完整版）
source backend/src/main/resources/db/production_management.sql;
```

### 方式二：使用 MySQL Workbench 或其他图形化工具

1. 打开 MySQL Workbench
2. 连接到数据库
3. 打开对应的SQL文件
4. 执行脚本

### 方式三：使用命令行直接执行

```bash
mysql -u root -p zssystem < backend/src/main/resources/db/production_management.sql
```

## 脚本内容说明

### 一、新建表

#### 1. equipment_production_product（设备生产产品配置表）
- **用途**：维护设备与产品的生产关系，包含订单数量和产能数量
- **关键字段**：
  - `equipment_id`：设备ID
  - `product_name`：产品名称
  - `order_quantity`：订单数量
  - `daily_capacity`：日产能
  - `sort_order`：排序（生产优先级）

#### 2. production_schedule（27天生产计划排程表）
- **用途**：存储自动生成的27天生产计划排程结果
- **关键字段**：
  - `equipment_id`：设备ID
  - `day_number`：第几天（1-27）
  - `product_name`：产品名称
  - `daily_capacity`：当天产能
  - `remaining_quantity`：剩余数量
- **唯一约束**：`equipment_id` + `day_number`（每个设备每天只能有一条记录）

### 二、新增字段

#### production_record 表新增字段：
- `equipment_no`：设备编号
- `schedule_id`：排程ID（关联production_schedule）
- `product_code`：产品编码
- `product_name`：产品名称

#### equipment 表新增字段：
- `group_name`：组别
- `machine_no`：机台号
- `equipment_model`：设备型号
- `robot_model`：机械手型号
- `enable_date`：启用日期
- `service_life`：使用年限（年）
- `mold_temp_machine`：模温机
- `chiller`：冻水机
- `basic_mold`：基本排模
- `spare_mold1`：备用排模1
- `spare_mold2`：备用排模2
- `spare_mold3`：备用排模3

## 执行顺序

1. 先执行新建表的SQL（equipment_production_product、production_schedule）
2. 再执行新增字段的SQL（production_record、equipment）

## 注意事项

1. **备份数据**：执行前请先备份数据库
2. **检查表结构**：如果表已存在，建议先检查现有表结构
3. **字段冲突**：如果字段已存在，使用完整版脚本会自动跳过，使用兼容版会报错但可以忽略
4. **索引创建**：脚本会自动创建必要的索引，如果索引已存在可能会报错，可以忽略

## 验证执行结果

执行完成后，可以使用以下SQL验证：

```sql
-- 检查新表是否创建成功
SHOW TABLES LIKE 'equipment_production_product';
SHOW TABLES LIKE 'production_schedule';

-- 检查 production_record 表的新字段
DESC production_record;

-- 检查 equipment 表的新字段
DESC equipment;

-- 检查索引
SHOW INDEX FROM production_record;
SHOW INDEX FROM equipment;
```

## 回滚方案

如果需要回滚，可以执行以下SQL：

```sql
-- 删除新表（注意：会删除所有数据）
DROP TABLE IF EXISTS `production_schedule`;
DROP TABLE IF EXISTS `equipment_production_product`;

-- 删除新增字段（注意：会删除字段中的所有数据）
ALTER TABLE `production_record` 
DROP COLUMN IF EXISTS `equipment_no`,
DROP COLUMN IF EXISTS `schedule_id`,
DROP COLUMN IF EXISTS `product_code`,
DROP COLUMN IF EXISTS `product_name`;

-- 删除 equipment 表的扩展字段（注意：会删除字段中的所有数据）
ALTER TABLE `equipment` 
DROP COLUMN IF EXISTS `group_name`,
DROP COLUMN IF EXISTS `machine_no`,
DROP COLUMN IF EXISTS `equipment_model`,
DROP COLUMN IF EXISTS `robot_model`,
DROP COLUMN IF EXISTS `enable_date`,
DROP COLUMN IF EXISTS `service_life`,
DROP COLUMN IF EXISTS `mold_temp_machine`,
DROP COLUMN IF EXISTS `chiller`,
DROP COLUMN IF EXISTS `basic_mold`,
DROP COLUMN IF EXISTS `spare_mold1`,
DROP COLUMN IF EXISTS `spare_mold2`,
DROP COLUMN IF EXISTS `spare_mold3`;
```

## 常见问题

### Q1: 执行时提示字段已存在错误
**A**: 如果使用兼容版脚本，字段已存在时会报错，这是正常的，可以忽略。建议使用完整版脚本，会自动检查字段是否存在。

### Q2: 执行时提示索引已存在错误
**A**: 索引已存在时会报错，可以忽略。或者先删除索引再执行：
```sql
DROP INDEX idx_schedule_id ON production_record;
DROP INDEX idx_group_name ON equipment;
DROP INDEX idx_machine_no ON equipment;
```

### Q3: 如何确认脚本执行成功？
**A**: 使用验证SQL检查表结构和字段是否创建成功。
