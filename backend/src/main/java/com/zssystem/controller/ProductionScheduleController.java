package com.zssystem.controller;

import com.zssystem.common.Result;
import com.zssystem.dto.ProductionScheduleQueryDTO;
import com.zssystem.service.ProductionScheduleService;
import com.zssystem.vo.ProductionScheduleVO;
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
            @RequestParam(required = false) LocalDate startDate) {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
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
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        ProductionScheduleVO vo = scheduleService.getScheduleByMachineNo(machineNo, startDate);
        return Result.success(vo);
    }
}
