package com.zssystem.controller;

import com.zssystem.common.Result;
import com.zssystem.dto.EquipmentProductionProductSaveDTO;
import com.zssystem.service.EquipmentProductionProductService;
import com.zssystem.vo.EquipmentProductionProductVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/production/equipment-product")
@Validated
public class EquipmentProductionProductController {

    @Autowired
    private EquipmentProductionProductService service;

    @GetMapping("/equipment/{equipmentId}")
    public Result<List<EquipmentProductionProductVO>> getProductListByEquipmentId(@PathVariable Long equipmentId) {
        List<EquipmentProductionProductVO> list = service.getProductListByEquipmentId(equipmentId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<EquipmentProductionProductVO> getById(@PathVariable Long id) {
        EquipmentProductionProductVO vo = service.getById(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody EquipmentProductionProductSaveDTO saveDTO) {
        service.create(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody EquipmentProductionProductSaveDTO saveDTO) {
        service.update(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }
}
