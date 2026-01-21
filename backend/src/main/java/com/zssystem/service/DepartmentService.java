package com.zssystem.service;

import com.zssystem.dto.DepartmentSaveDTO;
import com.zssystem.vo.DepartmentTreeVO;

import java.util.List;

public interface DepartmentService {
    List<DepartmentTreeVO> getDepartmentTree();
    DepartmentTreeVO getDepartmentById(Long id);
    void createDepartment(DepartmentSaveDTO saveDTO);
    void updateDepartment(Long id, DepartmentSaveDTO saveDTO);
    void deleteDepartment(Long id);
}
