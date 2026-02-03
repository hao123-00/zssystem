package com.zssystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Email(message = "请输入正确的邮箱格式")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    private String employeeNo;
    private String team;
    private String position;
    private String category;
    private java.time.LocalDate hireDate;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String password;

    private Long[] roleIds;
}

