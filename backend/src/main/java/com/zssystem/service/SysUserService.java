package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.UserCreateDTO;
import com.zssystem.dto.UserQueryDTO;
import com.zssystem.dto.UserUpdateDTO;
import com.zssystem.vo.UserVO;

public interface SysUserService {
    IPage<UserVO> getUserList(UserQueryDTO queryDTO);
    UserVO getUserById(Long id);
    void createUser(UserCreateDTO createDTO);
    void updateUser(Long id, UserUpdateDTO updateDTO);
    void deleteUser(Long id);
    void enableUser(Long id);
    void disableUser(Long id);
    void resetPassword(Long id);
}

