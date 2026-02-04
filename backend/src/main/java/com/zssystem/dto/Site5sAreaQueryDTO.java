package com.zssystem.dto;

import lombok.Data;

@Data
public class Site5sAreaQueryDTO {
    private String areaCode;
    private String areaName;
    private String checkItem;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
