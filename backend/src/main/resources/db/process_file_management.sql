-- ============================================
-- 注塑工艺文件管理功能 - 数据库表结构
-- 版本：V1.0
-- 日期：2026-01-24
-- ============================================

-- 1. 工艺文件主表
CREATE TABLE IF NOT EXISTS `process_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_no` varchar(50) NOT NULL COMMENT '工艺文件编号',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) NOT NULL COMMENT '设备编号（冗余）',
  `machine_no` varchar(50) NOT NULL COMMENT '机台号（冗余）',
  `file_name` varchar(200) NOT NULL COMMENT '文件名称',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(20) NOT NULL COMMENT '文件类型（xls, xlsx）',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-待车间主任审核，2-待注塑部经理会签，3-待生产技术部经理批准，5-已批准（生效中），-1-已驳回，-2-已作废',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) NOT NULL COMMENT '创建人姓名',
  `submit_time` datetime DEFAULT NULL COMMENT '提交审批时间',
  `approval_time` datetime DEFAULT NULL COMMENT '最终批准时间',
  `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
  `invalid_time` datetime DEFAULT NULL COMMENT '作废时间',
  `seal_image_path` varchar(500) DEFAULT NULL COMMENT '电子受控章图片路径',
  `is_current` int NOT NULL DEFAULT 1 COMMENT '是否当前版本：1-是，0-否',
  `parent_file_id` bigint DEFAULT NULL COMMENT '父文件ID（修改时关联）',
  `change_reason` varchar(500) DEFAULT NULL COMMENT '变更原因（修改时必填）',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_no` (`file_no`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_equipment_no` (`equipment_no`),
  KEY `idx_machine_no` (`machine_no`),
  KEY `idx_status` (`status`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_is_current` (`is_current`),
  KEY `idx_parent_file_id` (`parent_file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工艺文件主表';

-- 2. 工艺文件审批记录表
CREATE TABLE IF NOT EXISTS `process_file_approval` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_id` bigint NOT NULL COMMENT '工艺文件ID',
  `file_no` varchar(50) NOT NULL COMMENT '工艺文件编号（冗余）',
  `approval_level` int NOT NULL COMMENT '审批级别：1-车间主任审核，2-注塑部经理会签，3-生产技术部经理批准',
  `approver_id` bigint NOT NULL COMMENT '审批人ID',
  `approver_name` varchar(50) NOT NULL COMMENT '审批人姓名',
  `approver_role` varchar(50) NOT NULL COMMENT '审批人角色',
  `approval_result` int NOT NULL COMMENT '审批结果：1-通过，0-驳回',
  `approval_opinion` varchar(500) DEFAULT NULL COMMENT '审批意见（驳回时必填）',
  `approval_time` datetime NOT NULL COMMENT '审批时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_file_id` (`file_id`),
  KEY `idx_file_no` (`file_no`),
  KEY `idx_approver_id` (`approver_id`),
  KEY `idx_approval_level` (`approval_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工艺文件审批记录表';

-- 3. 工艺文件电子受控章表
CREATE TABLE IF NOT EXISTS `process_file_seal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_id` bigint NOT NULL COMMENT '工艺文件ID',
  `file_no` varchar(50) NOT NULL COMMENT '工艺文件编号（冗余）',
  `seal_no` varchar(50) NOT NULL COMMENT '印章编号',
  `seal_type` varchar(50) DEFAULT '受控章' COMMENT '印章类型',
  `seal_content` varchar(200) DEFAULT NULL COMMENT '印章内容',
  `seal_image_path` varchar(500) NOT NULL COMMENT '印章图片路径',
  `seal_time` datetime NOT NULL COMMENT '盖章时间',
  `seal_by_id` bigint NOT NULL COMMENT '盖章人ID（生产技术部经理）',
  `seal_by_name` varchar(50) NOT NULL COMMENT '盖章人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_id` (`file_id`),
  KEY `idx_file_no` (`file_no`),
  KEY `idx_seal_by_id` (`seal_by_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工艺文件电子受控章表';

-- 添加索引优化查询性能
CREATE INDEX idx_equipment_status ON process_file(equipment_id, status);
CREATE INDEX idx_status_current ON process_file(status, is_current);
CREATE INDEX idx_creator_status ON process_file(creator_id, status);
CREATE INDEX idx_submit_time ON process_file(submit_time);
CREATE INDEX idx_approval_time ON process_file(approval_time);
