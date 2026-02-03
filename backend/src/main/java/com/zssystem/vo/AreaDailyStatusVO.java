package com.zssystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AreaDailyStatusVO {
    private LocalDate statusDate;
    private List<AreaTaskVO> areas;
}
