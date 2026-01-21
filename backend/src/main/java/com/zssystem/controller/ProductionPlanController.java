package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.ProductionPlanQueryDTO;
import com.zssystem.dto.ProductionPlanSaveDTO;
import com.zssystem.service.ProductionPlanService;
import com.zssystem.vo.ProductionPlanVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production/plan")
@Validated
public class ProductionPlanController {

    @Autowired
    private ProductionPlanService planService;

    @GetMapping("/list")
    public Result<PageResult<ProductionPlanVO>> getPlanList(@Validated ProductionPlanQueryDTO queryDTO) {
        IPage<ProductionPlanVO> page = planService.getPlanList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<ProductionPlanVO> getPlanById(@PathVariable Long id) {
        ProductionPlanVO plan = planService.getPlanById(id);
        return Result.success(plan);
    }

    @PostMapping
    public Result<Void> createPlan(@Valid @RequestBody ProductionPlanSaveDTO saveDTO) {
        planService.createPlan(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updatePlan(@PathVariable Long id, @Valid @RequestBody ProductionPlanSaveDTO saveDTO) {
        planService.updatePlan(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        return Result.success();
    }
}
