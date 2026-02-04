package com.zssystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

@Data
public class Site5sAreaSaveDTO {
    private Long id;

    @NotBlank(message = "区域名称不能为空")
    @Size(max = 100)
    private String areaName;

    @NotBlank(message = "检查项目不能为空")
    @Size(max = 100)
    private String checkItem;

    @NotNull(message = "负责人1不能为空")
    private Long responsibleUserId;

    @NotNull(message = "负责人2不能为空")
    private Long responsibleUserId2;

    @NotNull(message = "早间拍照时间不能为空")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime morningPhotoTime;

    @NotNull(message = "晚间拍照时间不能为空")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime eveningPhotoTime;

    private Integer sortOrder = 0;

    @NotNull(message = "状态不能为空")
    private Integer status = 1;

    @Size(max = 500)
    private String remark;
}
