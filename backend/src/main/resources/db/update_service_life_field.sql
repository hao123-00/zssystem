-- ============================================
-- 修改使用年限字段类型：从 int 改为 varchar
-- 用于支持"X年X个月"格式
-- ============================================

-- 修改 equipment 表的 service_life 字段类型
ALTER TABLE `equipment` 
MODIFY COLUMN `service_life` varchar(50) DEFAULT NULL COMMENT '使用年限（格式：X年X个月）' AFTER `enable_date`;
