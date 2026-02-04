-- 区域某日放假记录（该日不需拍照，状态显示为放假）
CREATE TABLE IF NOT EXISTS `site_5s_area_day_off` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` bigint NOT NULL COMMENT '区域ID',
  `off_date` date NOT NULL COMMENT '放假日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_off_date` (`area_id`, `off_date`),
  KEY `idx_off_date` (`off_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域放假记录表';
