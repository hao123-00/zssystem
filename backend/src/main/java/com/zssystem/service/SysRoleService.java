package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.RoleQueryDTO;
import com.zssystem.dto.RoleSaveDTO;
import com.zssystem.vo.RoleVO;

import java.util.List;

public interface SysRoleService {
    IPage<RoleVO> getRoleList(RoleQueryDTO queryDTO);
    List<RoleVO> getAllRoles();
    RoleVO getById(Long id);
    void createRole(RoleSaveDTO saveDTO);
    void updateRole(Long id, RoleSaveDTO saveDTO);
    void deleteRole(Long id);
    void assignPermissions(Long roleId, List<Long> permissionIds);
    List<Long> getRolePermissions(Long roleId);
}

