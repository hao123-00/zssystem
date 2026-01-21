package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeVO {
    private Long id;
    private String employeeNo;
    private String name;
    private Integer gender;
    private Integer age;
    private String phone;
    private String email;
    private Long departmentId;
    private String departmentName;
    private String position;
    private LocalDate entryDate;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
