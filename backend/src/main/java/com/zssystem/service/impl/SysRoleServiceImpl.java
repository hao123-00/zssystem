package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.RoleQueryDTO;
import com.zssystem.dto.RoleSaveDTO;
import com.zssystem.entity.SysRole;
import com.zssystem.entity.SysRolePermission;
import com.zssystem.entity.SysUserRole;
import com.zssystem.mapper.SysRoleMapper;
import com.zssystem.mapper.SysRolePermissionMapper;
import com.zssystem.mapper.SysUserRoleMapper;
import com.zssystem.service.SysRoleService;
import com.zssystem.util.BeanUtil;
import com.zssystem.vo.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Override
    public IPage<RoleVO> getRoleList(RoleQueryDTO queryDTO) {
        Page<SysRole> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getRoleName() != null && !queryDTO.getRoleName().isBlank(),
                        SysRole::getRoleName, queryDTO.getRoleName())
                .eq(queryDTO.getStatus() != null, SysRole::getStatus, queryDTO.getStatus())
                .orderByDesc(SysRole::getCreateTime);

        IPage<SysRole> rolePage = roleMapper.selectPage(page, wrapper);
        return rolePage.convert(role -> {
            RoleVO vo = BeanUtil.copyProperties(role, RoleVO.class);
            List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(role.getId());
            vo.setPermissionIds(permissionIds);
            return vo;
        });
    }

    @Override
    public List<RoleVO> getAllRoles() {
        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1));
        List<RoleVO> list = new ArrayList<>();
        for (SysRole role : roles) {
            RoleVO vo = BeanUtil.copyProperties(role, RoleVO.class);
            List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(role.getId());
            vo.setPermissionIds(permissionIds);
            list.add(vo);
        }
        return list;
    }

    @Override
    public RoleVO getById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        RoleVO vo = BeanUtil.copyProperties(role, RoleVO.class);
        List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(id);
        vo.setPermissionIds(permissionIds);
        return vo;
    }

    @Override
    @Transactional
    public void createRole(RoleSaveDTO saveDTO) {
        SysRole exist = roleMapper.selectByRoleCode(saveDTO.getRoleCode());
        if (exist != null) {
            throw new RuntimeException("角色编码已存在");
        }
        SysRole role = BeanUtil.copyProperties(saveDTO, SysRole.class);
        roleMapper.insert(role);
        
        // 分配权限
        if (!CollectionUtils.isEmpty(saveDTO.getPermissionIds())) {
            assignPermissions(role.getId(), saveDTO.getPermissionIds());
        }
    }

    @Override
    @Transactional
    public void updateRole(Long id, RoleSaveDTO saveDTO) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        if (!role.getRoleCode().equals(saveDTO.getRoleCode())) {
            SysRole exist = roleMapper.selectByRoleCode(saveDTO.getRoleCode());
            if (exist != null && !exist.getId().equals(id)) {
                throw new RuntimeException("角色编码已存在");
            }
        }
        BeanUtil.copyProperties(saveDTO, role, "id", "createTime", "updateTime", "deleted", "permissionIds");
        roleMapper.updateById(role);
        
        // 更新权限分配
        if (saveDTO.getPermissionIds() != null) {
            assignPermissions(id, saveDTO.getPermissionIds());
        }
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, id));
        if (count != null && count > 0) {
            throw new RuntimeException("角色已被用户使用，无法删除");
        }
        roleMapper.deleteById(id);
        rolePermissionMapper.deleteByRoleId(id);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        rolePermissionMapper.deleteByRoleId(roleId);
        if (!CollectionUtils.isEmpty(permissionIds)) {
            for (Long pid : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(pid);
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    public List<Long> getRolePermissions(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }
}

