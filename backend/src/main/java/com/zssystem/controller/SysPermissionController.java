package com.zssystem.controller;

import com.zssystem.common.Result;
import com.zssystem.dto.PermissionSaveDTO;
import com.zssystem.service.SysPermissionService;
import com.zssystem.vo.PermissionTreeVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permission")
public class SysPermissionController {

    @Autowired
    private SysPermissionService permissionService;

    @GetMapping("/tree")
    public Result<List<PermissionTreeVO>> tree(@RequestParam(required = false) Integer type) {
        return Result.success(permissionService.getPermissionTree(type));
    }

    @GetMapping("/{id}")
    public Result<PermissionTreeVO> getById(@PathVariable Long id) {
        return Result.success(permissionService.getById(id));
    }

    @GetMapping("/role/{roleId}")
    public Result<List<PermissionTreeVO>> getPermissionsByRoleId(@PathVariable Long roleId) {
        return Result.success(permissionService.getPermissionsByRoleId(roleId));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody PermissionSaveDTO saveDTO) {
        permissionService.createPermission(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PermissionSaveDTO saveDTO) {
        permissionService.updatePermission(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return Result.success();
    }
}

