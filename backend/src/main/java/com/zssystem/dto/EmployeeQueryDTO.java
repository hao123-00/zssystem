package com.zssystem.dto;

import lombok.Data;

@Data
public class EmployeeQueryDTO {
    private String name;
    private String employeeNo;
    private Long departmentId;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
