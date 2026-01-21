package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.ProductionOrderQueryDTO;
import com.zssystem.dto.ProductionOrderSaveDTO;
import com.zssystem.service.ProductionOrderService;
import com.zssystem.vo.ProductionOrderVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production/order")
@Validated
public class ProductionOrderController {

    @Autowired
    private ProductionOrderService orderService;

    @GetMapping("/list")
    public Result<PageResult<ProductionOrderVO>> getOrderList(@Validated ProductionOrderQueryDTO queryDTO) {
        IPage<ProductionOrderVO> page = orderService.getOrderList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<ProductionOrderVO> getOrderById(@PathVariable Long id) {
        ProductionOrderVO order = orderService.getOrderById(id);
        return Result.success(order);
    }

    @PostMapping
    public Result<Void> createOrder(@Valid @RequestBody ProductionOrderSaveDTO saveDTO) {
        orderService.createOrder(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateOrder(@PathVariable Long id, @Valid @RequestBody ProductionOrderSaveDTO saveDTO) {
        orderService.updateOrder(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return Result.success();
    }
}
