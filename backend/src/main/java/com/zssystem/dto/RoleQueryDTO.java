package com.zssystem.dto;

import lombok.Data;

@Data
public class RoleQueryDTO {
    private String roleName;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}

