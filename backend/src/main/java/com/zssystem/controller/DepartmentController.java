package com.zssystem.controller;

import com.zssystem.common.Result;
import com.zssystem.dto.DepartmentSaveDTO;
import com.zssystem.service.DepartmentService;
import com.zssystem.vo.DepartmentTreeVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/tree")
    public Result<List<DepartmentTreeVO>> getDepartmentTree() {
        return Result.success(departmentService.getDepartmentTree());
    }

    @GetMapping("/{id}")
    public Result<DepartmentTreeVO> getDepartmentById(@PathVariable Long id) {
        return Result.success(departmentService.getDepartmentById(id));
    }

    @PostMapping
    public Result<Void> createDepartment(@Valid @RequestBody DepartmentSaveDTO saveDTO) {
        departmentService.createDepartment(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentSaveDTO saveDTO) {
        departmentService.updateDepartment(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.success();
    }
}
