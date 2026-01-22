package com.zssystem.controller;

import com.zssystem.common.Result;
import com.zssystem.dto.EquipmentProductSaveDTO;
import com.zssystem.service.EquipmentProductService;
import com.zssystem.vo.EquipmentProductVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment/product")
@Validated
public class EquipmentProductController {

    @Autowired
    private EquipmentProductService productService;

    @GetMapping("/equipment/{equipmentId}")
    public Result<List<EquipmentProductVO>> getProductListByEquipmentId(@PathVariable Long equipmentId) {
        List<EquipmentProductVO> list = productService.getProductListByEquipmentId(equipmentId);
        return Result.success(list);
    }

    @GetMapping("/product/{productCode}")
    public Result<List<EquipmentProductVO>> getEquipmentListByProductCode(@PathVariable String productCode) {
        List<EquipmentProductVO> list = productService.getEquipmentListByProductCode(productCode);
        return Result.success(list);
    }

    @PostMapping
    public Result<Void> bindProduct(@Valid @RequestBody EquipmentProductSaveDTO saveDTO) {
        productService.bindProduct(saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> unbindProduct(@PathVariable Long id) {
        productService.unbindProduct(id);
        return Result.success();
    }
}
