package com.zssystem.util;

import com.zssystem.entity.SysRole;
import com.zssystem.entity.SysUser;
import com.zssystem.mapper.SysRoleMapper;
import com.zssystem.mapper.SysUserMapper;
import com.zssystem.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 安全工具类 - 获取当前登录用户信息
 */
@Component
public class SecurityUtil {

    private static SysUserMapper sysUserMapper;
    private static SysUserRoleMapper sysUserRoleMapper;
    private static SysRoleMapper sysRoleMapper;

    @Autowired
    public void setSysUserMapper(SysUserMapper sysUserMapper) {
        SecurityUtil.sysUserMapper = sysUserMapper;
    }

    @Autowired
    public void setSysUserRoleMapper(SysUserRoleMapper sysUserRoleMapper) {
        SecurityUtil.sysUserRoleMapper = sysUserRoleMapper;
    }

    @Autowired
    public void setSysRoleMapper(SysRoleMapper sysRoleMapper) {
        SecurityUtil.sysRoleMapper = sysRoleMapper;
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        SysUser user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 获取当前登录用户完整信息
     */
    public static SysUser getCurrentUser() {
        String username = getCurrentUsername();
        if (username != null && sysUserMapper != null) {
            return sysUserMapper.selectByUsername(username);
        }
        return null;
    }

    /**
     * 获取当前用户角色名称（第一个角色）
     * 从用户角色关联表查询用户的角色
     */
    public static String getCurrentUserRole() {
        SysUser user = getCurrentUser();
        if (user != null && sysUserRoleMapper != null && sysRoleMapper != null) {
            // 查询用户角色ID列表
            List<Long> roleIds = sysUserRoleMapper.selectRoleIdsByUserId(user.getId());
            if (roleIds != null && !roleIds.isEmpty()) {
                // 获取第一个角色
                SysRole role = sysRoleMapper.selectById(roleIds.get(0));
                if (role != null) {
                    return role.getRoleName();
                }
            }
        }
        return null;
    }

    /**
     * 获取当前用户角色代码（第一个角色）
     */
    public static String getCurrentUserRoleCode() {
        SysUser user = getCurrentUser();
        if (user != null && sysUserRoleMapper != null && sysRoleMapper != null) {
            // 查询用户角色ID列表
            List<Long> roleIds = sysUserRoleMapper.selectRoleIdsByUserId(user.getId());
            if (roleIds != null && !roleIds.isEmpty()) {
                // 获取第一个角色
                SysRole role = sysRoleMapper.selectById(roleIds.get(0));
                if (role != null) {
                    return role.getRoleCode();
                }
            }
        }
        return null;
    }

    /**
     * 获取当前用户所有角色代码列表
     */
    public static List<String> getCurrentUserRoleCodes() {
        SysUser user = getCurrentUser();
        if (user != null && sysUserRoleMapper != null && sysRoleMapper != null) {
            // 查询用户角色ID列表
            List<Long> roleIds = sysUserRoleMapper.selectRoleIdsByUserId(user.getId());
            if (roleIds != null && !roleIds.isEmpty()) {
                return roleIds.stream()
                        .map(roleId -> {
                            SysRole role = sysRoleMapper.selectById(roleId);
                            return role != null ? role.getRoleCode() : null;
                        })
                        .filter(code -> code != null)
                        .toList();
            }
        }
        return List.of();
    }

    /**
     * 检查当前用户是否有指定角色（通过角色名称）
     */
    public static boolean hasRole(String roleName) {
        String currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equals(roleName);
    }

    /**
     * 检查当前用户是否有指定角色（通过角色代码）
     */
    public static boolean hasRoleCode(String roleCode) {
        List<String> roleCodes = getCurrentUserRoleCodes();
        return roleCodes.contains(roleCode);
    }
}
