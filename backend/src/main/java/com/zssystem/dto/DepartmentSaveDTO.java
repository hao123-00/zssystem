package com.zssystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DepartmentSaveDTO {
    private Long id;

    private Long parentId = 0L;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    private String deptCode;

    private String leader;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    @Email(message = "请输入正确的邮箱格式")
    private String email;

    private Integer sortOrder = 0;

    private Integer status = 1;
}
