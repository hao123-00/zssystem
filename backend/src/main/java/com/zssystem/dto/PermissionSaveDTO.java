package com.zssystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionSaveDTO {
    private Long parentId;

    @NotBlank(message = "权限编码不能为空")
    private String permissionCode;

    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    @NotNull(message = "权限类型不能为空")
    private Integer permissionType; // 1-菜单，2-按钮

    private String path;
    private String component;
    private String icon;
    private Integer sortOrder;
    private Integer status;
}

