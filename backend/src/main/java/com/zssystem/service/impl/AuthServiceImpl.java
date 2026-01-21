package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zssystem.dto.LoginDTO;
import com.zssystem.entity.SysUser;
import com.zssystem.mapper.SysUserMapper;
import com.zssystem.service.AuthService;
import com.zssystem.util.JwtUtil;
import com.zssystem.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 查询用户
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername())
                .eq(SysUser::getDeleted, 0)
        );
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 2. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 3. 检查状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 4. 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        
        // 5. 返回结果
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setRefreshToken(refreshToken);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getName());
        
        return vo;
    }
    
    @Override
    public void logout() {
        // JWT无状态，客户端删除Token即可
        // 如需服务端控制，可使用Redis存储黑名单
    }
    
    @Override
    public LoginVO refreshToken(String refreshToken) {
        // 验证refreshToken并生成新token
        Long userId = jwtUtil.getUserIdFromRefreshToken(refreshToken);
        SysUser user = userMapper.selectById(userId);
        
        if (user == null || user.getStatus() == 0) {
            throw new RuntimeException("Token无效");
        }
        
        String newToken = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        LoginVO vo = new LoginVO();
        vo.setToken(newToken);
        vo.setRefreshToken(refreshToken);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getName());
        
        return vo;
    }
}
