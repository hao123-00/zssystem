-- 区域管理需求调整：增加负责人、检查项目、早间/晚间拍照时间
-- 1. 将 duty_name 改为 check_item（检查项目）
-- 2. 增加 responsible_user_id（负责人，注塑组长）
-- 3. 增加 morning_photo_time（早间拍照时间）、evening_photo_time（晚间拍照时间）
-- slot_index: 1=早间, 2=晚间

ALTER TABLE `site_5s_area`
  CHANGE COLUMN `duty_name` `check_item` varchar(100) NOT NULL COMMENT '检查项目（如：灯光管理、地面清洁）',
  ADD COLUMN `responsible_user_id` bigint DEFAULT NULL COMMENT '负责人ID（注塑组长）' AFTER `check_item`,
  ADD COLUMN `morning_photo_time` time DEFAULT '08:00:00' COMMENT '早间拍照时间' AFTER `responsible_user_id`,
  ADD COLUMN `evening_photo_time` time DEFAULT '16:00:00' COMMENT '晚间拍照时间' AFTER `morning_photo_time`,
  ADD KEY `idx_responsible_user_id` (`responsible_user_id`);
