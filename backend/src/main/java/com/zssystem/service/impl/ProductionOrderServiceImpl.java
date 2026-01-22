package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.ProductionOrderSaveDTO;
import com.zssystem.dto.ProductionOrderQueryDTO;
import com.zssystem.entity.Equipment;
import com.zssystem.entity.ProductionOrder;
import com.zssystem.entity.ProductionOrderProduct;
import com.zssystem.mapper.EquipmentMapper;
import com.zssystem.mapper.ProductionOrderMapper;
import com.zssystem.mapper.ProductionOrderProductMapper;
import com.zssystem.service.ProductionOrderService;
import com.zssystem.util.BeanUtil;
import com.zssystem.util.CodeGenerator;
import com.zssystem.vo.ProductionOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProductionOrderServiceImpl implements ProductionOrderService {

    @Autowired
    private ProductionOrderMapper orderMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private ProductionOrderProductMapper orderProductMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put(0, "待排程");
        STATUS_MAP.put(1, "排程中");
        STATUS_MAP.put(2, "已完成");
    }

    @Override
    public IPage<ProductionOrderVO> getOrderList(ProductionOrderQueryDTO queryDTO) {
        Page<ProductionOrder> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ProductionOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getOrderNo() != null && !queryDTO.getOrderNo().isBlank(), 
                        ProductionOrder::getOrderNo, queryDTO.getOrderNo())
                .like(queryDTO.getMachineNo() != null && !queryDTO.getMachineNo().isBlank(), 
                        ProductionOrder::getMachineNo, queryDTO.getMachineNo())
                .eq(queryDTO.getStatus() != null, ProductionOrder::getStatus, queryDTO.getStatus())
                .orderByDesc(ProductionOrder::getCreateTime);

        // 如果按产品名称查询，需要关联订单产品表
        if (queryDTO.getProductName() != null && !queryDTO.getProductName().isBlank()) {
            // 先查询包含该产品名称的订单ID
            LambdaQueryWrapper<ProductionOrderProduct> productWrapper = new LambdaQueryWrapper<>();
            productWrapper.like(ProductionOrderProduct::getProductName, queryDTO.getProductName());
            java.util.List<ProductionOrderProduct> products = orderProductMapper.selectList(productWrapper);
            java.util.Set<Long> orderIds = products.stream()
                    .map(ProductionOrderProduct::getOrderId)
                    .collect(java.util.stream.Collectors.toSet());
            if (orderIds.isEmpty()) {
                // 如果没有匹配的产品，返回空结果
                return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            }
            wrapper.in(ProductionOrder::getId, orderIds);
        }

        IPage<ProductionOrder> orderPage = orderMapper.selectPage(page, wrapper);
        return orderPage.convert(order -> {
            ProductionOrderVO vo = BeanUtil.copyProperties(order, ProductionOrderVO.class);
            vo.setStatusText(STATUS_MAP.getOrDefault(order.getStatus(), "未知"));
            
            // 填充设备信息
            if (order.getEquipmentId() != null) {
                Equipment equipment = equipmentMapper.selectById(order.getEquipmentId());
                if (equipment != null) {
                    vo.setEquipmentNo(equipment.getEquipmentNo());
                }
            }
            
            // 填充产品列表
            LambdaQueryWrapper<ProductionOrderProduct> productWrapper = new LambdaQueryWrapper<>();
            productWrapper.eq(ProductionOrderProduct::getOrderId, order.getId())
                    .orderByAsc(ProductionOrderProduct::getSortOrder);
            java.util.List<ProductionOrderProduct> products = orderProductMapper.selectList(productWrapper);
            vo.setProducts(products.stream().map(p -> {
                ProductionOrderVO.ProductInfo info = new ProductionOrderVO.ProductInfo();
                info.setProductName(p.getProductName());
                info.setProductCode(p.getProductCode());
                info.setOrderQuantity(p.getOrderQuantity());
                info.setDailyCapacity(p.getDailyCapacity());
                info.setSortOrder(p.getSortOrder());
                return info;
            }).collect(java.util.stream.Collectors.toList()));
            
            return vo;
        });
    }

    @Override
    public ProductionOrderVO getOrderById(Long id) {
        ProductionOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        ProductionOrderVO vo = BeanUtil.copyProperties(order, ProductionOrderVO.class);
        vo.setStatusText(STATUS_MAP.getOrDefault(order.getStatus(), "未知"));
        
        // 填充设备信息
        if (order.getEquipmentId() != null) {
            Equipment equipment = equipmentMapper.selectById(order.getEquipmentId());
            if (equipment != null) {
                vo.setEquipmentNo(equipment.getEquipmentNo());
            }
        }
        
        // 填充产品列表
        LambdaQueryWrapper<ProductionOrderProduct> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(ProductionOrderProduct::getOrderId, order.getId())
                .orderByAsc(ProductionOrderProduct::getSortOrder);
        java.util.List<ProductionOrderProduct> products = orderProductMapper.selectList(productWrapper);
        vo.setProducts(products.stream().map(p -> {
            ProductionOrderVO.ProductInfo info = new ProductionOrderVO.ProductInfo();
            info.setProductName(p.getProductName());
            info.setProductCode(p.getProductCode());
            info.setOrderQuantity(p.getOrderQuantity());
            info.setDailyCapacity(p.getDailyCapacity());
            info.setSortOrder(p.getSortOrder());
            return info;
        }).collect(java.util.stream.Collectors.toList()));
        
        return vo;
    }

    @Override
    @Transactional
    public void createOrder(ProductionOrderSaveDTO saveDTO) {
        // 根据机台号查询设备
        Equipment equipment = equipmentMapper.selectOne(
            new LambdaQueryWrapper<Equipment>()
                .eq(Equipment::getMachineNo, saveDTO.getMachineNo())
        );
        
        // 生成订单编号（避免并发重复）
        String datePrefix = "ORDER" + LocalDate.now().format(DATE_FORMATTER);
        String maxOrderNo = orderMapper.getMaxOrderNoByPrefix(datePrefix);
        int sequence = 1;
        if (maxOrderNo != null && maxOrderNo.startsWith(datePrefix)) {
            // 提取序号：ORDER20260122 + 001 -> 提取 001
            String seqStr = maxOrderNo.substring(datePrefix.length());
            try {
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        String orderNo = CodeGenerator.generateOrderNo(sequence);
        
        // 检查订单编号是否已存在（双重检查，防止并发）
        int retryCount = 0;
        while (orderMapper.selectByOrderNo(orderNo) != null && retryCount < 10) {
            sequence++;
            orderNo = CodeGenerator.generateOrderNo(sequence);
            retryCount++;
        }
        if (retryCount >= 10) {
            throw new RuntimeException("生成订单编号失败，请稍后重试");
        }

        ProductionOrder order = new ProductionOrder();
        order.setOrderNo(orderNo);
        order.setMachineNo(saveDTO.getMachineNo());
        order.setRemark(saveDTO.getRemark());
        
        // 设置设备ID
        if (equipment != null) {
            order.setEquipmentId(equipment.getId());
        }
        
        if (order.getStatus() == null) {
            order.setStatus(0); // 默认待排程
        }
        orderMapper.insert(order);
        
        // 保存产品列表
        if (saveDTO.getProducts() != null && !saveDTO.getProducts().isEmpty()) {
            for (int i = 0; i < saveDTO.getProducts().size(); i++) {
                ProductionOrderSaveDTO.ProductInfo productInfo = saveDTO.getProducts().get(i);
                ProductionOrderProduct product = new ProductionOrderProduct();
                product.setOrderId(order.getId());
                product.setOrderNo(orderNo);
                product.setProductName(productInfo.getProductName());
                product.setProductCode(productInfo.getProductCode());
                product.setOrderQuantity(productInfo.getOrderQuantity());
                product.setDailyCapacity(productInfo.getDailyCapacity());
                product.setSortOrder(i + 1); // 1, 2, 3
                orderProductMapper.insert(product);
            }
        }
    }

    @Override
    @Transactional
    public void updateOrder(Long id, ProductionOrderSaveDTO saveDTO) {
        ProductionOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 更新订单基本信息
        order.setMachineNo(saveDTO.getMachineNo());
        order.setRemark(saveDTO.getRemark());
        
        // 如果机台号有变化，更新设备ID
        if (saveDTO.getMachineNo() != null && !saveDTO.getMachineNo().equals(order.getMachineNo())) {
            Equipment equipment = equipmentMapper.selectOne(
                new LambdaQueryWrapper<Equipment>()
                    .eq(Equipment::getMachineNo, saveDTO.getMachineNo())
            );
            if (equipment != null) {
                order.setEquipmentId(equipment.getId());
            }
        }
        
        orderMapper.updateById(order);
        
        // 删除旧的产品记录
        LambdaQueryWrapper<ProductionOrderProduct> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ProductionOrderProduct::getOrderId, id);
        orderProductMapper.delete(deleteWrapper);
        
        // 保存新的产品列表
        if (saveDTO.getProducts() != null && !saveDTO.getProducts().isEmpty()) {
            for (int i = 0; i < saveDTO.getProducts().size(); i++) {
                ProductionOrderSaveDTO.ProductInfo productInfo = saveDTO.getProducts().get(i);
                ProductionOrderProduct product = new ProductionOrderProduct();
                product.setOrderId(order.getId());
                product.setOrderNo(order.getOrderNo());
                product.setProductName(productInfo.getProductName());
                product.setProductCode(productInfo.getProductCode());
                product.setOrderQuantity(productInfo.getOrderQuantity());
                product.setDailyCapacity(productInfo.getDailyCapacity());
                product.setSortOrder(i + 1); // 1, 2, 3
                orderProductMapper.insert(product);
            }
        }
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        ProductionOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 删除关联的产品记录
        LambdaQueryWrapper<ProductionOrderProduct> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ProductionOrderProduct::getOrderId, id);
        orderProductMapper.delete(deleteWrapper);
        
        // 删除订单
        orderMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateCompletedQuantity(Long orderId, Integer quantity) {
        // 此方法在新需求中可能不再需要，但保留以保持接口兼容性
        ProductionOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            return;
        }

        // 根据实际生产记录更新订单状态
        // 这里可以根据生产记录来更新状态
        if (order.getStatus() == 0) {
            order.setStatus(1); // 排程中
        }
        
        orderMapper.updateById(order);
    }
}
