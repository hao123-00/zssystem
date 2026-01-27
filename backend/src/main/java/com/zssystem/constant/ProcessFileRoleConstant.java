package com.zssystem.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 工艺文件审批流程角色常量
 */
public class ProcessFileRoleConstant {
    
    /**
     * 角色代码常量
     */
    public static final String ROLE_CODE_INJECTION_LEADER = "INJECTION_LEADER"; // 注塑组长
    public static final String ROLE_CODE_WORKSHOP_DIRECTOR = "WORKSHOP_DIRECTOR"; // 车间主任
    public static final String ROLE_CODE_INJECTION_MANAGER = "INJECTION_MANAGER"; // 注塑部经理
    public static final String ROLE_CODE_PRODUCTION_TECH_MANAGER = "PRODUCTION_TECH_MANAGER"; // 生产技术部经理
    
    /**
     * 角色名称常量
     */
    public static final String ROLE_NAME_INJECTION_LEADER = "注塑组长";
    public static final String ROLE_NAME_WORKSHOP_DIRECTOR = "车间主任";
    public static final String ROLE_NAME_INJECTION_MANAGER = "注塑部经理";
    public static final String ROLE_NAME_PRODUCTION_TECH_MANAGER = "生产技术部经理";
    
    /**
     * 角色代码到角色名称的映射
     */
    private static final Map<String, String> ROLE_CODE_TO_NAME_MAP = new HashMap<>();
    
    /**
     * 角色名称到角色代码的映射
     */
    private static final Map<String, String> ROLE_NAME_TO_CODE_MAP = new HashMap<>();
    
    /**
     * 审批级别到角色代码的映射
     */
    private static final Map<Integer, String> APPROVAL_LEVEL_TO_ROLE_CODE_MAP = new HashMap<>();
    
    /**
     * 审批级别到角色名称的映射
     */
    private static final Map<Integer, String> APPROVAL_LEVEL_TO_ROLE_NAME_MAP = new HashMap<>();
    
    static {
        // 初始化角色代码到名称的映射
        ROLE_CODE_TO_NAME_MAP.put(ROLE_CODE_INJECTION_LEADER, ROLE_NAME_INJECTION_LEADER);
        ROLE_CODE_TO_NAME_MAP.put(ROLE_CODE_WORKSHOP_DIRECTOR, ROLE_NAME_WORKSHOP_DIRECTOR);
        ROLE_CODE_TO_NAME_MAP.put(ROLE_CODE_INJECTION_MANAGER, ROLE_NAME_INJECTION_MANAGER);
        ROLE_CODE_TO_NAME_MAP.put(ROLE_CODE_PRODUCTION_TECH_MANAGER, ROLE_NAME_PRODUCTION_TECH_MANAGER);
        
        // 初始化角色名称到代码的映射
        ROLE_NAME_TO_CODE_MAP.put(ROLE_NAME_INJECTION_LEADER, ROLE_CODE_INJECTION_LEADER);
        ROLE_NAME_TO_CODE_MAP.put(ROLE_NAME_WORKSHOP_DIRECTOR, ROLE_CODE_WORKSHOP_DIRECTOR);
        ROLE_NAME_TO_CODE_MAP.put(ROLE_NAME_INJECTION_MANAGER, ROLE_CODE_INJECTION_MANAGER);
        ROLE_NAME_TO_CODE_MAP.put(ROLE_NAME_PRODUCTION_TECH_MANAGER, ROLE_CODE_PRODUCTION_TECH_MANAGER);
        
        // 初始化审批级别到角色代码的映射
        APPROVAL_LEVEL_TO_ROLE_CODE_MAP.put(1, ROLE_CODE_WORKSHOP_DIRECTOR); // 车间主任审核
        APPROVAL_LEVEL_TO_ROLE_CODE_MAP.put(2, ROLE_CODE_INJECTION_MANAGER); // 注塑部经理会签
        APPROVAL_LEVEL_TO_ROLE_CODE_MAP.put(3, ROLE_CODE_PRODUCTION_TECH_MANAGER); // 生产技术部经理批准
        
        // 初始化审批级别到角色名称的映射
        APPROVAL_LEVEL_TO_ROLE_NAME_MAP.put(1, ROLE_NAME_WORKSHOP_DIRECTOR);
        APPROVAL_LEVEL_TO_ROLE_NAME_MAP.put(2, ROLE_NAME_INJECTION_MANAGER);
        APPROVAL_LEVEL_TO_ROLE_NAME_MAP.put(3, ROLE_NAME_PRODUCTION_TECH_MANAGER);
    }
    
    /**
     * 根据角色代码获取角色名称
     */
    public static String getRoleNameByCode(String roleCode) {
        return ROLE_CODE_TO_NAME_MAP.get(roleCode);
    }
    
    /**
     * 根据角色名称获取角色代码
     */
    public static String getRoleCodeByName(String roleName) {
        return ROLE_NAME_TO_CODE_MAP.get(roleName);
    }
    
    /**
     * 根据审批级别获取角色代码
     */
    public static String getRoleCodeByApprovalLevel(int approvalLevel) {
        return APPROVAL_LEVEL_TO_ROLE_CODE_MAP.get(approvalLevel);
    }
    
    /**
     * 根据审批级别获取角色名称
     */
    public static String getRoleNameByApprovalLevel(int approvalLevel) {
        return APPROVAL_LEVEL_TO_ROLE_NAME_MAP.get(approvalLevel);
    }
    
    /**
     * 检查角色代码是否为工艺文件审批相关角色
     */
    public static boolean isProcessFileRole(String roleCode) {
        return ROLE_CODE_TO_NAME_MAP.containsKey(roleCode);
    }
    
    /**
     * 检查角色名称是否为工艺文件审批相关角色
     */
    public static boolean isProcessFileRoleName(String roleName) {
        return ROLE_NAME_TO_CODE_MAP.containsKey(roleName);
    }
}
