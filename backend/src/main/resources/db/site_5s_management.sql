-- 现场5S管理功能数据库表

-- 1. 5S检查记录表
CREATE TABLE IF NOT EXISTS `site_5s_check` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `check_no` varchar(50) NOT NULL COMMENT '检查单号',
  `check_date` date NOT NULL COMMENT '检查日期',
  `check_area` varchar(100) NOT NULL COMMENT '检查区域',
  `checker_name` varchar(50) DEFAULT NULL COMMENT '检查人员',
  -- 5S各项评分（每项满分20分）
  `sort_score` int DEFAULT NULL COMMENT '整理得分（0-20）',
  `set_score` int DEFAULT NULL COMMENT '整顿得分（0-20）',
  `shine_score` int DEFAULT NULL COMMENT '清扫得分（0-20）',
  `standardize_score` int DEFAULT NULL COMMENT '清洁得分（0-20）',
  `sustain_score` int DEFAULT NULL COMMENT '素养得分（0-20）',
  `total_score` int DEFAULT NULL COMMENT '总分（0-100）',
  `problem_description` varchar(1000) DEFAULT NULL COMMENT '问题描述',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_no` (`check_no`),
  KEY `idx_check_date` (`check_date`),
  KEY `idx_check_area` (`check_area`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5S检查记录表';

-- 2. 5S整改任务表
CREATE TABLE IF NOT EXISTS `site_5s_rectification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_no` varchar(50) NOT NULL COMMENT '任务编号',
  `check_id` bigint NOT NULL COMMENT '检查记录ID',
  `problem_description` varchar(1000) NOT NULL COMMENT '问题描述',
  `area` varchar(100) NOT NULL COMMENT '区域',
  `department` varchar(50) DEFAULT NULL COMMENT '责任部门',
  `responsible_person` varchar(50) DEFAULT NULL COMMENT '责任人',
  `deadline` date DEFAULT NULL COMMENT '整改期限',
  `rectification_content` varchar(1000) DEFAULT NULL COMMENT '整改内容',
  `rectification_date` date DEFAULT NULL COMMENT '整改日期',
  `verifier_name` varchar(50) DEFAULT NULL COMMENT '验证人员',
  `verification_date` date DEFAULT NULL COMMENT '验证日期',
  `verification_result` varchar(500) DEFAULT NULL COMMENT '验证结果',
  `status` tinyint DEFAULT 0 COMMENT '状态：0-待整改，1-整改中，2-待验证，3-已完成',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_check_id` (`check_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deadline` (`deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5S整改任务表';

-- 3. 区域表
CREATE TABLE IF NOT EXISTS `site_area` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_code` varchar(50) NOT NULL COMMENT '区域编码',
  `area_name` varchar(100) NOT NULL COMMENT '区域名称',
  `department` varchar(50) DEFAULT NULL COMMENT '负责部门',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_code` (`area_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域表';
