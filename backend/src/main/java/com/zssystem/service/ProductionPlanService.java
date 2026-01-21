package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.ProductionPlanQueryDTO;
import com.zssystem.dto.ProductionPlanSaveDTO;
import com.zssystem.vo.ProductionPlanVO;

public interface ProductionPlanService {
    IPage<ProductionPlanVO> getPlanList(ProductionPlanQueryDTO queryDTO);
    ProductionPlanVO getPlanById(Long id);
    void createPlan(ProductionPlanSaveDTO saveDTO);
    void updatePlan(Long id, ProductionPlanSaveDTO saveDTO);
    void deletePlan(Long id);
    void updateCompletedQuantity(Long planId, Integer quantity);
}
