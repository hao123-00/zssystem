-- 用户表新增：工号、班组、岗位、类别、入职日期

SET @dbname = DATABASE();
SET @tablename = 'sys_user';

SET @col = 'employee_no';
SET @stmt = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @col) = 0,
  'ALTER TABLE sys_user ADD COLUMN employee_no varchar(50) DEFAULT NULL COMMENT ''工号'' AFTER phone;',
  'SELECT 1;'
));
PREPARE s FROM @stmt; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'team';
SET @stmt = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @col) = 0,
  'ALTER TABLE sys_user ADD COLUMN team varchar(50) DEFAULT NULL COMMENT ''班组'' AFTER employee_no;',
  'SELECT 1;'
));
PREPARE s FROM @stmt; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'position';
SET @stmt = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @col) = 0,
  'ALTER TABLE sys_user ADD COLUMN position varchar(50) DEFAULT NULL COMMENT ''岗位'' AFTER team;',
  'SELECT 1;'
));
PREPARE s FROM @stmt; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'category';
SET @stmt = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @col) = 0,
  'ALTER TABLE sys_user ADD COLUMN category varchar(50) DEFAULT NULL COMMENT ''类别'' AFTER position;',
  'SELECT 1;'
));
PREPARE s FROM @stmt; EXECUTE s; DEALLOCATE PREPARE s;

SET @col = 'hire_date';
SET @stmt = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @col) = 0,
  'ALTER TABLE sys_user ADD COLUMN hire_date date DEFAULT NULL COMMENT ''入职日期'' AFTER category;',
  'SELECT 1;'
));
PREPARE s FROM @stmt; EXECUTE s; DEALLOCATE PREPARE s;
