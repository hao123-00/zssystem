package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zssystem.dto.PermissionSaveDTO;
import com.zssystem.entity.SysPermission;
import com.zssystem.entity.SysRolePermission;
import com.zssystem.mapper.SysPermissionMapper;
import com.zssystem.mapper.SysRolePermissionMapper;
import com.zssystem.service.SysPermissionService;
import com.zssystem.util.BeanUtil;
import com.zssystem.vo.PermissionTreeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysPermissionServiceImpl implements SysPermissionService {

    @Autowired
    private SysPermissionMapper permissionMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Override
    public List<PermissionTreeVO> getPermissionTree(Integer type) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(type != null, SysPermission::getPermissionType, type)
                .orderByAsc(SysPermission::getSortOrder);
        List<SysPermission> permissions = permissionMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        Map<Long, PermissionTreeVO> map = permissions.stream()
                .collect(Collectors.toMap(SysPermission::getId, p -> BeanUtil.copyProperties(p, PermissionTreeVO.class)));

        List<PermissionTreeVO> roots = new ArrayList<>();
        for (PermissionTreeVO node : map.values()) {
            if (node.getParentId() == null || node.getParentId() == 0) {
                roots.add(node);
            } else {
                PermissionTreeVO parent = map.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        sortTree(roots);
        return roots;
    }

    private void sortTree(List<PermissionTreeVO> list) {
        list.sort(Comparator.comparing(p -> Optional.ofNullable(p.getSortOrder()).orElse(0)));
        for (PermissionTreeVO child : list) {
            if (!CollectionUtils.isEmpty(child.getChildren())) {
                sortTree(child.getChildren());
            }
        }
    }

    @Override
    public PermissionTreeVO getById(Long id) {
        SysPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }
        return BeanUtil.copyProperties(permission, PermissionTreeVO.class);
    }

    @Override
    @Transactional
    public void createPermission(PermissionSaveDTO saveDTO) {
        // 校验权限编码唯一性
        SysPermission exist = permissionMapper.selectByPermissionCode(saveDTO.getPermissionCode());
        if (exist != null) {
            throw new RuntimeException("权限编码已存在");
        }
        
        SysPermission permission = BeanUtil.copyProperties(saveDTO, SysPermission.class);
        if (permission.getParentId() == null) {
            permission.setParentId(0L);
        }
        permissionMapper.insert(permission);
    }

    @Override
    @Transactional
    public void updatePermission(Long id, PermissionSaveDTO saveDTO) {
        SysPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        // 校验权限编码唯一性
        if (!permission.getPermissionCode().equals(saveDTO.getPermissionCode())) {
            SysPermission exist = permissionMapper.selectByPermissionCode(saveDTO.getPermissionCode());
            if (exist != null && !exist.getId().equals(id)) {
                throw new RuntimeException("权限编码已存在");
            }
        }
        
        BeanUtil.copyProperties(saveDTO, permission, "id", "createTime", "updateTime", "deleted");
        if (permission.getParentId() == null) {
            permission.setParentId(0L);
        }
        permissionMapper.updateById(permission);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        SysPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        // 检查是否有子权限
        Long childCount = permissionMapper.selectCount(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new RuntimeException("存在子权限，无法删除");
        }
        
        // 检查是否被角色使用
        Long count = rolePermissionMapper.selectCount(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getPermissionId, id));
        if (count != null && count > 0) {
            throw new RuntimeException("权限已被角色使用，无法删除");
        }
        permissionMapper.deleteById(id);
    }

    @Override
    public List<PermissionTreeVO> getPermissionsByRoleId(Long roleId) {
        List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
        if (CollectionUtils.isEmpty(permissionIds)) {
            return Collections.emptyList();
        }
        
        List<SysPermission> permissions = permissionMapper.selectBatchIds(permissionIds);
        if (CollectionUtils.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        
        Map<Long, PermissionTreeVO> map = permissions.stream()
                .collect(Collectors.toMap(SysPermission::getId, p -> BeanUtil.copyProperties(p, PermissionTreeVO.class)));
        
        List<PermissionTreeVO> roots = new ArrayList<>();
        for (PermissionTreeVO node : map.values()) {
            if (node.getParentId() == null || node.getParentId() == 0) {
                roots.add(node);
            } else {
                PermissionTreeVO parent = map.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        sortTree(roots);
        return roots;
    }
}

