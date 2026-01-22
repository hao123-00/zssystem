-- ============================================
-- 生产管理模块数据库脚本（简化版）
-- 创建时间：2026-01-20
-- 说明：直接执行的SQL语句，不包含条件判断
-- ============================================

-- ============================================
-- 一、新建表
-- ============================================

-- 1. 设备生产产品配置表
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

-- 2. 27天生产计划排程表
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
-- 二、为 production_record 表添加新字段
-- 注意：如果字段已存在，执行会报错，请先检查表结构
-- ============================================

-- 添加 equipment_no 字段
ALTER TABLE `production_record` 
ADD COLUMN IF NOT EXISTS `equipment_no` varchar(50) DEFAULT NULL COMMENT '设备编号' AFTER `equipment_id`;

-- 添加 schedule_id 字段
ALTER TABLE `production_record` 
ADD COLUMN IF NOT EXISTS `schedule_id` bigint DEFAULT NULL COMMENT '排程ID（关联production_schedule）' AFTER `equipment_no`;

-- 添加 product_code 字段
ALTER TABLE `production_record` 
ADD COLUMN IF NOT EXISTS `product_code` varchar(50) DEFAULT NULL COMMENT '产品编码' AFTER `schedule_id`;

-- 添加 product_name 字段
ALTER TABLE `production_record` 
ADD COLUMN IF NOT EXISTS `product_name` varchar(100) DEFAULT NULL COMMENT '产品名称' AFTER `product_code`;

-- 添加 schedule_id 索引
CREATE INDEX IF NOT EXISTS `idx_schedule_id` ON `production_record` (`schedule_id`);

-- ============================================
-- 三、为 equipment 表添加扩展字段（如果不存在）
-- 注意：如果字段已存在，执行会报错，请先检查表结构
-- ============================================

-- 添加组别字段
ALTER TABLE `equipment` 
ADD COLUMN IF NOT EXISTS `group_name` varchar(50) DEFAULT NULL COMMENT '组别' AFTER `equipment_name`;

-- 添加机台号字段
ALTER TABLE `equipment` 
ADD COLUMN IF NOT EXISTS `machine_no` varchar(50) DEFAULT NULL COMMENT '机台号' AFTER `group_name`;

-- 添加设备型号字段
ALTER TABLE `equipment` 
ADD COLUMN IF NOT EXISTS `equipment_model` varchar(100) DEFAULT NULL COMMENT '设备型号' AFTER `machine_no`;

-- 添加机械手型号字段
ALTER TABLE `equipment` 
ADD COLUMN IF NOT EXISTS `robot_model` varchar(100) DEFAULT NULL COMMENT '机械手型号' AFTER `equipment_model`;

-- 添加启用日期字段
ALTER TABLE `equipment` 
ADD COLUMN IF NOT EXISTS `enable_date` date DEFAULT NULL COMMENT '启用日期' AFTER `robot_model`;

-- 添加使用年限字段
ALTER TABLE `equipment` 
ADD COLUMN IF NOT EXISTS `service_life` int DEFAULT NULL COMMENT '使用年限（年）' AFTER `enable_date`;

-- 添加模温机字段
ALTER TABLE `equipment` 
ADD COLUMN IF NOT EXISTS `mold_temp_machine` varchar(100) DEFAULT NULL COMMENT '模温机' AFTER `service_life`;

-- 添加冻水机字段
ALTER TABLE `equipment` 
ADD COLUMN IF NOT EXISTS `chiller` varchar(100) DEFAULT NULL COMMENT '冻水机' AFTER `mold_temp_machine`;

-- 添加基本排模字段
ALTER TABLE `equipment` 
ADD COLUMN IF NOT EXISTS `basic_mold` varchar(100) DEFAULT NULL COMMENT '基本排模' AFTER `chiller`;

-- 添加备用排模1字段
ALTER TABLE `equipment` 
ADD COLUMN IF NOT EXISTS `spare_mold1` varchar(100) DEFAULT NULL COMMENT '备用排模1' AFTER `basic_mold`;

-- 添加备用排模2字段
ALTER TABLE `equipment` 
ADD COLUMN IF NOT EXISTS `spare_mold2` varchar(100) DEFAULT NULL COMMENT '备用排模2' AFTER `spare_mold1`;

-- 添加备用排模3字段
ALTER TABLE `equipment` 
ADD COLUMN IF NOT EXISTS `spare_mold3` varchar(100) DEFAULT NULL COMMENT '备用排模3' AFTER `spare_mold2`;

-- 添加索引
CREATE INDEX IF NOT EXISTS `idx_group_name` ON `equipment` (`group_name`);
CREATE INDEX IF NOT EXISTS `idx_machine_no` ON `equipment` (`machine_no`);

-- ============================================
-- 脚本执行完成
-- ============================================
