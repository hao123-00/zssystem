-- 干燥温度改为支持文本（如"60℃-70℃"），由 decimal 改为 varchar
ALTER TABLE process_file_detail MODIFY COLUMN drying_temp varchar(50) DEFAULT NULL COMMENT '干燥温度';
