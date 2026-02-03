-- 将交接班记录表的 record_date 从 DATE 改为 DATETIME，精确到时分秒
-- 已有数据会保留日期，时间默认为 00:00:00

ALTER TABLE `handover_record` MODIFY COLUMN `record_date` datetime NOT NULL COMMENT '记录日期时间（精确到时分秒）';
