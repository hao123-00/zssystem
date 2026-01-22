-- ============================================
-- 生产管理模块数据库脚本
-- 创建时间：2026-01-20
-- 说明：包含新建表和新增字段的SQL语句
-- ============================================

-- ============================================
-- 一、新建表
-- ============================================

-- 1. 设备生产产品配置表（equipment_production_product）
-- 用于维护设备与产品的生产关系，包含订单数量和产能数量
CREATE TABLE IF NOT EXISTS `equipment_production_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) NOT NULL COMMENT '设备编号（冗余，便于查询）',
  `product_code` varchar(50) DEFAULT NULL COMMENT '产品编码',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `order_quantity` int NOT NULL COMMENT '订单数量',
  `daily_capacity` int NOT NULL COMMENT '日产能（每天能生产的数量）',
  `sort_order` int DEFAULT 0 COMMENT '排序（用于确定生产优先级）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_product_code` (`product_code`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备生产产品配置表';

-- 2. 27天生产计划排程表（production_schedule）
-- 用于存储自动生成的27天生产计划排程结果
CREATE TABLE IF NOT EXISTS `production_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) NOT NULL COMMENT '设备编号',
  `schedule_date` date NOT NULL COMMENT '排程日期（27天中的某一天）',
  `day_number` int NOT NULL COMMENT '第几天（1-27）',
  `product_code` varchar(50) DEFAULT NULL COMMENT '产品编码',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `daily_capacity` int NOT NULL COMMENT '当天产能',
  `remaining_quantity` int NOT NULL COMMENT '剩余数量（订单数量 - 产能 × 已生产天数）',
  `equipment_product_id` bigint NOT NULL COMMENT '关联的设备生产产品配置ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_schedule_date` (`schedule_date`),
  KEY `idx_day_number` (`day_number`),
  UNIQUE KEY `uk_equipment_day` (`equipment_id`, `day_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='27天生产计划排程表';

-- ============================================
-- 二、新增表字段（production_record表）
-- ============================================

-- 检查并添加 equipment_no 字段
SET @dbname = DATABASE();
SET @tablename = 'production_record';
SET @columnname = 'equipment_no';

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `equipment_no` varchar(50) DEFAULT NULL COMMENT ''设备编号'' AFTER `equipment_id`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 schedule_id 字段
SET @columnname = 'schedule_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `schedule_id` bigint DEFAULT NULL COMMENT ''排程ID（关联production_schedule）'' AFTER `equipment_no`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 product_code 字段
SET @columnname = 'product_code';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `product_code` varchar(50) DEFAULT NULL COMMENT ''产品编码'' AFTER `schedule_id`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 product_name 字段
SET @columnname = 'product_name';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `product_name` varchar(100) DEFAULT NULL COMMENT ''产品名称'' AFTER `product_code`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 为 production_record 表添加 schedule_id 索引（如果不存在）
-- 注意：MySQL 5.7 不支持 CREATE INDEX IF NOT EXISTS，使用 ALTER TABLE 方式
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = 'production_record' 
   AND INDEX_NAME = 'idx_schedule_id') = 0,
  'ALTER TABLE `production_record` ADD INDEX `idx_schedule_id` (`schedule_id`);',
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================
-- 三、设备表（equipment）扩展字段检查
-- 如果设备表已存在但缺少扩展字段，则添加
-- ============================================

SET @tablename = 'equipment';

-- 检查并添加 group_name 字段
SET @columnname = 'group_name';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `group_name` varchar(50) DEFAULT NULL COMMENT ''组别'' AFTER `equipment_name`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 machine_no 字段
SET @columnname = 'machine_no';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `machine_no` varchar(50) DEFAULT NULL COMMENT ''机台号'' AFTER `group_name`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 equipment_model 字段
SET @columnname = 'equipment_model';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `equipment_model` varchar(100) DEFAULT NULL COMMENT ''设备型号'' AFTER `machine_no`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 robot_model 字段
SET @columnname = 'robot_model';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `robot_model` varchar(100) DEFAULT NULL COMMENT ''机械手型号'' AFTER `equipment_model`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 enable_date 字段
SET @columnname = 'enable_date';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `enable_date` date DEFAULT NULL COMMENT ''启用日期'' AFTER `robot_model`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 service_life 字段
SET @columnname = 'service_life';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `service_life` int DEFAULT NULL COMMENT ''使用年限（年）'' AFTER `enable_date`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 mold_temp_machine 字段
SET @columnname = 'mold_temp_machine';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `mold_temp_machine` varchar(100) DEFAULT NULL COMMENT ''模温机'' AFTER `service_life`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 chiller 字段
SET @columnname = 'chiller';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `chiller` varchar(100) DEFAULT NULL COMMENT ''冻水机'' AFTER `mold_temp_machine`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 basic_mold 字段
SET @columnname = 'basic_mold';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `basic_mold` varchar(100) DEFAULT NULL COMMENT ''基本排模'' AFTER `chiller`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 spare_mold1 字段
SET @columnname = 'spare_mold1';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `spare_mold1` varchar(100) DEFAULT NULL COMMENT ''备用排模1'' AFTER `basic_mold`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 spare_mold2 字段
SET @columnname = 'spare_mold2';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `spare_mold2` varchar(100) DEFAULT NULL COMMENT ''备用排模2'' AFTER `spare_mold1`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 spare_mold3 字段
SET @columnname = 'spare_mold3';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `spare_mold3` varchar(100) DEFAULT NULL COMMENT ''备用排模3'' AFTER `spare_mold2`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 为 equipment 表添加索引（如果不存在）
-- 注意：MySQL 5.7 不支持 CREATE INDEX IF NOT EXISTS，使用 ALTER TABLE 方式
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = 'equipment' 
   AND INDEX_NAME = 'idx_group_name') = 0,
  'ALTER TABLE `equipment` ADD INDEX `idx_group_name` (`group_name`);',
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = 'equipment' 
   AND INDEX_NAME = 'idx_machine_no') = 0,
  'ALTER TABLE `equipment` ADD INDEX `idx_machine_no` (`machine_no`);',
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================
-- 脚本执行完成
-- ============================================
