package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zssystem.dto.DepartmentSaveDTO;
import com.zssystem.entity.Department;
import com.zssystem.entity.Employee;
import com.zssystem.mapper.DepartmentMapper;
import com.zssystem.mapper.EmployeeMapper;
import com.zssystem.service.DepartmentService;
import com.zssystem.util.BeanUtil;
import com.zssystem.vo.DepartmentTreeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public List<DepartmentTreeVO> getDepartmentTree() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Department::getSortOrder);
        List<Department> departments = departmentMapper.selectList(wrapper);
        
        if (CollectionUtils.isEmpty(departments)) {
            return Collections.emptyList();
        }
        
        Map<Long, DepartmentTreeVO> map = departments.stream()
                .collect(Collectors.toMap(Department::getId, d -> BeanUtil.copyProperties(d, DepartmentTreeVO.class)));

        List<DepartmentTreeVO> roots = new ArrayList<>();
        for (DepartmentTreeVO node : map.values()) {
            if (node.getParentId() == null || node.getParentId() == 0) {
                roots.add(node);
            } else {
                DepartmentTreeVO parent = map.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        sortTree(roots);
        return roots;
    }

    private void sortTree(List<DepartmentTreeVO> list) {
        list.sort(Comparator.comparing(d -> Optional.ofNullable(d.getSortOrder()).orElse(0)));
        for (DepartmentTreeVO child : list) {
            if (!CollectionUtils.isEmpty(child.getChildren())) {
                sortTree(child.getChildren());
            }
        }
    }

    @Override
    public DepartmentTreeVO getDepartmentById(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }
        return BeanUtil.copyProperties(department, DepartmentTreeVO.class);
    }

    @Override
    @Transactional
    public void createDepartment(DepartmentSaveDTO saveDTO) {
        // 校验父部门是否存在
        if (saveDTO.getParentId() != null && saveDTO.getParentId() != 0) {
            Department parent = departmentMapper.selectById(saveDTO.getParentId());
            if (parent == null) {
                throw new RuntimeException("父部门不存在");
            }
        }

        Department department = BeanUtil.copyProperties(saveDTO, Department.class);
        if (department.getParentId() == null) {
            department.setParentId(0L);
        }
        departmentMapper.insert(department);
    }

    @Override
    @Transactional
    public void updateDepartment(Long id, DepartmentSaveDTO saveDTO) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }

        // 不能将自己设置为父部门
        if (saveDTO.getParentId() != null && saveDTO.getParentId().equals(id)) {
            throw new RuntimeException("不能将自己设置为父部门");
        }

        // 校验父部门是否存在
        if (saveDTO.getParentId() != null && saveDTO.getParentId() != 0) {
            Department parent = departmentMapper.selectById(saveDTO.getParentId());
            if (parent == null) {
                throw new RuntimeException("父部门不存在");
            }
        }

        BeanUtil.copyProperties(saveDTO, department, "id", "createTime", "updateTime", "deleted");
        if (department.getParentId() == null) {
            department.setParentId(0L);
        }
        departmentMapper.updateById(department);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }

        // 检查是否有子部门
        Long childCount = departmentMapper.selectCount(new LambdaQueryWrapper<Department>()
                .eq(Department::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new RuntimeException("存在子部门，无法删除");
        }

        // 检查是否有关联员工
        Long employeeCount = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getDepartmentId, id));
        if (employeeCount != null && employeeCount > 0) {
            throw new RuntimeException("部门下存在员工，无法删除");
        }

        departmentMapper.deleteById(id);
    }
}
