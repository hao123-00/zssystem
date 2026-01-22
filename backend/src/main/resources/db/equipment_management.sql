-- ============================================
-- 设备管理模块数据库脚本
-- 创建时间：2026-01-20
-- 说明：包含新建表和新增字段的SQL语句
-- ============================================

-- ============================================
-- 一、新建表
-- ============================================

-- 1. 设备可生产产品表（equipment_product）
-- 用于维护"设备-可生产产品"的多对多关系
CREATE TABLE IF NOT EXISTS `equipment_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) NOT NULL COMMENT '设备编号（冗余，便于查询）',
  `product_code` varchar(50) NOT NULL COMMENT '产品编码',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_equipment_product` (`equipment_id`, `product_code`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备可生产产品表';

-- 2. 设备点检表（equipment_check）
-- 注塑成型设备点检表，记录30天的点检数据，包含16个检查项
CREATE TABLE IF NOT EXISTS `equipment_check` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) NOT NULL COMMENT '设备编号',
  `equipment_name` varchar(100) NOT NULL COMMENT '设备名称',
  `check_month` varchar(7) NOT NULL COMMENT '检查月份，格式：YYYY-MM',
  `check_date` date NOT NULL COMMENT '检查日期',
  `checker_name` varchar(50) NOT NULL COMMENT '检点人姓名',
  -- 电路部分（3项）
  `circuit_item1` tinyint DEFAULT NULL COMMENT '发热圈/感温线/交流接触器温度控制器：1-正常，0-异常',
  `circuit_item2` tinyint DEFAULT NULL COMMENT '电箱排风扇/安全门开关/烘料斗温度：1-正常，0-异常',
  `circuit_item3` tinyint DEFAULT NULL COMMENT '形成开关：1-正常，0-异常',
  -- 机架部分（3项）
  `frame_item1` tinyint DEFAULT NULL COMMENT '哥林柱、机架螺母：1-正常，0-异常',
  `frame_item2` tinyint DEFAULT NULL COMMENT '安全挡板/射咀/低压保护：1-正常，0-异常',
  `frame_item3` tinyint DEFAULT NULL COMMENT '调模牙盘变形及余音：1-正常，0-异常',
  -- 油路部分（5项）
  `oil_item1` tinyint DEFAULT NULL COMMENT '油泵压力/动作：1-正常，0-异常',
  `oil_item2` tinyint DEFAULT NULL COMMENT '油泵/溶胶/马达杂音：1-正常，0-异常',
  `oil_item3` tinyint DEFAULT NULL COMMENT '油温/冷却器：1-正常，0-异常',
  `oil_item4` tinyint DEFAULT NULL COMMENT '自动加油润滑油管：1-正常，0-异常',
  `oil_item5` tinyint DEFAULT NULL COMMENT '机台油管漏油：1-正常，0-异常',
  -- 周边设备（5项）
  `peripheral_item1` tinyint DEFAULT NULL COMMENT '模温机、冻水机异响：1-正常，0-异常',
  `peripheral_item2` tinyint DEFAULT NULL COMMENT '模温机冷却水、过滤网：1-正常，0-异常',
  `peripheral_item3` tinyint DEFAULT NULL COMMENT '油温机缺油、温度：1-正常，0-异常',
  `peripheral_item4` tinyint DEFAULT NULL COMMENT '冻水机过滤网、运水：1-正常，0-异常',
  `peripheral_item5` tinyint DEFAULT NULL COMMENT '冻水机制冷系统、交流触感器：1-正常，0-异常',
  -- 备注
  `remark` varchar(1000) DEFAULT NULL COMMENT '异常项备注说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_check_month` (`check_month`),
  KEY `idx_check_date` (`check_date`),
  KEY `idx_checker_name` (`checker_name`),
  UNIQUE KEY `uk_equipment_date` (`equipment_id`, `check_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备点检表';

-- 3. 设备维护记录表（equipment_maintenance）
CREATE TABLE IF NOT EXISTS `equipment_maintenance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `maintenance_date` date NOT NULL COMMENT '维护日期',
  `maintenance_type` varchar(50) DEFAULT NULL COMMENT '维护类型',
  `maintenance_content` varchar(500) DEFAULT NULL COMMENT '维护内容',
  `maintainer_name` varchar(50) DEFAULT NULL COMMENT '维护人员',
  `cost` decimal(10,2) DEFAULT NULL COMMENT '维护费用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_maintenance_date` (`maintenance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维护记录表';

-- 4. 设备故障记录表（equipment_fault）
CREATE TABLE IF NOT EXISTS `equipment_fault` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `fault_date` datetime NOT NULL COMMENT '故障日期',
  `fault_description` varchar(500) NOT NULL COMMENT '故障描述',
  `handle_method` varchar(500) DEFAULT NULL COMMENT '处理方式',
  `handler_name` varchar(50) DEFAULT NULL COMMENT '处理人员',
  `handle_date` datetime DEFAULT NULL COMMENT '处理日期',
  `status` tinyint DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-已处理',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_fault_date` (`fault_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备故障记录表';

-- ============================================
-- 二、为 equipment 表添加 manufacturer 和 purchase_date 字段（如果不存在）
-- ============================================

SET @dbname = DATABASE();
SET @tablename = 'equipment';

-- 检查并添加 manufacturer 字段
SET @columnname = 'manufacturer';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `manufacturer` varchar(100) DEFAULT NULL COMMENT ''制造商'' AFTER `equipment_model`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 purchase_date 字段
SET @columnname = 'purchase_date';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `purchase_date` date DEFAULT NULL COMMENT ''购买日期'' AFTER `manufacturer`;'),
  'SELECT 1;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================
-- 脚本执行完成
-- ============================================
