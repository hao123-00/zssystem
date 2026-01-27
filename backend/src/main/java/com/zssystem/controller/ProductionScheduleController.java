package com.zssystem.controller;

import com.zssystem.common.Result;
import com.zssystem.dto.ProductionScheduleQueryDTO;
import com.zssystem.service.ProductionScheduleService;
import com.zssystem.util.ExcelUtil;
import com.zssystem.vo.ProductionScheduleVO;
import com.zssystem.vo.ProductionScheduleDetailVO;
import com.zssystem.vo.excel.ProductionScheduleExportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/production/schedule")
@Validated
public class ProductionScheduleController {

    @Autowired
    private ProductionScheduleService scheduleService;

    @PostMapping("/generate")
    public Result<ProductionScheduleVO> generateSchedule(
            @RequestParam String machineNo,
            @RequestParam LocalDate startDate) {
        ProductionScheduleVO vo = scheduleService.generateSchedule(machineNo, startDate);
        return Result.success(vo);
    }

    @GetMapping("/list")
    public Result<List<ProductionScheduleVO>> getScheduleList(@Validated ProductionScheduleQueryDTO queryDTO) {
        List<ProductionScheduleVO> list = scheduleService.getScheduleList(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/machine/{machineNo}")
    public Result<ProductionScheduleVO> getScheduleByMachineNo(
            @PathVariable String machineNo,
            @RequestParam(required = false) LocalDate startDate) {
        ProductionScheduleVO vo = scheduleService.getScheduleByMachineNo(machineNo, startDate);
        return Result.success(vo);
    }

    @GetMapping("/export")
    public void exportSchedule(@Validated ProductionScheduleQueryDTO queryDTO) {
        if (queryDTO.getStartDate() == null) {
            throw new RuntimeException("排程开始日期不能为空");
        }
        List<ProductionScheduleExportVO> exportData = scheduleService.getExportData(queryDTO);
        String fileName = ExcelUtil.generateFileName("生产管理_生产计划排程");
        // 使用从前端传递的日期列表设置Excel列标题和数据
        ExcelUtil.exportExcel(exportData, fileName, "生产计划排程", ProductionScheduleExportVO.class,
                new com.zssystem.util.ScheduleExcelWriteHandler(queryDTO.getDateList()),
                new com.zssystem.util.ScheduleDataWriteHandler(queryDTO.getDateList()));
    }

    @DeleteMapping("/machine/{machineNo}")
    public Result<Void> deleteScheduleByMachineNo(@PathVariable String machineNo) {
        scheduleService.deleteScheduleByMachineNo(machineNo);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteScheduleById(@PathVariable Long id) {
        scheduleService.deleteScheduleById(id);
        return Result.success();
    }

    @GetMapping("/detail/list")
    public Result<List<ProductionScheduleDetailVO>> getScheduleDetailList(@Validated ProductionScheduleQueryDTO queryDTO) {
        List<ProductionScheduleDetailVO> list = scheduleService.getScheduleDetailList(queryDTO);
        return Result.success(list);
    }
}
