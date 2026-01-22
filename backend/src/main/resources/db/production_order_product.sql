-- 创建订单产品表，支持一个订单包含多个产品
CREATE TABLE IF NOT EXISTS `production_order_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID（关联production_order表）',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `product_code` varchar(50) DEFAULT NULL COMMENT '产品编码',
  `order_quantity` int NOT NULL COMMENT '订单数量',
  `daily_capacity` int NOT NULL COMMENT '日产能（每天能生产的数量）',
  `sort_order` int DEFAULT 0 COMMENT '排序（用于确定生产优先级，1-第一产品，2-第二产品，3-第三产品）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单产品表';

-- 修改生产订单表，移除产品相关字段（保留订单号和机台号）
-- 注意：如果字段不存在，这些语句会报错，但可以忽略
SET @dbname = DATABASE();
SET @tablename = 'production_order';
SET @columnname = 'product_code';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  CONCAT('ALTER TABLE ', @tablename, ' DROP COLUMN ', @columnname),
  'SELECT 1'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;

SET @columnname = 'product_name';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  CONCAT('ALTER TABLE ', @tablename, ' DROP COLUMN ', @columnname),
  'SELECT 1'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;

SET @columnname = 'order_quantity';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  CONCAT('ALTER TABLE ', @tablename, ' DROP COLUMN ', @columnname),
  'SELECT 1'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;

SET @columnname = 'daily_capacity';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  CONCAT('ALTER TABLE ', @tablename, ' DROP COLUMN ', @columnname),
  'SELECT 1'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;

SET @columnname = 'sort_order';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  CONCAT('ALTER TABLE ', @tablename, ' DROP COLUMN ', @columnname),
  'SELECT 1'
));
PREPARE alterIfExists FROM @preparedStatement;
EXECUTE alterIfExists;
DEALLOCATE PREPARE alterIfExists;
