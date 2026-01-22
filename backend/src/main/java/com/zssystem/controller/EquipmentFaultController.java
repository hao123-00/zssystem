package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.EquipmentFaultQueryDTO;
import com.zssystem.dto.EquipmentFaultSaveDTO;
import com.zssystem.service.EquipmentFaultService;
import com.zssystem.vo.EquipmentFaultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment/fault")
@Validated
public class EquipmentFaultController {

    @Autowired
    private EquipmentFaultService faultService;

    @GetMapping("/list")
    public Result<PageResult<EquipmentFaultVO>> getFaultList(@Validated EquipmentFaultQueryDTO queryDTO) {
        IPage<EquipmentFaultVO> page = faultService.getFaultList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<EquipmentFaultVO> getFaultById(@PathVariable Long id) {
        EquipmentFaultVO vo = faultService.getFaultById(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Void> createFault(@Valid @RequestBody EquipmentFaultSaveDTO saveDTO) {
        faultService.createFault(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateFault(@PathVariable Long id, @Valid @RequestBody EquipmentFaultSaveDTO saveDTO) {
        faultService.updateFault(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFault(@PathVariable Long id) {
        faultService.deleteFault(id);
        return Result.success();
    }
}
