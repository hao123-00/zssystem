-- 5S区域管理：区域表
CREATE TABLE IF NOT EXISTS `site_5s_area` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_code` varchar(50) NOT NULL COMMENT '区域编码',
  `area_name` varchar(100) NOT NULL COMMENT '区域名称',
  `duty_name` varchar(100) NOT NULL COMMENT '职能名称（如：灯光管理、地面清洁）',
  `sort_order` int DEFAULT 0 COMMENT '排序号，越小越靠前',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_code` (`area_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5S区域表';

-- 区域拍照时段配置表
CREATE TABLE IF NOT EXISTS `site_5s_area_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` bigint NOT NULL COMMENT '区域ID',
  `slot_index` int NOT NULL COMMENT '时段序号（1=第一次，2=第二次...）',
  `scheduled_time` time NOT NULL COMMENT '规定拍照时间（如 08:00）',
  `tolerance_minutes` int DEFAULT 30 COMMENT '前后容忍分钟数，如30表示07:30-08:30内有效',
  `remark` varchar(200) DEFAULT NULL COMMENT '时段说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_area_id` (`area_id`),
  UNIQUE KEY `uk_area_slot` (`area_id`, `slot_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域拍照时段配置表';

-- 区域拍照记录表
CREATE TABLE IF NOT EXISTS `site_5s_area_photo` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` bigint NOT NULL COMMENT '区域ID',
  `photo_date` date NOT NULL COMMENT '拍照日期',
  `slot_index` int NOT NULL COMMENT '对应时段序号',
  `photo_path` varchar(500) NOT NULL COMMENT '照片存储路径',
  `uploader_id` bigint DEFAULT NULL COMMENT '上传人ID',
  `uploader_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `upload_time` datetime NOT NULL COMMENT '实际上传时间',
  `is_on_time` tinyint DEFAULT 1 COMMENT '是否按时：0-超时，1-按时',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_date_slot` (`area_id`, `photo_date`, `slot_index`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_photo_date` (`photo_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域拍照记录表';
