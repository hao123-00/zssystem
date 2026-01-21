package com.zssystem.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private String refreshToken;
    private Long userId;
    private String username;
    private String realName;
}
