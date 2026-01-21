-- 注塑部管理系统数据库初始化脚本

-- 1. 创建用户表（如果不存在）
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 如果表已存在但字段名是 name 而不是 real_name，则重命名
-- MySQL 8.0+ 支持 IF EXISTS，但为了兼容性，先检查再修改
-- 如果字段名是 name，则重命名为 real_name
SET @dbname = DATABASE();
SET @tablename = 'sys_user';
SET @columnname = 'name';
SET @newname = 'real_name';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) > 0,
  CONCAT('ALTER TABLE ', @tablename, ' CHANGE COLUMN ', @columnname, ' ', @newname, ' varchar(50) DEFAULT NULL COMMENT ''真实姓名'';'),
  'SELECT 1;'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;

-- 如果表已存在但缺少 real_name 字段（且不是从 name 重命名来的），则添加
ALTER TABLE `sys_user` 
ADD COLUMN IF NOT EXISTS `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名' AFTER `password`;

-- 3. 如果表已存在但缺少其他字段，则添加
ALTER TABLE `sys_user` 
ADD COLUMN IF NOT EXISTS `email` varchar(100) DEFAULT NULL COMMENT '邮箱' AFTER `real_name`,
ADD COLUMN IF NOT EXISTS `phone` varchar(20) DEFAULT NULL COMMENT '手机号' AFTER `email`,
ADD COLUMN IF NOT EXISTS `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用' AFTER `phone`,
ADD COLUMN IF NOT EXISTS `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `status`,
ADD COLUMN IF NOT EXISTS `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`,
ADD COLUMN IF NOT EXISTS `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除' AFTER `update_time`;

-- 4. 确保索引存在
CREATE INDEX IF NOT EXISTS `idx_status` ON `sys_user` (`status`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_username` ON `sys_user` (`username`);

-- 5. 初始化默认管理员账号（如果不存在）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `status`) 
SELECT 'admin', '$2a$10$M85tePnVnTl0MhOP3rSGjeOdqqb7H3Q5AsqKp/BRegqYbjjejSlE2', '系统管理员', 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'admin');
