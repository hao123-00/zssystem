-- 为 equipment 表添加 basic_mold4（基本排模4）字段
-- 执行时间：按需执行

SET @dbname = DATABASE();
SET @tablename = 'equipment';
SET @columnname = 'basic_mold4';

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname
   AND TABLE_NAME = @tablename
   AND COLUMN_NAME = @columnname) = 0,
  'ALTER TABLE equipment ADD COLUMN basic_mold4 varchar(100) DEFAULT NULL COMMENT ''基本排模4'' AFTER spare_mold3;',
  'SELECT 1;'
));

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;
