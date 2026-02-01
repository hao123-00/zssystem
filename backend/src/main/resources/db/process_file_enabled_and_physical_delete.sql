-- 工艺文件管理功能修改：启用/搁置、物理删除
-- 1. 增加 enabled 字段：1-启用，0-搁置，同一机台号只能有一个启用
-- 2. 删除改为物理删除（代码层面实现，此脚本仅添加 enabled 字段）

-- 若列已存在可跳过
ALTER TABLE `process_file` ADD COLUMN `enabled` tinyint NOT NULL DEFAULT 0 COMMENT '启用状态：1-启用，0-搁置，同机台号只能有一个启用' AFTER `is_current`;
