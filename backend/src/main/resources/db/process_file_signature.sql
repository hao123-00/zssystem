-- 工艺文件电子签名表
CREATE TABLE IF NOT EXISTS `process_file_signature` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_id` bigint NOT NULL COMMENT '工艺文件ID',
  `file_no` varchar(50) NOT NULL COMMENT '工艺文件编号（冗余）',
  `signature_type` varchar(50) NOT NULL COMMENT '签名类型：SUBMIT-提交，APPROVE_LEVEL1-审核（车间主任），APPROVE_LEVEL2-会签（注塑部经理），APPROVE_LEVEL3-批准（生产技术部经理）',
  `signer_id` bigint NOT NULL COMMENT '签名人ID',
  `signer_name` varchar(50) NOT NULL COMMENT '签名人姓名',
  `signer_role` varchar(50) NOT NULL COMMENT '签名人角色',
  `signature_image_path` varchar(500) NOT NULL COMMENT '签名图片路径',
  `signature_time` datetime NOT NULL COMMENT '签名时间',
  `ip_address` varchar(50) DEFAULT NULL COMMENT '签名IP地址',
  `device_info` varchar(200) DEFAULT NULL COMMENT '设备信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_file_id` (`file_id`),
  KEY `idx_file_no` (`file_no`),
  KEY `idx_signer_id` (`signer_id`),
  KEY `idx_signature_type` (`signature_type`),
  KEY `idx_signature_time` (`signature_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工艺文件电子签名表';

-- 更新审批记录表，添加签名ID字段
-- 检查字段是否存在，如果不存在则添加
SET @dbname = DATABASE();
SET @tablename = 'process_file_approval';
SET @columnname = 'signature_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND COLUMN_NAME = @columnname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' bigint DEFAULT NULL COMMENT ''电子签名ID'' AFTER `approval_time`;'),
  'SELECT 1;'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;

-- 添加索引（如果不存在）
SET @indexname = 'idx_signature_id';
SET @preparedStatement2 = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
   WHERE TABLE_SCHEMA = @dbname 
   AND TABLE_NAME = @tablename 
   AND INDEX_NAME = @indexname) = 0,
  CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX ', @indexname, ' (', @columnname, ');'),
  'SELECT 1;'
));
PREPARE alterIfExists2 FROM @preparedStatement2;
EXECUTE alterIfExists2;
DEALLOCATE PREPARE alterIfExists2;
