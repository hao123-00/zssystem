package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.EmployeeQueryDTO;
import com.zssystem.dto.EmployeeSaveDTO;
import com.zssystem.entity.Department;
import com.zssystem.entity.Employee;
import com.zssystem.mapper.DepartmentMapper;
import com.zssystem.mapper.EmployeeMapper;
import com.zssystem.service.EmployeeService;
import com.zssystem.util.BeanUtil;
import com.zssystem.vo.EmployeeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public IPage<EmployeeVO> getEmployeeList(EmployeeQueryDTO queryDTO) {
        Page<Employee> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getName() != null && !queryDTO.getName().isBlank(), Employee::getName, queryDTO.getName())
                .like(queryDTO.getEmployeeNo() != null && !queryDTO.getEmployeeNo().isBlank(), Employee::getEmployeeNo, queryDTO.getEmployeeNo())
                .eq(queryDTO.getDepartmentId() != null, Employee::getDepartmentId, queryDTO.getDepartmentId())
                .eq(queryDTO.getStatus() != null, Employee::getStatus, queryDTO.getStatus())
                .orderByDesc(Employee::getCreateTime);

        IPage<Employee> employeePage = employeeMapper.selectPage(page, wrapper);
        return employeePage.convert(employee -> {
            EmployeeVO vo = BeanUtil.copyProperties(employee, EmployeeVO.class);
            // 填充部门名称
            if (employee.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(employee.getDepartmentId());
                if (department != null) {
                    vo.setDepartmentName(department.getDeptName());
                }
            }
            return vo;
        });
    }

    @Override
    public EmployeeVO getEmployeeById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }
        EmployeeVO vo = BeanUtil.copyProperties(employee, EmployeeVO.class);
        // 填充部门名称
        if (employee.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(employee.getDepartmentId());
            if (department != null) {
                vo.setDepartmentName(department.getDeptName());
            }
        }
        return vo;
    }

    @Override
    @Transactional
    public void createEmployee(EmployeeSaveDTO saveDTO) {
        // 校验工号唯一性
        Employee exist = employeeMapper.selectByEmployeeNo(saveDTO.getEmployeeNo());
        if (exist != null) {
            throw new RuntimeException("工号已存在");
        }
        
        // 校验部门是否存在
        if (saveDTO.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(saveDTO.getDepartmentId());
            if (department == null) {
                throw new RuntimeException("部门不存在");
            }
        }

        Employee employee = BeanUtil.copyProperties(saveDTO, Employee.class);
        employeeMapper.insert(employee);
    }

    @Override
    @Transactional
    public void updateEmployee(Long id, EmployeeSaveDTO saveDTO) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }

        // 校验工号唯一性
        if (!employee.getEmployeeNo().equals(saveDTO.getEmployeeNo())) {
            Employee exist = employeeMapper.selectByEmployeeNo(saveDTO.getEmployeeNo());
            if (exist != null && !exist.getId().equals(id)) {
                throw new RuntimeException("工号已存在");
            }
        }

        // 校验部门是否存在
        if (saveDTO.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(saveDTO.getDepartmentId());
            if (department == null) {
                throw new RuntimeException("部门不存在");
            }
        }

        BeanUtil.copyProperties(saveDTO, employee, "id", "createTime", "updateTime", "deleted");
        employeeMapper.updateById(employee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }
        employeeMapper.deleteById(id);
    }
}
