package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.ProductionRecordQueryDTO;
import com.zssystem.dto.ProductionRecordSaveDTO;
import com.zssystem.service.ProductionRecordService;
import com.zssystem.vo.ProductionRecordVO;
import com.zssystem.vo.ProductionStatisticsVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/production/record")
@Validated
public class ProductionRecordController {

    @Autowired
    private ProductionRecordService recordService;

    @GetMapping("/list")
    public Result<PageResult<ProductionRecordVO>> getRecordList(@Validated ProductionRecordQueryDTO queryDTO) {
        IPage<ProductionRecordVO> page = recordService.getRecordList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<ProductionRecordVO> getRecordById(@PathVariable Long id) {
        ProductionRecordVO record = recordService.getRecordById(id);
        return Result.success(record);
    }

    @PostMapping
    public Result<Void> createRecord(@Valid @RequestBody ProductionRecordSaveDTO saveDTO) {
        recordService.createRecord(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateRecord(@PathVariable Long id, @Valid @RequestBody ProductionRecordSaveDTO saveDTO) {
        recordService.updateRecord(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return Result.success();
    }

    @GetMapping("/statistics")
    public Result<List<ProductionStatisticsVO>> getStatistics(
            @RequestParam String dimension,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        List<ProductionStatisticsVO> statistics = recordService.getStatistics(dimension, startDate, endDate);
        return Result.success(statistics);
    }
}
