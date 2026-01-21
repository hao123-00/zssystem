package com.zssystem.service;

import com.zssystem.dto.LoginDTO;
import com.zssystem.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO dto);
    void logout();
    LoginVO refreshToken(String refreshToken);
}
