-- ============================================
-- 生产订单表和生产计划排程表更新脚本
-- 创建时间：2026-01-20
-- 说明：更新生产订单表和排程表结构，支持按机台号生成排程，避开星期天
-- ============================================

-- ============================================
-- 一、更新 production_order 表
-- ============================================

-- 如果表不存在，创建表
CREATE TABLE IF NOT EXISTS `production_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号（唯一）',
  `machine_no` varchar(50) NOT NULL COMMENT '机台号',
  `equipment_id` bigint DEFAULT NULL COMMENT '设备ID（关联equipment表）',
  `product_code` varchar(50) DEFAULT NULL COMMENT '产品编码',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `order_quantity` int NOT NULL COMMENT '订单数量',
  `daily_capacity` int NOT NULL COMMENT '日产能（每天能生产的数量）',
  `sort_order` int DEFAULT 0 COMMENT '排序（用于确定生产优先级，按录入顺序）',
  `status` tinyint DEFAULT 0 COMMENT '状态：0-待排程，1-排程中，2-已完成',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_machine_no` (`machine_no`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_product_code` (`product_code`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产订单表';

-- 如果表已存在，更新字段
SET @dbname = DATABASE();
SET @tablename = 'production_order';

-- 检查并添加 machine_no 字段
SET @columnname = 'machine_no';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `machine_no` varchar(50) NOT NULL COMMENT ''机台号'' AFTER `order_no`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 equipment_id 字段
SET @columnname = 'equipment_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `equipment_id` bigint DEFAULT NULL COMMENT ''设备ID（关联equipment表）'' AFTER `machine_no`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 order_quantity 字段（如果不存在，重命名 quantity）
SET @columnname = 'order_quantity';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @dbname 
     AND TABLE_NAME = @tablename 
     AND COLUMN_NAME = 'quantity') > 0,
    'ALTER TABLE production_order CHANGE COLUMN `quantity` `order_quantity` int NOT NULL COMMENT ''订单数量'';',
    'ALTER TABLE production_order ADD COLUMN `order_quantity` int NOT NULL COMMENT ''订单数量'';'
  )),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 daily_capacity 字段
SET @columnname = 'daily_capacity';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `daily_capacity` int NOT NULL COMMENT ''日产能（每天能生产的数量）'' AFTER `order_quantity`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 sort_order 字段
SET @columnname = 'sort_order';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `sort_order` int DEFAULT 0 COMMENT ''排序（用于确定生产优先级，按录入顺序）'' AFTER `daily_capacity`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 更新 status 字段注释
ALTER TABLE `production_order` MODIFY COLUMN `status` tinyint DEFAULT 0 COMMENT '状态：0-待排程，1-排程中，2-已完成';

-- 添加索引
SET @indexname = 'idx_machine_no';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND INDEX_NAME = @indexname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX `idx_machine_no` (`machine_no`);'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================
-- 二、更新 production_schedule 表
-- ============================================

SET @tablename = 'production_schedule';

-- 检查并添加 machine_no 字段
SET @columnname = 'machine_no';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `machine_no` varchar(50) NOT NULL COMMENT ''机台号'' AFTER `id`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 production_quantity 字段
SET @columnname = 'production_quantity';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `production_quantity` int NOT NULL COMMENT ''排产数量（等于产能）'' AFTER `product_name`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 order_id 字段（如果不存在，重命名 equipment_product_id）
SET @columnname = 'order_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @dbname 
     AND TABLE_NAME = @tablename 
     AND COLUMN_NAME = 'equipment_product_id') > 0,
    'ALTER TABLE production_schedule CHANGE COLUMN `equipment_product_id` `order_id` bigint NOT NULL COMMENT ''关联的生产订单ID'';',
    'ALTER TABLE production_schedule ADD COLUMN `order_id` bigint NOT NULL COMMENT ''关联的生产订单ID'';'
  )),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 is_sunday 字段
SET @columnname = 'is_sunday';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `is_sunday` tinyint DEFAULT 0 COMMENT ''是否为星期天：0-否，1-是（用于标识跳过的日期）'' AFTER `order_id`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加索引
SET @indexname = 'idx_machine_no';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND INDEX_NAME = @indexname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX `idx_machine_no` (`machine_no`);'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @indexname = 'idx_order_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND INDEX_NAME = @indexname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX `idx_order_id` (`order_id`);'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 删除旧的唯一索引，添加新的唯一索引
SET @indexname = 'uk_machine_date';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND INDEX_NAME = @indexname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD UNIQUE KEY `uk_machine_date` (`machine_no`, `schedule_date`);'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================
-- 脚本执行完成
-- ============================================
