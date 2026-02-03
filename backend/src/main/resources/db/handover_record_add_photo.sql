-- 交接班记录表增加照片路径字段（拍照/上传照片路径，15天后自动删除，不显示）
SET @dbname = DATABASE();
SET @tablename = 'handover_record';
SET @columnname = 'photo_path';

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname
   AND TABLE_NAME = @tablename
   AND COLUMN_NAME = @columnname) = 0,
  'ALTER TABLE handover_record ADD COLUMN photo_path varchar(500) DEFAULT NULL COMMENT ''拍照照片路径，15天后自动删除'' AFTER receiving_leader;',
  'SELECT 1;'
));

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;
