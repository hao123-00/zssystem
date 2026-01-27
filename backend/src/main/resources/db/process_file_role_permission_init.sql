-- 工艺文件管理审批流程角色和权限初始化脚本
-- 执行时间：2026-01-25

-- ============================================
-- 1. 创建工艺文件审批流程所需的角色
-- ============================================

-- 注塑组长（编制/上传工艺文件）
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `status`, `create_time`, `update_time`, `deleted`)
SELECT 'INJECTION_LEADER', '注塑组长', '负责编制和上传注塑工艺文件', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_code` = 'INJECTION_LEADER' AND `deleted` = 0);

-- 车间主任（第一级审核）
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `status`, `create_time`, `update_time`, `deleted`)
SELECT 'WORKSHOP_DIRECTOR', '车间主任', '负责审核注塑工艺文件', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_code` = 'WORKSHOP_DIRECTOR' AND `deleted` = 0);

-- 注塑部经理（第二级会签）
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `status`, `create_time`, `update_time`, `deleted`)
SELECT 'INJECTION_MANAGER', '注塑部经理', '负责会签注塑工艺文件', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_code` = 'INJECTION_MANAGER' AND `deleted` = 0);

-- 生产技术部经理（第三级批准）
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `status`, `create_time`, `update_time`, `deleted`)
SELECT 'PRODUCTION_TECH_MANAGER', '生产技术部经理', '负责最终批准注塑工艺文件并盖电子受控章', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_code` = 'PRODUCTION_TECH_MANAGER' AND `deleted` = 0);

-- ============================================
-- 2. 创建工艺文件管理相关权限
-- ============================================

-- 获取生产管理菜单的ID（作为父权限）
SET @parent_menu_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PRODUCTION_MANAGEMENT' AND `deleted` = 0 LIMIT 1);

-- 如果生产管理菜单不存在，先创建它
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
SELECT 0, 'PRODUCTION_MANAGEMENT', '生产管理', 1, '/production', 'Layout', 'ProductionOutlined', 6, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'PRODUCTION_MANAGEMENT' AND `deleted` = 0);

SET @parent_menu_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PRODUCTION_MANAGEMENT' AND `deleted` = 0 LIMIT 1);

-- 工艺文件管理菜单
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
SELECT @parent_menu_id, 'PROCESS_FILE_MANAGEMENT', '工艺文件管理', 1, '/production/process-file', 'Production/ProcessFile/index', 'FileTextOutlined', 1, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_MANAGEMENT' AND `deleted` = 0);

-- 获取工艺文件管理菜单的ID
SET @process_file_menu_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_MANAGEMENT' AND `deleted` = 0 LIMIT 1);

-- 工艺文件上传权限（按钮）
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
SELECT @process_file_menu_id, 'PROCESS_FILE_UPLOAD', '工艺文件上传', 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_UPLOAD' AND `deleted` = 0);

-- 工艺文件提交审批权限（按钮）
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
SELECT @process_file_menu_id, 'PROCESS_FILE_SUBMIT', '工艺文件提交审批', 2, NULL, NULL, NULL, 2, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_SUBMIT' AND `deleted` = 0);

-- 工艺文件审核权限（按钮）- 车间主任
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
SELECT @process_file_menu_id, 'PROCESS_FILE_APPROVE_LEVEL1', '工艺文件审核（车间主任）', 2, NULL, NULL, NULL, 3, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_APPROVE_LEVEL1' AND `deleted` = 0);

-- 工艺文件会签权限（按钮）- 注塑部经理
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
SELECT @process_file_menu_id, 'PROCESS_FILE_APPROVE_LEVEL2', '工艺文件会签（注塑部经理）', 2, NULL, NULL, NULL, 4, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_APPROVE_LEVEL2' AND `deleted` = 0);

-- 工艺文件批准权限（按钮）- 生产技术部经理
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
SELECT @process_file_menu_id, 'PROCESS_FILE_APPROVE_LEVEL3', '工艺文件批准（生产技术部经理）', 2, NULL, NULL, NULL, 5, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_APPROVE_LEVEL3' AND `deleted` = 0);

-- 工艺文件查看权限（按钮）
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
SELECT @process_file_menu_id, 'PROCESS_FILE_VIEW', '工艺文件查看', 2, NULL, NULL, NULL, 6, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_VIEW' AND `deleted` = 0);

-- 工艺文件下载权限（按钮）
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
SELECT @process_file_menu_id, 'PROCESS_FILE_DOWNLOAD', '工艺文件下载', 2, NULL, NULL, NULL, 7, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_DOWNLOAD' AND `deleted` = 0);

-- 工艺文件修改权限（按钮）
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
SELECT @process_file_menu_id, 'PROCESS_FILE_EDIT', '工艺文件修改', 2, NULL, NULL, NULL, 8, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_EDIT' AND `deleted` = 0);

-- 工艺文件作废权限（按钮）
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
SELECT @process_file_menu_id, 'PROCESS_FILE_INVALIDATE', '工艺文件作废', 2, NULL, NULL, NULL, 9, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_INVALIDATE' AND `deleted` = 0);

-- 待我审批菜单
INSERT INTO `sys_permission` (`parent_id`, `permission_code`, `permission_name`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`)
SELECT @parent_menu_id, 'PROCESS_FILE_PENDING_APPROVAL', '待我审批', 1, '/production/process-file/pending-approval', 'Production/ProcessFile/PendingApprovalList', 'CheckCircleOutlined', 2, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_PENDING_APPROVAL' AND `deleted` = 0);

-- ============================================
-- 3. 建立角色和权限的关联关系
-- ============================================

-- 获取角色ID
SET @injection_leader_role_id = (SELECT `id` FROM `sys_role` WHERE `role_code` = 'INJECTION_LEADER' AND `deleted` = 0 LIMIT 1);
SET @workshop_director_role_id = (SELECT `id` FROM `sys_role` WHERE `role_code` = 'WORKSHOP_DIRECTOR' AND `deleted` = 0 LIMIT 1);
SET @injection_manager_role_id = (SELECT `id` FROM `sys_role` WHERE `role_code` = 'INJECTION_MANAGER' AND `deleted` = 0 LIMIT 1);
SET @production_tech_manager_role_id = (SELECT `id` FROM `sys_role` WHERE `role_code` = 'PRODUCTION_TECH_MANAGER' AND `deleted` = 0 LIMIT 1);

-- 获取权限ID
SET @process_file_menu_permission_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_MANAGEMENT' AND `deleted` = 0 LIMIT 1);
SET @process_file_upload_permission_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_UPLOAD' AND `deleted` = 0 LIMIT 1);
SET @process_file_submit_permission_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_SUBMIT' AND `deleted` = 0 LIMIT 1);
SET @process_file_approve_level1_permission_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_APPROVE_LEVEL1' AND `deleted` = 0 LIMIT 1);
SET @process_file_approve_level2_permission_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_APPROVE_LEVEL2' AND `deleted` = 0 LIMIT 1);
SET @process_file_approve_level3_permission_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_APPROVE_LEVEL3' AND `deleted` = 0 LIMIT 1);
SET @process_file_view_permission_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_VIEW' AND `deleted` = 0 LIMIT 1);
SET @process_file_download_permission_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_DOWNLOAD' AND `deleted` = 0 LIMIT 1);
SET @process_file_edit_permission_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_EDIT' AND `deleted` = 0 LIMIT 1);
SET @process_file_invalidate_permission_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_INVALIDATE' AND `deleted` = 0 LIMIT 1);
SET @process_file_pending_approval_permission_id = (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'PROCESS_FILE_PENDING_APPROVAL' AND `deleted` = 0 LIMIT 1);

-- 注塑组长权限：菜单、上传、提交审批、查看、下载、修改
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @injection_leader_role_id, @process_file_menu_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @injection_leader_role_id AND `permission_id` = @process_file_menu_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @injection_leader_role_id, @process_file_upload_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @injection_leader_role_id AND `permission_id` = @process_file_upload_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @injection_leader_role_id, @process_file_submit_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @injection_leader_role_id AND `permission_id` = @process_file_submit_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @injection_leader_role_id, @process_file_view_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @injection_leader_role_id AND `permission_id` = @process_file_view_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @injection_leader_role_id, @process_file_download_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @injection_leader_role_id AND `permission_id` = @process_file_download_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @injection_leader_role_id, @process_file_edit_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @injection_leader_role_id AND `permission_id` = @process_file_edit_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @injection_leader_role_id, @process_file_invalidate_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @injection_leader_role_id AND `permission_id` = @process_file_invalidate_permission_id);

-- 车间主任权限：菜单、审核（第一级）、查看、下载、待我审批
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @workshop_director_role_id, @process_file_menu_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @workshop_director_role_id AND `permission_id` = @process_file_menu_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @workshop_director_role_id, @process_file_approve_level1_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @workshop_director_role_id AND `permission_id` = @process_file_approve_level1_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @workshop_director_role_id, @process_file_view_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @workshop_director_role_id AND `permission_id` = @process_file_view_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @workshop_director_role_id, @process_file_download_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @workshop_director_role_id AND `permission_id` = @process_file_download_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @workshop_director_role_id, @process_file_pending_approval_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @workshop_director_role_id AND `permission_id` = @process_file_pending_approval_permission_id);

-- 注塑部经理权限：菜单、会签（第二级）、查看、下载、待我审批
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @injection_manager_role_id, @process_file_menu_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @injection_manager_role_id AND `permission_id` = @process_file_menu_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @injection_manager_role_id, @process_file_approve_level2_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @injection_manager_role_id AND `permission_id` = @process_file_approve_level2_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @injection_manager_role_id, @process_file_view_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @injection_manager_role_id AND `permission_id` = @process_file_view_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @injection_manager_role_id, @process_file_download_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @injection_manager_role_id AND `permission_id` = @process_file_download_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @injection_manager_role_id, @process_file_pending_approval_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @injection_manager_role_id AND `permission_id` = @process_file_pending_approval_permission_id);

-- 生产技术部经理权限：菜单、批准（第三级）、查看、下载、待我审批、作废
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @production_tech_manager_role_id, @process_file_menu_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @production_tech_manager_role_id AND `permission_id` = @process_file_menu_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @production_tech_manager_role_id, @process_file_approve_level3_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @production_tech_manager_role_id AND `permission_id` = @process_file_approve_level3_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @production_tech_manager_role_id, @process_file_view_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @production_tech_manager_role_id AND `permission_id` = @process_file_view_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @production_tech_manager_role_id, @process_file_download_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @production_tech_manager_role_id AND `permission_id` = @process_file_download_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @production_tech_manager_role_id, @process_file_pending_approval_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @production_tech_manager_role_id AND `permission_id` = @process_file_pending_approval_permission_id);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT @production_tech_manager_role_id, @process_file_invalidate_permission_id, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_permission` WHERE `role_id` = @production_tech_manager_role_id AND `permission_id` = @process_file_invalidate_permission_id);

-- ============================================
-- 4. 验证数据
-- ============================================

-- 查询创建的角色
SELECT '创建的角色：' AS info;
SELECT `id`, `role_code`, `role_name`, `description`, `status` 
FROM `sys_role` 
WHERE `role_code` IN ('INJECTION_LEADER', 'WORKSHOP_DIRECTOR', 'INJECTION_MANAGER', 'PRODUCTION_TECH_MANAGER')
AND `deleted` = 0
ORDER BY `id`;

-- 查询创建的权限
SELECT '创建的权限：' AS info;
SELECT `id`, `permission_code`, `permission_name`, `permission_type`, `parent_id`
FROM `sys_permission` 
WHERE `permission_code` LIKE 'PROCESS_FILE%'
AND `deleted` = 0
ORDER BY `permission_type`, `sort_order`;

-- 查询角色权限关联
SELECT '角色权限关联：' AS info;
SELECT r.`role_code`, r.`role_name`, p.`permission_code`, p.`permission_name`
FROM `sys_role` r
INNER JOIN `sys_role_permission` rp ON r.`id` = rp.`role_id`
INNER JOIN `sys_permission` p ON rp.`permission_id` = p.`id`
WHERE r.`role_code` IN ('INJECTION_LEADER', 'WORKSHOP_DIRECTOR', 'INJECTION_MANAGER', 'PRODUCTION_TECH_MANAGER')
AND r.`deleted` = 0 AND p.`deleted` = 0
ORDER BY r.`role_code`, p.`permission_type`, p.`sort_order`;
