-- ============================================
-- 注塑部管理系统 - 全量表结构脚本
-- 说明：本脚本包含系统使用的所有数据库表结构（共31张表）
-- 适用于新建数据库或结构参考，按依赖顺序排列
-- 注意：process_file_detail 表工艺参数字段较多，完整定义见 process_file_detail.sql
-- ============================================

SET NAMES utf8mb4;

-- ============================================
-- 一、系统基础表
-- ============================================

-- 1. 系统用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `employee_no` varchar(50) DEFAULT NULL COMMENT '工号',
  `team` varchar(50) DEFAULT NULL COMMENT '班组',
  `position` varchar(50) DEFAULT NULL COMMENT '岗位',
  `category` varchar(50) DEFAULT NULL COMMENT '类别',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 部门表
CREATE TABLE IF NOT EXISTS `sys_department` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父部门ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '部门名称',
  `dept_code` varchar(50) DEFAULT NULL COMMENT '部门编码',
  `leader` varchar(50) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` int DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 3. 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` varchar(50) DEFAULT NULL COMMENT '角色编码',
  `role_name` varchar(50) DEFAULT NULL COMMENT '角色名称',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `status` int DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 4. 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT 0 COMMENT '父权限ID',
  `permission_code` varchar(100) DEFAULT NULL COMMENT '权限编码',
  `permission_name` varchar(100) DEFAULT NULL COMMENT '权限名称',
  `permission_type` int DEFAULT 1 COMMENT '类型：1-菜单，2-按钮',
  `path` varchar(200) DEFAULT NULL COMMENT '路径',
  `component` varchar(200) DEFAULT NULL COMMENT '组件',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` int DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 5. 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 6. 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 7. 员工表
CREATE TABLE IF NOT EXISTS `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_no` varchar(50) DEFAULT NULL COMMENT '工号',
  `name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `gender` int DEFAULT NULL COMMENT '性别：0-女，1-男',
  `age` int DEFAULT NULL COMMENT '年龄',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `department_id` bigint DEFAULT NULL COMMENT '部门ID',
  `position` varchar(50) DEFAULT NULL COMMENT '岗位',
  `entry_date` date DEFAULT NULL COMMENT '入职日期',
  `status` int DEFAULT 1 COMMENT '状态：0-离职，1-在职',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_department_id` (`department_id`),
  KEY `idx_employee_no` (`employee_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- ============================================
-- 二、设备管理相关表
-- ============================================

-- 8. 设备表
CREATE TABLE IF NOT EXISTS `equipment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_no` varchar(50) NOT NULL COMMENT '设备编号',
  `equipment_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `group_name` varchar(50) DEFAULT NULL COMMENT '组别',
  `machine_no` varchar(50) DEFAULT NULL COMMENT '机台号',
  `equipment_model` varchar(100) DEFAULT NULL COMMENT '设备型号',
  `manufacturer` varchar(100) DEFAULT NULL COMMENT '制造商',
  `purchase_date` date DEFAULT NULL COMMENT '购买日期',
  `robot_model` varchar(100) DEFAULT NULL COMMENT '机械手型号',
  `enable_date` date DEFAULT NULL COMMENT '启用日期',
  `service_life` varchar(20) DEFAULT NULL COMMENT '使用年限（年）',
  `mold_temp_machine` varchar(100) DEFAULT NULL COMMENT '模温机',
  `chiller` varchar(100) DEFAULT NULL COMMENT '冻水机',
  `basic_mold` varchar(100) DEFAULT NULL COMMENT '基本排模',
  `spare_mold1` varchar(100) DEFAULT NULL COMMENT '备用排模1',
  `spare_mold2` varchar(100) DEFAULT NULL COMMENT '备用排模2',
  `spare_mold3` varchar(100) DEFAULT NULL COMMENT '备用排模3',
  `basic_mold4` varchar(100) DEFAULT NULL COMMENT '基本排模4',
  `status` int DEFAULT 1 COMMENT '状态：0-停用，1-正常，2-维修中',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_equipment_no` (`equipment_no`),
  KEY `idx_group_name` (`group_name`),
  KEY `idx_machine_no` (`machine_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 9. 设备可生产产品表
CREATE TABLE IF NOT EXISTS `equipment_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) NOT NULL COMMENT '设备编号（冗余）',
  `product_code` varchar(50) NOT NULL COMMENT '产品编码',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_equipment_product` (`equipment_id`, `product_code`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备可生产产品表';

-- 10. 设备点检表
CREATE TABLE IF NOT EXISTS `equipment_check` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) NOT NULL COMMENT '设备编号',
  `equipment_name` varchar(100) NOT NULL COMMENT '设备名称',
  `check_month` varchar(7) NOT NULL COMMENT '检查月份 YYYY-MM',
  `check_date` date NOT NULL COMMENT '检查日期',
  `checker_name` varchar(50) NOT NULL COMMENT '检点人姓名',
  `circuit_item1` tinyint DEFAULT NULL COMMENT '电路项1：1-正常，0-异常',
  `circuit_item2` tinyint DEFAULT NULL COMMENT '电路项2',
  `circuit_item3` tinyint DEFAULT NULL COMMENT '电路项3',
  `frame_item1` tinyint DEFAULT NULL COMMENT '机架项1',
  `frame_item2` tinyint DEFAULT NULL COMMENT '机架项2',
  `frame_item3` tinyint DEFAULT NULL COMMENT '机架项3',
  `oil_item1` tinyint DEFAULT NULL COMMENT '油路项1',
  `oil_item2` tinyint DEFAULT NULL COMMENT '油路项2',
  `oil_item3` tinyint DEFAULT NULL COMMENT '油路项3',
  `oil_item4` tinyint DEFAULT NULL COMMENT '油路项4',
  `oil_item5` tinyint DEFAULT NULL COMMENT '油路项5',
  `peripheral_item1` tinyint DEFAULT NULL COMMENT '周边设备项1',
  `peripheral_item2` tinyint DEFAULT NULL COMMENT '周边设备项2',
  `peripheral_item3` tinyint DEFAULT NULL COMMENT '周边设备项3',
  `peripheral_item4` tinyint DEFAULT NULL COMMENT '周边设备项4',
  `peripheral_item5` tinyint DEFAULT NULL COMMENT '周边设备项5',
  `remark` varchar(1000) DEFAULT NULL COMMENT '异常项备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_equipment_date` (`equipment_id`, `check_date`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_check_month` (`check_month`),
  KEY `idx_check_date` (`check_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备点检表';

-- 11. 设备维护记录表
CREATE TABLE IF NOT EXISTS `equipment_maintenance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `maintenance_date` date NOT NULL COMMENT '维护日期',
  `maintenance_type` varchar(50) DEFAULT NULL COMMENT '维护类型',
  `maintenance_content` varchar(500) DEFAULT NULL COMMENT '维护内容',
  `maintainer_name` varchar(50) DEFAULT NULL COMMENT '维护人员',
  `cost` decimal(10,2) DEFAULT NULL COMMENT '维护费用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_maintenance_date` (`maintenance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维护记录表';

-- 12. 设备故障记录表
CREATE TABLE IF NOT EXISTS `equipment_fault` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `fault_date` datetime NOT NULL COMMENT '故障日期',
  `fault_description` varchar(500) NOT NULL COMMENT '故障描述',
  `handle_method` varchar(500) DEFAULT NULL COMMENT '处理方式',
  `handler_name` varchar(50) DEFAULT NULL COMMENT '处理人员',
  `handle_date` datetime DEFAULT NULL COMMENT '处理日期',
  `status` tinyint DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-已处理',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_fault_date` (`fault_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备故障记录表';

-- ============================================
-- 三、交接班与生产管理表
-- ============================================

-- 13. 交接班记录表
CREATE TABLE IF NOT EXISTS `handover_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) NOT NULL COMMENT '设备编号',
  `record_date` date NOT NULL COMMENT '记录日期',
  `shift` varchar(20) DEFAULT NULL COMMENT '班次',
  `product_name` varchar(100) DEFAULT NULL COMMENT '产品名称',
  `material` varchar(100) DEFAULT NULL COMMENT '材质',
  `equipment_cleaning` varchar(50) DEFAULT NULL COMMENT '设备清洁',
  `floor_cleaning` varchar(50) DEFAULT NULL COMMENT '地面清洁',
  `leakage` varchar(50) DEFAULT NULL COMMENT '有无漏油',
  `item_placement` varchar(50) DEFAULT NULL COMMENT '物品摆放',
  `injection_machine` varchar(50) DEFAULT NULL COMMENT '注塑机',
  `robot` varchar(50) DEFAULT NULL COMMENT '机械手',
  `assembly_line` varchar(50) DEFAULT NULL COMMENT '流水线',
  `mold` varchar(50) DEFAULT NULL COMMENT '模具',
  `process` varchar(200) DEFAULT NULL COMMENT '工艺',
  `handover_leader` varchar(50) DEFAULT NULL COMMENT '交接组长',
  `receiving_leader` varchar(50) DEFAULT NULL COMMENT '接班组长',
  `photo_path` varchar(500) DEFAULT NULL COMMENT '拍照照片路径',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_record_date` (`record_date`),
  KEY `idx_equipment_month` (`equipment_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交接班记录表';

-- 14. 设备生产产品配置表
CREATE TABLE IF NOT EXISTS `equipment_production_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) NOT NULL COMMENT '设备编号',
  `product_code` varchar(50) DEFAULT NULL COMMENT '产品编码',
  `product_name` varchar(100) DEFAULT NULL COMMENT '产品名称',
  `order_quantity` int NOT NULL COMMENT '订单数量',
  `daily_capacity` int NOT NULL COMMENT '日产能',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备生产产品配置表';

-- 15. 生产订单表
CREATE TABLE IF NOT EXISTS `production_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `machine_no` varchar(50) NOT NULL COMMENT '机台号',
  `equipment_id` bigint DEFAULT NULL COMMENT '设备ID',
  `status` tinyint DEFAULT 0 COMMENT '状态：0-待排程，1-排程中，2-已完成',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_machine_no` (`machine_no`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产订单表';

-- 16. 订单产品表
CREATE TABLE IF NOT EXISTS `production_order_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `product_code` varchar(50) DEFAULT NULL COMMENT '产品编码',
  `order_quantity` int NOT NULL COMMENT '订单数量',
  `daily_capacity` int NOT NULL COMMENT '日产能',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单产品表';

-- 17. 27天生产计划排程表
CREATE TABLE IF NOT EXISTS `production_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) NOT NULL COMMENT '设备编号',
  `machine_no` varchar(50) DEFAULT NULL COMMENT '机台号',
  `schedule_date` date NOT NULL COMMENT '排程日期',
  `day_number` int NOT NULL COMMENT '第几天(1-27)',
  `product_code` varchar(50) DEFAULT NULL COMMENT '产品编码',
  `product_name` varchar(100) DEFAULT NULL COMMENT '产品名称',
  `production_quantity` int DEFAULT NULL COMMENT '排产数量',
  `daily_capacity` int NOT NULL COMMENT '当天产能',
  `remaining_quantity` int NOT NULL COMMENT '剩余数量',
  `order_id` bigint DEFAULT NULL COMMENT '关联生产订单ID',
  `is_sunday` tinyint DEFAULT 0 COMMENT '是否星期天：0-否，1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_machine_date` (`machine_no`, `schedule_date`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_schedule_date` (`schedule_date`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='27天生产计划排程表';

-- 18. 生产计划表
CREATE TABLE IF NOT EXISTS `production_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_no` varchar(50) DEFAULT NULL COMMENT '计划编号',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `equipment_id` bigint DEFAULT NULL COMMENT '设备ID',
  `mold_id` bigint DEFAULT NULL COMMENT '模具ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作员ID',
  `plan_start_time` datetime DEFAULT NULL COMMENT '计划开始时间',
  `plan_end_time` datetime DEFAULT NULL COMMENT '计划结束时间',
  `plan_quantity` int DEFAULT NULL COMMENT '计划数量',
  `status` int DEFAULT 0 COMMENT '状态：0-待执行，1-执行中，2-已完成',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产计划表';

-- 19. 生产记录表
CREATE TABLE IF NOT EXISTS `production_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_no` varchar(50) DEFAULT NULL COMMENT '记录编号',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `plan_id` bigint DEFAULT NULL COMMENT '计划ID',
  `equipment_id` bigint DEFAULT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) DEFAULT NULL COMMENT '设备编号',
  `schedule_id` bigint DEFAULT NULL COMMENT '排程ID',
  `product_code` varchar(50) DEFAULT NULL COMMENT '产品编码',
  `product_name` varchar(100) DEFAULT NULL COMMENT '产品名称',
  `mold_id` bigint DEFAULT NULL COMMENT '模具ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作员ID',
  `production_date` date DEFAULT NULL COMMENT '生产日期',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `quantity` int DEFAULT NULL COMMENT '产量',
  `defect_quantity` int DEFAULT NULL COMMENT '不良数量',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_production_date` (`production_date`),
  KEY `idx_schedule_id` (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产记录表';

-- ============================================
-- 四、工艺文件管理表
-- ============================================

-- 20. 工艺文件主表
CREATE TABLE IF NOT EXISTS `process_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_no` varchar(50) NOT NULL COMMENT '工艺文件编号',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) NOT NULL COMMENT '设备编号',
  `machine_no` varchar(50) NOT NULL COMMENT '机台号',
  `file_name` varchar(200) NOT NULL COMMENT '文件名称',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(20) NOT NULL COMMENT '文件类型',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-待审核，2-待会签，3-待批准，5-已批准，-1-已驳回，-2-已作废',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) NOT NULL COMMENT '创建人姓名',
  `submit_time` datetime DEFAULT NULL COMMENT '提交审批时间',
  `approval_time` datetime DEFAULT NULL COMMENT '最终批准时间',
  `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
  `invalid_time` datetime DEFAULT NULL COMMENT '作废时间',
  `seal_image_path` varchar(500) DEFAULT NULL COMMENT '电子受控章图片路径',
  `is_current` int NOT NULL DEFAULT 1 COMMENT '是否当前版本：1-是，0-否',
  `parent_file_id` bigint DEFAULT NULL COMMENT '父文件ID',
  `change_reason` varchar(500) DEFAULT NULL COMMENT '变更原因',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_no` (`file_no`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_current` (`is_current`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工艺文件主表';

-- 21. 工艺文件审批记录表
CREATE TABLE IF NOT EXISTS `process_file_approval` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_id` bigint NOT NULL COMMENT '工艺文件ID',
  `file_no` varchar(50) NOT NULL COMMENT '工艺文件编号',
  `approval_level` int NOT NULL COMMENT '审批级别：1-车间主任，2-注塑部经理，3-生产技术部经理',
  `approver_id` bigint NOT NULL COMMENT '审批人ID',
  `approver_name` varchar(50) NOT NULL COMMENT '审批人姓名',
  `approver_role` varchar(50) NOT NULL COMMENT '审批人角色',
  `approval_result` int NOT NULL COMMENT '审批结果：1-通过，0-驳回',
  `approval_opinion` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `approval_time` datetime NOT NULL COMMENT '审批时间',
  `signature_id` bigint DEFAULT NULL COMMENT '电子签名ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_file_id` (`file_id`),
  KEY `idx_approver_id` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工艺文件审批记录表';

-- 22. 工艺文件电子受控章表
CREATE TABLE IF NOT EXISTS `process_file_seal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_id` bigint NOT NULL COMMENT '工艺文件ID',
  `file_no` varchar(50) NOT NULL COMMENT '工艺文件编号',
  `seal_no` varchar(50) NOT NULL COMMENT '印章编号',
  `seal_type` varchar(50) DEFAULT '受控章' COMMENT '印章类型',
  `seal_content` varchar(200) DEFAULT NULL COMMENT '印章内容',
  `seal_image_path` varchar(500) NOT NULL COMMENT '印章图片路径',
  `seal_time` datetime NOT NULL COMMENT '盖章时间',
  `seal_by_id` bigint NOT NULL COMMENT '盖章人ID',
  `seal_by_name` varchar(50) NOT NULL COMMENT '盖章人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_id` (`file_id`),
  KEY `idx_file_no` (`file_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工艺文件电子受控章表';

-- 23. 工艺文件详细内容表
CREATE TABLE IF NOT EXISTS `process_file_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_id` bigint NOT NULL COMMENT '工艺文件ID',
  `file_no` varchar(50) NOT NULL COMMENT '工艺文件编号',
  `product_model` varchar(100) DEFAULT NULL COMMENT '产品型号',
  `product_name` varchar(200) DEFAULT NULL COMMENT '产品名称',
  `mold_manufacturing_company` varchar(200) DEFAULT NULL COMMENT '模具制造公司',
  `part_name` varchar(200) DEFAULT NULL COMMENT '零件名称',
  `project_leader` varchar(50) DEFAULT NULL COMMENT '项目负责人',
  `material_name` varchar(100) DEFAULT NULL COMMENT '材料名称',
  `material_grade` varchar(100) DEFAULT NULL COMMENT '材料牌号',
  `material_color` varchar(50) DEFAULT NULL COMMENT '材料颜色',
  `equipment_id` bigint DEFAULT NULL COMMENT '设备ID',
  `equipment_no` varchar(50) DEFAULT NULL COMMENT '设备编号',
  `machine_no` varchar(50) DEFAULT NULL COMMENT '机台号',
  `equipment_name` varchar(200) DEFAULT NULL COMMENT '设备名称/规格',
  `drying_equipment` varchar(200) DEFAULT NULL COMMENT '干燥使用设备',
  `material_fill_height` varchar(50) DEFAULT NULL COMMENT '盛料高度',
  `drying_temp` varchar(50) DEFAULT NULL COMMENT '干燥温度',
  `product_key_dimension_image1` varchar(500) DEFAULT NULL COMMENT '产品关键尺寸图片1路径',
  `product_key_dimension_image2` varchar(500) DEFAULT NULL COMMENT '产品关键尺寸图片2路径',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_file_id` (`file_id`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工艺文件详细内容表';

-- 24. 工艺文件电子签名表
CREATE TABLE IF NOT EXISTS `process_file_signature` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_id` bigint NOT NULL COMMENT '工艺文件ID',
  `file_no` varchar(50) NOT NULL COMMENT '工艺文件编号',
  `signature_type` varchar(50) NOT NULL COMMENT '签名类型',
  `signer_id` bigint NOT NULL COMMENT '签名人ID',
  `signer_name` varchar(50) NOT NULL COMMENT '签名人姓名',
  `signer_role` varchar(50) NOT NULL COMMENT '签名人角色',
  `signature_image_path` varchar(500) NOT NULL COMMENT '签名图片路径',
  `signature_time` datetime NOT NULL COMMENT '签名时间',
  `ip_address` varchar(50) DEFAULT NULL COMMENT '签名IP地址',
  `device_info` varchar(200) DEFAULT NULL COMMENT '设备信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_file_id` (`file_id`),
  KEY `idx_signature_type` (`signature_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工艺文件电子签名表';

-- ============================================
-- 五、现场5S管理表
-- ============================================

-- 25. 5S检查记录表
CREATE TABLE IF NOT EXISTS `site_5s_check` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `check_no` varchar(50) NOT NULL COMMENT '检查单号',
  `check_date` date NOT NULL COMMENT '检查日期',
  `check_area` varchar(100) NOT NULL COMMENT '检查区域',
  `checker_name` varchar(50) DEFAULT NULL COMMENT '检查人员',
  `sort_score` int DEFAULT NULL COMMENT '整理得分(0-20)',
  `set_score` int DEFAULT NULL COMMENT '整顿得分(0-20)',
  `shine_score` int DEFAULT NULL COMMENT '清扫得分(0-20)',
  `standardize_score` int DEFAULT NULL COMMENT '清洁得分(0-20)',
  `sustain_score` int DEFAULT NULL COMMENT '素养得分(0-20)',
  `total_score` int DEFAULT NULL COMMENT '总分(0-100)',
  `problem_description` varchar(1000) DEFAULT NULL COMMENT '问题描述',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_no` (`check_no`),
  KEY `idx_check_date` (`check_date`),
  KEY `idx_check_area` (`check_area`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5S检查记录表';

-- 26. 5S整改任务表
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
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_check_id` (`check_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5S整改任务表';

-- 27. 区域表（5S通用）
CREATE TABLE IF NOT EXISTS `site_area` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_code` varchar(50) NOT NULL COMMENT '区域编码',
  `area_name` varchar(100) NOT NULL COMMENT '区域名称',
  `department` varchar(50) DEFAULT NULL COMMENT '负责部门',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_code` (`area_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域表';

-- 28. 5S区域表（灯光管理等，含早间/晚间拍照）
CREATE TABLE IF NOT EXISTS `site_5s_area` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_code` varchar(50) NOT NULL COMMENT '区域编码',
  `area_name` varchar(100) NOT NULL COMMENT '区域名称',
  `check_item` varchar(100) NOT NULL COMMENT '检查项目（如：灯光管理）',
  `responsible_user_id` bigint DEFAULT NULL COMMENT '负责人1（注塑组长）',
  `responsible_user_id_2` bigint DEFAULT NULL COMMENT '负责人2（注塑组长）',
  `morning_photo_time` time DEFAULT '08:00:00' COMMENT '早间拍照时间',
  `evening_photo_time` time DEFAULT '16:00:00' COMMENT '晚间拍照时间',
  `sort_order` int DEFAULT 0 COMMENT '排序号',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_code` (`area_code`),
  KEY `idx_status` (`status`),
  KEY `idx_responsible_user_id` (`responsible_user_id`),
  KEY `idx_responsible_user_id_2` (`responsible_user_id_2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5S区域表（灯光管理）';

-- 29. 区域拍照时段配置表
CREATE TABLE IF NOT EXISTS `site_5s_area_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` bigint NOT NULL COMMENT '区域ID',
  `slot_index` int NOT NULL COMMENT '时段序号（1=早间，2=晚间）',
  `scheduled_time` time NOT NULL COMMENT '规定拍照时间',
  `tolerance_minutes` int DEFAULT 30 COMMENT '前后容忍分钟数',
  `remark` varchar(200) DEFAULT NULL COMMENT '时段说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_area_id` (`area_id`),
  UNIQUE KEY `uk_area_slot` (`area_id`, `slot_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域拍照时段配置表';

-- 30. 区域拍照记录表
CREATE TABLE IF NOT EXISTS `site_5s_area_photo` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` bigint NOT NULL COMMENT '区域ID',
  `photo_date` date NOT NULL COMMENT '拍照日期',
  `slot_index` int NOT NULL COMMENT '时段序号（1=早间，2=晚间）',
  `photo_path` varchar(500) NOT NULL COMMENT '照片存储路径',
  `uploader_id` bigint DEFAULT NULL COMMENT '上传人ID',
  `uploader_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `upload_time` datetime NOT NULL COMMENT '实际上传时间',
  `is_on_time` tinyint DEFAULT 1 COMMENT '是否按时：0-超时，1-按时',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_date_slot` (`area_id`, `photo_date`, `slot_index`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_photo_date` (`photo_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域拍照记录表';

-- 31. 区域放假记录表
CREATE TABLE IF NOT EXISTS `site_5s_area_day_off` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` bigint NOT NULL COMMENT '区域ID',
  `off_date` date NOT NULL COMMENT '放假日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_off_date` (`area_id`, `off_date`),
  KEY `idx_off_date` (`off_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域放假记录表';

-- ============================================
-- 脚本结束
-- ============================================
