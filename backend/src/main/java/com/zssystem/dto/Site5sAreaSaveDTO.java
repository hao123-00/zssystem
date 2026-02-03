package com.zssystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class Site5sAreaSaveDTO {
    private Long id;

    @NotBlank(message = "区域编码不能为空")
    @Size(max = 50)
    private String areaCode;

    @NotBlank(message = "区域名称不能为空")
    @Size(max = 100)
    private String areaName;

    @NotBlank(message = "职能名称不能为空")
    @Size(max = 100)
    private String dutyName;

    private Integer sortOrder = 0;

    @NotNull(message = "状态不能为空")
    private Integer status = 1;

    @Size(max = 500)
    private String remark;

    @Valid
    @Size(min = 1, message = "至少配置1个拍照时段")
    private List<Site5sAreaScheduleDTO> schedules;
}
