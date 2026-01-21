package com.zssystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zssystem.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    @Select("SELECT * FROM employee WHERE employee_no = #{employeeNo} AND deleted = 0 LIMIT 1")
    Employee selectByEmployeeNo(String employeeNo);
}
