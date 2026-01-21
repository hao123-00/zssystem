package com.zssystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.dto.ProductionOrderQueryDTO;
import com.zssystem.dto.ProductionOrderSaveDTO;
import com.zssystem.vo.ProductionOrderVO;

public interface ProductionOrderService {
    IPage<ProductionOrderVO> getOrderList(ProductionOrderQueryDTO queryDTO);
    ProductionOrderVO getOrderById(Long id);
    void createOrder(ProductionOrderSaveDTO saveDTO);
    void updateOrder(Long id, ProductionOrderSaveDTO saveDTO);
    void deleteOrder(Long id);
    void updateCompletedQuantity(Long orderId, Integer quantity);
}
