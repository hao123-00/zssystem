package com.zssystem.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Site5sCheckQueryDTO {
    private LocalDate checkDate;
    private String checkArea;
    private String checkerName;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
