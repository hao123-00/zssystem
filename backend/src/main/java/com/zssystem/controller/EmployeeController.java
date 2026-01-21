package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.EmployeeQueryDTO;
import com.zssystem.dto.EmployeeSaveDTO;
import com.zssystem.service.EmployeeService;
import com.zssystem.vo.EmployeeVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
@Validated
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/list")
    public Result<PageResult<EmployeeVO>> getEmployeeList(@Validated EmployeeQueryDTO queryDTO) {
        IPage<EmployeeVO> page = employeeService.getEmployeeList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<EmployeeVO> getEmployeeById(@PathVariable Long id) {
        EmployeeVO employee = employeeService.getEmployeeById(id);
        return Result.success(employee);
    }

    @PostMapping
    public Result<Void> createEmployee(@Valid @RequestBody EmployeeSaveDTO saveDTO) {
        employeeService.createEmployee(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeSaveDTO saveDTO) {
        employeeService.updateEmployee(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return Result.success();
    }
}
