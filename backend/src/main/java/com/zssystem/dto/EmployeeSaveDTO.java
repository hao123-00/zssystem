package com.zssystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeSaveDTO {
    private Long id;

    @NotBlank(message = "工号不能为空")
    private String employeeNo;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private Integer gender; // 0-女，1-男

    private Integer age;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    @Email(message = "请输入正确的邮箱格式")
    private String email;

    @NotNull(message = "部门不能为空")
    private Long departmentId;

    private String position;

    private LocalDate entryDate;

    @NotNull(message = "状态不能为空")
    private Integer status; // 0-离职，1-在职
}
