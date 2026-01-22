package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.EquipmentCheckQueryDTO;
import com.zssystem.dto.EquipmentCheckSaveDTO;
import com.zssystem.service.EquipmentCheckService;
import com.zssystem.vo.EquipmentCheckVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment/check")
@Validated
public class EquipmentCheckController {

    @Autowired
    private EquipmentCheckService checkService;

    @GetMapping("/list")
    public Result<PageResult<EquipmentCheckVO>> getCheckList(@Validated EquipmentCheckQueryDTO queryDTO) {
        IPage<EquipmentCheckVO> page = checkService.getCheckList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<EquipmentCheckVO> getCheckById(@PathVariable Long id) {
        EquipmentCheckVO vo = checkService.getCheckById(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Void> saveCheck(@Valid @RequestBody EquipmentCheckSaveDTO saveDTO) {
        checkService.saveCheck(saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCheck(@PathVariable Long id) {
        checkService.deleteCheck(id);
        return Result.success();
    }
}
