package com.zssystem.service;

import com.zssystem.dto.PermissionSaveDTO;
import com.zssystem.vo.PermissionTreeVO;

import java.util.List;

public interface SysPermissionService {
    List<PermissionTreeVO> getPermissionTree(Integer type);
    PermissionTreeVO getById(Long id);
    void createPermission(PermissionSaveDTO saveDTO);
    void updatePermission(Long id, PermissionSaveDTO saveDTO);
    void deletePermission(Long id);
    List<PermissionTreeVO> getPermissionsByRoleId(Long roleId);
}

