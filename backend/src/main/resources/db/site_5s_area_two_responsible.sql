-- 区域指定两名负责人（组长）
ALTER TABLE `site_5s_area`
  ADD COLUMN `responsible_user_id_2` bigint DEFAULT NULL COMMENT '负责人2（注塑组长）' AFTER `responsible_user_id`,
  ADD KEY `idx_responsible_user_id_2` (`responsible_user_id_2`);
