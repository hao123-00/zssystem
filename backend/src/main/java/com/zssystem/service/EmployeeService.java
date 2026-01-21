package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.EmployeeQueryDTO;
import com.zssystem.dto.EmployeeSaveDTO;
import com.zssystem.vo.EmployeeVO;

public interface EmployeeService {
    IPage<EmployeeVO> getEmployeeList(EmployeeQueryDTO queryDTO);
    EmployeeVO getEmployeeById(Long id);
    void createEmployee(EmployeeSaveDTO saveDTO);
    void updateEmployee(Long id, EmployeeSaveDTO saveDTO);
    void deleteEmployee(Long id);
}
