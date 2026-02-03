package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String employeeNo;
    private String team;
    private String position;
    private String category;
    private LocalDate hireDate;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<RoleVO> roles;
}

