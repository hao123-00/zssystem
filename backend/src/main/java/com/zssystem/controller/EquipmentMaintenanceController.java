package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.EquipmentMaintenanceQueryDTO;
import com.zssystem.dto.EquipmentMaintenanceSaveDTO;
import com.zssystem.service.EquipmentMaintenanceService;
import com.zssystem.vo.EquipmentMaintenanceVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment/maintenance")
@Validated
public class EquipmentMaintenanceController {

    @Autowired
    private EquipmentMaintenanceService maintenanceService;

    @GetMapping("/list")
    public Result<PageResult<EquipmentMaintenanceVO>> getMaintenanceList(@Validated EquipmentMaintenanceQueryDTO queryDTO) {
        IPage<EquipmentMaintenanceVO> page = maintenanceService.getMaintenanceList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<EquipmentMaintenanceVO> getMaintenanceById(@PathVariable Long id) {
        EquipmentMaintenanceVO vo = maintenanceService.getMaintenanceById(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Void> createMaintenance(@Valid @RequestBody EquipmentMaintenanceSaveDTO saveDTO) {
        maintenanceService.createMaintenance(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateMaintenance(@PathVariable Long id, @Valid @RequestBody EquipmentMaintenanceSaveDTO saveDTO) {
        maintenanceService.updateMaintenance(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMaintenance(@PathVariable Long id) {
        maintenanceService.deleteMaintenance(id);
        return Result.success();
    }
}
