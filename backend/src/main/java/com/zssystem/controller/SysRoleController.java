package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.RoleQueryDTO;
import com.zssystem.dto.RoleSaveDTO;
import com.zssystem.service.SysRoleService;
import com.zssystem.vo.RoleVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
@Validated
public class SysRoleController {

    @Autowired
    private SysRoleService roleService;

    @GetMapping("/list")
    public Result<PageResult<RoleVO>> getRoleList(@Validated RoleQueryDTO queryDTO) {
        IPage<RoleVO> page = roleService.getRoleList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<RoleVO> getRoleById(@PathVariable Long id) {
        RoleVO role = roleService.getById(id);
        return Result.success(role);
    }

    @GetMapping("/all")
    public Result<List<RoleVO>> getAllRoles() {
        List<RoleVO> list = roleService.getAllRoles();
        return Result.success(list);
    }

    @PostMapping
    public Result<Void> createRole(@Valid @RequestBody RoleSaveDTO saveDTO) {
        roleService.createRole(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id,
                                   @Valid @RequestBody RoleSaveDTO saveDTO) {
        roleService.updateRole(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id,
                                          @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }

    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        List<Long> permissionIds = roleService.getRolePermissions(id);
        return Result.success(permissionIds);
    }
}

