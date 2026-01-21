package com.zssystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("employee")
public class Employee {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("employee_no")
    private String employeeNo;

    private String name;

    private Integer gender; // 0-女，1-男

    private Integer age;

    private String phone;

    private String email;

    @TableField("department_id")
    private Long departmentId;

    private String position;

    @TableField("entry_date")
    private LocalDate entryDate;

    private Integer status; // 0-离职，1-在职

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
