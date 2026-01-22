package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.EquipmentQueryDTO;
import com.zssystem.dto.EquipmentSaveDTO;
import com.zssystem.service.EquipmentService;
import com.zssystem.vo.EquipmentVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment")
@Validated
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping("/list")
    public Result<PageResult<EquipmentVO>> getEquipmentList(@Validated EquipmentQueryDTO queryDTO) {
        IPage<EquipmentVO> page = equipmentService.getEquipmentList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<EquipmentVO> getEquipmentById(@PathVariable Long id) {
        EquipmentVO vo = equipmentService.getEquipmentById(id);
        return Result.success(vo);
    }

    @GetMapping("/no/{equipmentNo}")
    public Result<EquipmentVO> getEquipmentByNo(@PathVariable String equipmentNo) {
        EquipmentVO vo = equipmentService.getEquipmentByNo(equipmentNo);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Void> createEquipment(@Valid @RequestBody EquipmentSaveDTO saveDTO) {
        equipmentService.createEquipment(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateEquipment(@PathVariable Long id, @Valid @RequestBody EquipmentSaveDTO saveDTO) {
        equipmentService.updateEquipment(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return Result.success();
    }
}
