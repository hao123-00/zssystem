package com.zssystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zssystem.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    @Select("SELECT * FROM sys_permission WHERE permission_code = #{permissionCode} AND deleted = 0 LIMIT 1")
    SysPermission selectByPermissionCode(String permissionCode);
}

