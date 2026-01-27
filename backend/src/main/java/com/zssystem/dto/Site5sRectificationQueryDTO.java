package com.zssystem.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Site5sRectificationQueryDTO {
    private String taskNo;
    private String area;
    private String department;
    private String responsiblePerson;
    private Integer status; // 0-待整改，1-整改中，2-待验证，3-已完成
    private LocalDate deadline;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
