package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.ProductionOrderSaveDTO;
import com.zssystem.dto.ProductionOrderQueryDTO;
import com.zssystem.entity.ProductionOrder;
import com.zssystem.entity.ProductionRecord;
import com.zssystem.mapper.ProductionOrderMapper;
import com.zssystem.mapper.ProductionRecordMapper;
import com.zssystem.service.ProductionOrderService;
import com.zssystem.util.BeanUtil;
import com.zssystem.util.CodeGenerator;
import com.zssystem.vo.ProductionOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ProductionOrderServiceImpl implements ProductionOrderService {

    @Autowired
    private ProductionOrderMapper orderMapper;

    @Autowired
    private ProductionRecordMapper recordMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<ProductionOrderVO> getOrderList(ProductionOrderQueryDTO queryDTO) {
        Page<ProductionOrder> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ProductionOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getOrderNo() != null && !queryDTO.getOrderNo().isBlank(), 
                        ProductionOrder::getOrderNo, queryDTO.getOrderNo())
                .like(queryDTO.getCustomerName() != null && !queryDTO.getCustomerName().isBlank(), 
                        ProductionOrder::getCustomerName, queryDTO.getCustomerName())
                .like(queryDTO.getProductName() != null && !queryDTO.getProductName().isBlank(), 
                        ProductionOrder::getProductName, queryDTO.getProductName())
                .eq(queryDTO.getStatus() != null, ProductionOrder::getStatus, queryDTO.getStatus())
                .orderByDesc(ProductionOrder::getCreateTime);

        IPage<ProductionOrder> orderPage = orderMapper.selectPage(page, wrapper);
        return orderPage.convert(order -> BeanUtil.copyProperties(order, ProductionOrderVO.class));
    }

    @Override
    public ProductionOrderVO getOrderById(Long id) {
        ProductionOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return BeanUtil.copyProperties(order, ProductionOrderVO.class);
    }

    @Override
    @Transactional
    public void createOrder(ProductionOrderSaveDTO saveDTO) {
        // 生成订单编号
        String datePrefix = "ORDER" + LocalDate.now().format(DATE_FORMATTER);
        Integer count = orderMapper.countByPrefix(datePrefix);
        String orderNo = CodeGenerator.generateOrderNo(count + 1);

        ProductionOrder order = BeanUtil.copyProperties(saveDTO, ProductionOrder.class);
        order.setOrderNo(orderNo);
        order.setCompletedQuantity(0);
        if (order.getStatus() == null) {
            order.setStatus(0); // 默认待生产
        }
        orderMapper.insert(order);
    }

    @Override
    @Transactional
    public void updateOrder(Long id, ProductionOrderSaveDTO saveDTO) {
        ProductionOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查是否有生产记录
        if (saveDTO.getStatus() != null && saveDTO.getStatus() == 3) { // 取消订单
            Long recordCount = recordMapper.selectCount(new LambdaQueryWrapper<ProductionRecord>()
                    .eq(ProductionRecord::getOrderId, id));
            if (recordCount != null && recordCount > 0) {
                throw new RuntimeException("订单已有生产记录，无法取消");
            }
        }

        BeanUtil.copyProperties(saveDTO, order, "id", "orderNo", "completedQuantity", "createTime", "updateTime", "deleted");
        
        // 如果完成数量达到订单数量，自动更新状态为已完成
        if (order.getCompletedQuantity() != null && order.getQuantity() != null 
                && order.getCompletedQuantity() >= order.getQuantity()) {
            order.setStatus(2); // 已完成
        }
        
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        ProductionOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查是否有生产记录
        Long recordCount = recordMapper.selectCount(new LambdaQueryWrapper<ProductionRecord>()
                .eq(ProductionRecord::getOrderId, id));
        if (recordCount != null && recordCount > 0) {
            throw new RuntimeException("订单已有生产记录，无法删除");
        }

        orderMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateCompletedQuantity(Long orderId, Integer quantity) {
        ProductionOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            return;
        }

        Integer newCompletedQuantity = (order.getCompletedQuantity() == null ? 0 : order.getCompletedQuantity()) + quantity;
        order.setCompletedQuantity(newCompletedQuantity);

        // 如果完成数量达到订单数量，自动更新状态为已完成
        if (newCompletedQuantity >= order.getQuantity()) {
            order.setStatus(2); // 已完成
        } else if (order.getStatus() == 0 && newCompletedQuantity > 0) {
            // 如果从待生产状态开始有产量，更新为生产中
            order.setStatus(1); // 生产中
        }

        orderMapper.updateById(order);
    }
}
