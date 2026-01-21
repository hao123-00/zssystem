package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.UserCreateDTO;
import com.zssystem.dto.UserQueryDTO;
import com.zssystem.dto.UserUpdateDTO;
import com.zssystem.entity.SysRole;
import com.zssystem.entity.SysUser;
import com.zssystem.entity.SysUserRole;
import com.zssystem.mapper.SysRoleMapper;
import com.zssystem.mapper.SysUserMapper;
import com.zssystem.mapper.SysUserRoleMapper;
import com.zssystem.service.SysUserService;
import com.zssystem.util.BeanUtil;
import com.zssystem.vo.RoleVO;
import com.zssystem.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public IPage<UserVO> getUserList(UserQueryDTO queryDTO) {
        Page<SysUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getUsername() != null && !queryDTO.getUsername().isBlank(), SysUser::getUsername, queryDTO.getUsername())
                .like(queryDTO.getRealName() != null && !queryDTO.getRealName().isBlank(), SysUser::getName, queryDTO.getRealName())
                .eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus())
                .orderByDesc(SysUser::getCreateTime);
        IPage<SysUser> userPage = userMapper.selectPage(page, wrapper);
        return userPage.convert(user -> {
            UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
            // 填充角色信息
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
            if (roleIds != null && !roleIds.isEmpty()) {
                List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
                vo.setRoles(roles.stream()
                        .map(role -> BeanUtil.copyProperties(role, RoleVO.class))
                        .collect(Collectors.toList()));
            }
            return vo;
        });
    }

    @Override
    public UserVO getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
        // 填充角色信息
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(id);
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
            vo.setRoles(roles.stream()
                    .map(role -> BeanUtil.copyProperties(role, RoleVO.class))
                    .collect(Collectors.toList()));
        }
        return vo;
    }

    @Override
    @Transactional
    public void createUser(UserCreateDTO createDTO) {
        SysUser exist = userMapper.selectByUsername(createDTO.getUsername());
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(createDTO.getUsername());
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setName(createDTO.getRealName());
        user.setEmail(createDTO.getEmail());
        user.setPhone(createDTO.getPhone());
        user.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : 1);
        userMapper.insert(user);

        // 保存用户角色关联
        if (createDTO.getRoleIds() != null && createDTO.getRoleIds().length > 0) {
            for (Long roleId : createDTO.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateDTO updateDTO) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 更新用户信息
        user.setName(updateDTO.getRealName());
        user.setEmail(updateDTO.getEmail());
        user.setPhone(updateDTO.getPhone());
        user.setStatus(updateDTO.getStatus());
        
        // 如果提供了密码，则更新密码
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }
        
        userMapper.updateById(user);

        // 更新用户角色关联
        if (updateDTO.getRoleIds() != null) {
            // 删除原有角色关联
            userRoleMapper.delete(
                    new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getUserId, id)
            );

            // 添加新角色关联
            if (updateDTO.getRoleIds().length > 0) {
                for (Long roleId : updateDTO.getRoleIds()) {
                    SysUserRole userRole = new SysUserRole();
                    userRole.setUserId(id);
                    userRole.setRoleId(roleId);
                    userRoleMapper.insert(userRole);
                }
            }
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && auth.getName().equals(user.getUsername())) {
            throw new RuntimeException("不能删除自己");
        }
        userMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void enableUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(1);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void disableUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(0);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
    }
}

