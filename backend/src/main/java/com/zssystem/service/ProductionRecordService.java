package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.ProductionRecordQueryDTO;
import com.zssystem.dto.ProductionRecordSaveDTO;
import com.zssystem.vo.ProductionRecordVO;
import com.zssystem.vo.ProductionStatisticsVO;

import java.time.LocalDate;
import java.util.List;

public interface ProductionRecordService {
    IPage<ProductionRecordVO> getRecordList(ProductionRecordQueryDTO queryDTO);
    ProductionRecordVO getRecordById(Long id);
    void createRecord(ProductionRecordSaveDTO saveDTO);
    void updateRecord(Long id, ProductionRecordSaveDTO saveDTO);
    void deleteRecord(Long id);
    List<ProductionStatisticsVO> getStatistics(String dimension, LocalDate startDate, LocalDate endDate);
}
