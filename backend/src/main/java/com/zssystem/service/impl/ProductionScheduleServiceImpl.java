package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zssystem.dto.ProductionScheduleQueryDTO;
import com.zssystem.entity.Equipment;
import com.zssystem.entity.ProductionOrder;
import com.zssystem.entity.ProductionOrderProduct;
import com.zssystem.entity.ProductionSchedule;
import com.zssystem.mapper.EquipmentMapper;
import com.zssystem.mapper.ProductionOrderMapper;
import com.zssystem.mapper.ProductionOrderProductMapper;
import com.zssystem.mapper.ProductionScheduleMapper;
import com.zssystem.service.ProductionScheduleService;
import com.zssystem.util.BeanUtil;
import com.zssystem.vo.ProductionScheduleVO;
import com.zssystem.vo.ScheduleDayVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductionScheduleServiceImpl implements ProductionScheduleService {

    @Autowired
    private ProductionScheduleMapper scheduleMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private ProductionOrderMapper orderMapper;

    @Autowired
    private ProductionOrderProductMapper orderProductMapper;

    @Override
    @Transactional
    public ProductionScheduleVO generateSchedule(String machineNo, LocalDate startDate) {
        // 1. 查询机台号对应的设备信息（可选）
        Equipment equipment = equipmentMapper.selectOne(
            new LambdaQueryWrapper<Equipment>()
                .eq(Equipment::getMachineNo, machineNo)
        );
        
        // 2. 查询该机台号的所有生产订单（查询待排程和排程中的订单）
        List<ProductionOrder> orders = orderMapper.selectList(
            new LambdaQueryWrapper<ProductionOrder>()
                .eq(ProductionOrder::getMachineNo, machineNo)
                .in(ProductionOrder::getStatus, 0, 1) // 查询待排程和排程中的订单
                .orderByDesc(ProductionOrder::getCreateTime)
        );
        
        if (orders.isEmpty()) {
            throw new RuntimeException("该机台号未配置生产订单");
        }
        
        // 3. 查询所有订单的产品（按订单创建时间和产品排序）
        List<ProductionOrderProduct> allProducts = new ArrayList<>();
        for (ProductionOrder order : orders) {
            List<ProductionOrderProduct> products = orderProductMapper.selectList(
                new LambdaQueryWrapper<ProductionOrderProduct>()
                    .eq(ProductionOrderProduct::getOrderId, order.getId())
                    .orderByAsc(ProductionOrderProduct::getSortOrder)
            );
            allProducts.addAll(products);
        }
        
        if (allProducts.isEmpty()) {
            throw new RuntimeException("该机台号的订单未配置产品");
        }
        
        // 4. 删除该机台号旧的排程记录
        scheduleMapper.delete(new LambdaQueryWrapper<ProductionSchedule>()
            .eq(ProductionSchedule::getMachineNo, machineNo));
        
        // 5. 生成排程（避开星期天）
        List<ScheduleDayVO> scheduleDays = new ArrayList<>();
        int currentProductIndex = 0;
        int currentProductDays = 0; // 当前产品已生产天数
        ProductionOrderProduct currentProduct = allProducts.get(currentProductIndex);
        int remainingQuantity = currentProduct.getOrderQuantity();
        int dayNumber = 0; // 排程天数（排除星期天）
        LocalDate currentDate = startDate;
        
        // 持续生成排程，直到所有产品完成或达到最大天数限制
        int maxDays = 100; // 最大排程天数限制，避免无限循环
        while (dayNumber < maxDays && currentProductIndex < allProducts.size()) {
            // 判断是否为星期天，如果是则跳过
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SUNDAY) {
                // 记录跳过的星期天（不增加dayNumber，因为星期天不计入排程天数）
                ProductionSchedule skipSchedule = new ProductionSchedule();
                skipSchedule.setMachineNo(machineNo);
                skipSchedule.setScheduleDate(currentDate);
                skipSchedule.setIsSunday(1);
                skipSchedule.setDayNumber(0); // 星期天不计入排程天数，设置为0或-1表示跳过
                // 设置必需的字段，避免数据库约束错误
                skipSchedule.setProductName("-"); // 星期天不生产，设置为占位符
                skipSchedule.setProductionQuantity(0);
                skipSchedule.setDailyCapacity(0);
                skipSchedule.setRemainingQuantity(0);
                // 如果有当前产品，使用当前产品的订单ID，否则使用第一个产品的订单ID
                if (currentProduct != null) {
                    skipSchedule.setOrderId(currentProduct.getOrderId());
                } else if (!allProducts.isEmpty()) {
                    skipSchedule.setOrderId(allProducts.get(0).getOrderId());
                }
                if (equipment != null) {
                    skipSchedule.setEquipmentId(equipment.getId());
                    skipSchedule.setEquipmentNo(equipment.getEquipmentNo());
                }
                scheduleMapper.insert(skipSchedule);
                
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
            dayNumber++;
            
            // 计算剩余数量：剩余数量 = 订单数量 - 产能 × 已生产天数
            remainingQuantity = currentProduct.getOrderQuantity() - 
                               (currentProduct.getDailyCapacity() * currentProductDays);
            
            // 如果剩余数量 <= 0，切换到下一个产品
            if (remainingQuantity <= 0) {
                if (currentProductIndex < allProducts.size() - 1) {
                    currentProductIndex++;
                    currentProduct = allProducts.get(currentProductIndex);
                    currentProductDays = 0;
                    remainingQuantity = currentProduct.getOrderQuantity();
                } else {
                    // 所有产品已完成，退出循环
                    break;
                }
            }
            
            // 创建排程记录
            ProductionSchedule schedule = new ProductionSchedule();
            schedule.setMachineNo(machineNo);
            if (equipment != null) {
                schedule.setEquipmentId(equipment.getId());
                schedule.setEquipmentNo(equipment.getEquipmentNo());
            }
            schedule.setScheduleDate(currentDate);
            schedule.setDayNumber(dayNumber);
            schedule.setProductCode(currentProduct.getProductCode());
            schedule.setProductName(currentProduct.getProductName());
            schedule.setProductionQuantity(currentProduct.getDailyCapacity());
            schedule.setDailyCapacity(currentProduct.getDailyCapacity());
            schedule.setRemainingQuantity(remainingQuantity);
            schedule.setOrderId(currentProduct.getOrderId());
            schedule.setIsSunday(0);
            scheduleMapper.insert(schedule);
            
            // 添加到VO
            ScheduleDayVO dayVO = new ScheduleDayVO();
            dayVO.setDayNumber(dayNumber);
            dayVO.setScheduleDate(currentDate);
            dayVO.setProductName(currentProduct.getProductName());
            dayVO.setProductionQuantity(currentProduct.getDailyCapacity());
            dayVO.setDailyCapacity(currentProduct.getDailyCapacity());
            dayVO.setRemainingQuantity(remainingQuantity);
            scheduleDays.add(dayVO);
            
            currentProductDays++;
            currentDate = currentDate.plusDays(1);
        }
        
        // 6. 判断是否能在指定时间内完成所有产品的生产目标
        boolean canComplete = currentProductIndex >= allProducts.size() - 1 && 
                             (currentProductIndex == allProducts.size() - 1 ? remainingQuantity <= 0 : true);
        
        // 6. 构建返回VO
        ProductionScheduleVO vo = new ProductionScheduleVO();
        vo.setMachineNo(machineNo);
        if (equipment != null) {
            vo.setEquipmentId(equipment.getId());
            vo.setEquipmentNo(equipment.getEquipmentNo());
            vo.setEquipmentName(equipment.getEquipmentName());
            vo.setGroupName(equipment.getGroupName());
        }
        vo.setScheduleStartDate(startDate);
        vo.setScheduleDays(scheduleDays);
        vo.setCanCompleteTarget(canComplete);
        
        return vo;
    }

    @Override
    public List<ProductionScheduleVO> getScheduleList(ProductionScheduleQueryDTO queryDTO) {
        // 查询符合条件的机台号
        LambdaQueryWrapper<ProductionOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(queryDTO.getMachineNo() != null && !queryDTO.getMachineNo().isBlank(),
                        ProductionOrder::getMachineNo, queryDTO.getMachineNo());
        
        List<ProductionOrder> orders = orderMapper.selectList(orderWrapper);
        
        // 获取唯一的机台号列表
        List<String> machineNos = orders.stream()
            .map(ProductionOrder::getMachineNo)
            .distinct()
            .collect(Collectors.toList());
        
        LocalDate startDate = queryDTO.getStartDate() != null ? queryDTO.getStartDate() : LocalDate.now();
        
        return machineNos.stream().map(machineNo -> {
            return getScheduleByMachineNo(machineNo, startDate);
        }).collect(Collectors.toList());
    }

    @Override
    public ProductionScheduleVO getScheduleByMachineNo(String machineNo, LocalDate startDate) {
        // 查询机台号对应的设备信息（可选）
        Equipment equipment = equipmentMapper.selectOne(
            new LambdaQueryWrapper<Equipment>()
                .eq(Equipment::getMachineNo, machineNo)
        );
        
        // 查询该机台号的排程（排除星期天）
        List<ProductionSchedule> schedules = scheduleMapper.selectList(
            new LambdaQueryWrapper<ProductionSchedule>()
                .eq(ProductionSchedule::getMachineNo, machineNo)
                .eq(ProductionSchedule::getIsSunday, 0) // 排除星期天
                .ge(startDate != null, ProductionSchedule::getScheduleDate, startDate)
                .orderByAsc(ProductionSchedule::getDayNumber)
        );
        
        if (schedules.isEmpty()) {
            // 如果没有排程，返回空VO
            ProductionScheduleVO vo = new ProductionScheduleVO();
            vo.setMachineNo(machineNo);
            if (equipment != null) {
                vo.setEquipmentId(equipment.getId());
                vo.setEquipmentNo(equipment.getEquipmentNo());
                vo.setEquipmentName(equipment.getEquipmentName());
                vo.setGroupName(equipment.getGroupName());
            }
            vo.setScheduleStartDate(startDate);
            vo.setScheduleDays(new ArrayList<>());
            vo.setCanCompleteTarget(false);
            return vo;
        }
        
        // 转换为VO
        List<ScheduleDayVO> scheduleDays = schedules.stream().map(schedule -> {
            ScheduleDayVO dayVO = new ScheduleDayVO();
            dayVO.setDayNumber(schedule.getDayNumber());
            dayVO.setScheduleDate(schedule.getScheduleDate());
            dayVO.setProductName(schedule.getProductName());
            dayVO.setProductionQuantity(schedule.getProductionQuantity());
            dayVO.setDailyCapacity(schedule.getDailyCapacity());
            dayVO.setRemainingQuantity(schedule.getRemainingQuantity());
            return dayVO;
        }).collect(Collectors.toList());
        
        // 判断是否完成目标（检查所有订单是否都能完成）
        boolean canComplete = true;
        if (!scheduleDays.isEmpty()) {
            // 检查最后一个排程的剩余数量
            ScheduleDayVO lastDay = scheduleDays.get(scheduleDays.size() - 1);
            if (lastDay.getRemainingQuantity() > 0) {
                canComplete = false;
            }
        }
        
        ProductionScheduleVO vo = new ProductionScheduleVO();
        vo.setMachineNo(machineNo);
        if (equipment != null) {
            vo.setEquipmentId(equipment.getId());
            vo.setEquipmentNo(equipment.getEquipmentNo());
            vo.setEquipmentName(equipment.getEquipmentName());
            vo.setGroupName(equipment.getGroupName());
        }
        vo.setScheduleStartDate(startDate);
        vo.setScheduleDays(scheduleDays);
        vo.setCanCompleteTarget(canComplete);
        
        return vo;
    }
}
