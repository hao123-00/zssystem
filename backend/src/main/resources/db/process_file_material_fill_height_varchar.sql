-- 盛料高度改为支持文本（如"满桶"），由 decimal 改为 varchar
ALTER TABLE process_file_detail MODIFY COLUMN material_fill_height varchar(100) DEFAULT NULL COMMENT '盛料高度';
