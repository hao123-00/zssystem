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
import com.zssystem.vo.ProductionScheduleDetailVO;
import com.zssystem.vo.ScheduleDayVO;
import com.zssystem.vo.excel.ProductionScheduleExportVO;
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
        
        // 4. 物理删除该机台号旧的排程记录
        // 由于唯一约束 uk_equipment_day (equipment_id, day_number) 不包含 deleted 字段，
        // 逻辑删除的记录仍然会被唯一约束检查，所以需要使用物理删除
        scheduleMapper.physicalDeleteByMachineNo(machineNo);
        
        // 5. 生成排程（避开星期天）
        List<ScheduleDayVO> scheduleDays = new ArrayList<>();
        int currentProductIndex = 0;
        int currentProductDays = 0; // 当前产品已生产天数
        ProductionOrderProduct currentProduct = allProducts.get(currentProductIndex);
        int remainingQuantity = currentProduct.getOrderQuantity();
        int dayNumber = 0; // 排程天数（排除星期天）
        LocalDate currentDate = startDate;
        boolean allProductsCompleted = false; // 标记所有产品是否完成
        
        // 持续生成排程，直到所有产品完成或达到最大天数限制
        // 一个月约30天，去掉星期日后约26天，设置为30天以确保覆盖一个月
        int maxDays = 30; // 最大排程天数限制（一个月，去掉星期日）
        while (dayNumber < maxDays && currentProductIndex < allProducts.size()) {
            // 先计算当前产品的剩余数量
            remainingQuantity = currentProduct.getOrderQuantity() - 
                               (currentProduct.getDailyCapacity() * currentProductDays);
            
            // 如果剩余数量 <= 0，不另算一天进行排程，直接切换到下一个产品
            if (remainingQuantity <= 0) {
                if (currentProductIndex < allProducts.size() - 1) {
                    // 切换到下一个产品
                    currentProductIndex++;
                    currentProduct = allProducts.get(currentProductIndex);
                    currentProductDays = 0;
                    remainingQuantity = currentProduct.getOrderQuantity();
                    // 继续循环，不增加dayNumber，不创建排程记录
                    continue;
                } else {
                    // 最后一个产品的剩余数量 <= 0，所有产品已完成
                    allProductsCompleted = true;
                    break;
                }
            }
            
            // 判断是否为星期天，如果是则跳过（不插入数据库记录，避免唯一约束冲突）
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SUNDAY) {
                // 星期天不计入排程天数，直接跳过，不插入数据库记录
                // 这样可以避免 uk_equipment_day (equipment_id, day_number) 唯一约束冲突
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
            dayNumber++;
            
            // 创建排程记录（剩余数量>0时才创建）
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
        boolean canComplete = false;
        if (allProductsCompleted) {
            // 所有产品都已完成，计划完成
            canComplete = true;
        } else {
            // 计算所有产品的总需求天数（排除星期天）
            int totalRequiredDays = 0;
            for (ProductionOrderProduct product : allProducts) {
                if (product.getDailyCapacity() > 0) {
                    // 向上取整：需要的天数 = (订单数量 + 产能 - 1) / 产能
                    int daysNeeded = (product.getOrderQuantity() + product.getDailyCapacity() - 1) / product.getDailyCapacity();
                    totalRequiredDays += daysNeeded;
                }
            }
            // 判断在最大天数内是否能完成
            canComplete = totalRequiredDays <= maxDays;
        }
        
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
        // 查询所有有排程记录的机台号（从排程表中查询，而不是从订单表）
        LambdaQueryWrapper<ProductionSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getMachineNo() != null && !queryDTO.getMachineNo().isBlank()) {
            scheduleWrapper.eq(ProductionSchedule::getMachineNo, queryDTO.getMachineNo());
        }
        if (queryDTO.getStartDate() != null) {
            scheduleWrapper.ge(ProductionSchedule::getScheduleDate, queryDTO.getStartDate());
        }
        
        // 获取唯一的机台号列表（从排程表中）
        List<ProductionSchedule> allSchedules = scheduleMapper.selectList(scheduleWrapper);
        List<String> machineNos = allSchedules.stream()
            .map(ProductionSchedule::getMachineNo)
            .distinct()
            .collect(Collectors.toList());
        
        if (machineNos.isEmpty()) {
            return new ArrayList<>();
        }
        
        LocalDate startDate = queryDTO.getStartDate();
        
        // 为每个机台号生成排程VO
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
        
        // 查询该机台号的排程（显示所有排程记录，按日期排序）
        LambdaQueryWrapper<ProductionSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(ProductionSchedule::getMachineNo, machineNo);
        if (startDate != null) {
            scheduleWrapper.ge(ProductionSchedule::getScheduleDate, startDate);
        }
        scheduleWrapper.orderByAsc(ProductionSchedule::getScheduleDate)
               .orderByAsc(ProductionSchedule::getDayNumber);
        
        List<ProductionSchedule> schedules = scheduleMapper.selectList(scheduleWrapper);
        
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
        
        // 判断是否完成目标
        boolean canComplete = false;
        if (!scheduleDays.isEmpty()) {
            // 检查最后一个排程的剩余数量
            ScheduleDayVO lastDay = scheduleDays.get(scheduleDays.size() - 1);
            // 如果最后一个排程的剩余数量 <= 0，说明所有产品都已完成
            if (lastDay.getRemainingQuantity() <= 0) {
                canComplete = true;
            } else {
                // 如果还有剩余数量，计算所有产品的总需求天数
                // 查询该机台号的所有订单产品
                List<ProductionOrder> orders = orderMapper.selectList(
                    new LambdaQueryWrapper<ProductionOrder>()
                        .eq(ProductionOrder::getMachineNo, machineNo)
                        .in(ProductionOrder::getStatus, 0, 1)
                );
                
                List<ProductionOrderProduct> allProducts = new ArrayList<>();
                for (ProductionOrder order : orders) {
                    List<ProductionOrderProduct> products = orderProductMapper.selectList(
                        new LambdaQueryWrapper<ProductionOrderProduct>()
                            .eq(ProductionOrderProduct::getOrderId, order.getId())
                            .orderByAsc(ProductionOrderProduct::getSortOrder)
                    );
                    allProducts.addAll(products);
                }
                
                // 计算总需求天数
                int totalRequiredDays = 0;
                for (ProductionOrderProduct product : allProducts) {
                    if (product.getDailyCapacity() > 0) {
                        int daysNeeded = (product.getOrderQuantity() + product.getDailyCapacity() - 1) / product.getDailyCapacity();
                        totalRequiredDays += daysNeeded;
                    }
                }
                
                // 判断在30天内是否能完成
                canComplete = totalRequiredDays <= 30;
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

    @Override
    public List<ProductionScheduleExportVO> getExportData(ProductionScheduleQueryDTO queryDTO) {
        // 获取排程开始日期（用于过滤和生成日期列）
        if (queryDTO.getStartDate() == null) {
            throw new RuntimeException("排程开始日期不能为空");
        }
        LocalDate startDate = queryDTO.getStartDate();
        
        // 查询所有符合排程开始日期的排程记录（按机台号分组）
        LambdaQueryWrapper<ProductionSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getMachineNo() != null && !queryDTO.getMachineNo().isBlank()) {
            scheduleWrapper.eq(ProductionSchedule::getMachineNo, queryDTO.getMachineNo());
        }
        // 按排程开始日期过滤：只导出排程日期 >= 开始日期的记录
        scheduleWrapper.ge(ProductionSchedule::getScheduleDate, startDate);
        
        // 获取所有符合条件的排程记录
        List<ProductionSchedule> allSchedules = scheduleMapper.selectList(scheduleWrapper);
        
        // 按机台号分组
        java.util.Map<String, List<ProductionSchedule>> schedulesByMachine = allSchedules.stream()
            .collect(Collectors.groupingBy(ProductionSchedule::getMachineNo));
        
        List<ProductionScheduleExportVO> exportList = new ArrayList<>();
        
        // 为每个机台号创建一行导出数据
        for (java.util.Map.Entry<String, List<ProductionSchedule>> entry : schedulesByMachine.entrySet()) {
            String machineNo = entry.getKey();
            List<ProductionSchedule> machineSchedules = entry.getValue();
            
            // 查询设备信息
            Equipment equipment = equipmentMapper.selectOne(
                new LambdaQueryWrapper<Equipment>()
                    .eq(Equipment::getMachineNo, machineNo)
            );
            
            // 按日期排序
            machineSchedules.sort((a, b) -> a.getScheduleDate().compareTo(b.getScheduleDate()));
            
            // 为每个机台号创建一行导出数据（合并所有产品的排程信息）
            ProductionScheduleExportVO exportVO = createExportVOByMachine(
                equipment, machineSchedules, startDate
            );
            exportList.add(exportVO);
        }
        
        return exportList;
    }
    
    /**
     * 按机台号创建导出VO（一个机台号一行，合并所有产品的排程信息）
     */
    private ProductionScheduleExportVO createExportVOByMachine(
            Equipment equipment,
            List<ProductionSchedule> allSchedules,
            LocalDate startDate) {
        ProductionScheduleExportVO exportVO = new ProductionScheduleExportVO();
        
        // 填充设备信息
        if (equipment != null) {
            exportVO.setGroupName(equipment.getGroupName() != null ? equipment.getGroupName() : "-");
            exportVO.setMachineNo(equipment.getMachineNo() != null ? equipment.getMachineNo() : "-");
            exportVO.setEquipmentModel(equipment.getEquipmentModel() != null ? equipment.getEquipmentModel() : "-");
            exportVO.setRobotModel(equipment.getRobotModel() != null ? equipment.getRobotModel() : "-");
            exportVO.setEnableDate(equipment.getEnableDate() != null ? 
                equipment.getEnableDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "-");
            exportVO.setServiceLife(equipment.getServiceLife() != null ? equipment.getServiceLife() + "年" : "-");
            exportVO.setMoldTempMachine(equipment.getMoldTempMachine() != null ? equipment.getMoldTempMachine() : "-");
            exportVO.setChiller(equipment.getChiller() != null ? equipment.getChiller() : "-");
            exportVO.setBasicMold(equipment.getBasicMold() != null ? equipment.getBasicMold() : "-");
            exportVO.setSpareMold1(equipment.getSpareMold1() != null ? equipment.getSpareMold1() : "-");
            exportVO.setSpareMold2(equipment.getSpareMold2() != null ? equipment.getSpareMold2() : "-");
            exportVO.setSpareMold3(equipment.getSpareMold3() != null ? equipment.getSpareMold3() : "-");
        } else {
            exportVO.setGroupName("-");
            exportVO.setMachineNo("-");
            exportVO.setEquipmentModel("-");
            exportVO.setRobotModel("-");
            exportVO.setEnableDate("-");
            exportVO.setServiceLife("-");
            exportVO.setMoldTempMachine("-");
            exportVO.setChiller("-");
            exportVO.setBasicMold("-");
            exportVO.setSpareMold1("-");
            exportVO.setSpareMold2("-");
            exportVO.setSpareMold3("-");
        }
        
        // 产品信息：合并所有产品的信息（用逗号分隔）
        if (!allSchedules.isEmpty()) {
            java.util.Set<String> productNames = allSchedules.stream()
                .map(ProductionSchedule::getProductName)
                .filter(name -> name != null && !name.equals("-"))
                .collect(java.util.stream.Collectors.toSet());
            exportVO.setProductName(String.join(", ", productNames));
            
            // 获取所有排程记录关联的订单ID
            java.util.Set<Long> orderIds = allSchedules.stream()
                .map(ProductionSchedule::getOrderId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());
            
            // 查询订单产品信息，获取订单数量和产能
            List<String> orderQuantityList = new ArrayList<>();
            List<String> dailyCapacityList = new ArrayList<>();
            
            for (Long orderId : orderIds) {
                List<ProductionOrderProduct> products = orderProductMapper.selectList(
                    new LambdaQueryWrapper<ProductionOrderProduct>()
                        .eq(ProductionOrderProduct::getOrderId, orderId)
                        .orderByAsc(ProductionOrderProduct::getSortOrder)
                );
                
                for (ProductionOrderProduct product : products) {
                    if (product.getOrderQuantity() != null) {
                        orderQuantityList.add(product.getOrderQuantity().toString());
                    }
                    if (product.getDailyCapacity() != null) {
                        dailyCapacityList.add(product.getDailyCapacity().toString());
                    }
                }
            }
            
            // 设置订单数量和产能（用逗号分隔多个产品的值）
            if (!orderQuantityList.isEmpty()) {
                exportVO.setOrderQuantity(String.join(", ", orderQuantityList));
            } else {
                exportVO.setOrderQuantity("-");
            }
            
            if (!dailyCapacityList.isEmpty()) {
                exportVO.setDailyCapacity(String.join(", ", dailyCapacityList));
            } else {
                exportVO.setDailyCapacity("-");
            }
        } else {
            exportVO.setProductName("-");
            exportVO.setOrderQuantity("-");
            exportVO.setDailyCapacity("-");
        }
        
        // 按日期映射排程数据（使用scheduleDate作为key）
        java.util.Map<LocalDate, ProductionSchedule> scheduleMap = allSchedules.stream()
            .collect(Collectors.toMap(
                ProductionSchedule::getScheduleDate,
                s -> s,
                (existing, replacement) -> existing // 如果有重复日期，保留第一个
            ));
        
        // 生成30天的日期列表（从开始日期起，排除星期天）
        java.util.List<LocalDate> dateList = new ArrayList<>();
        LocalDate currentDate = startDate;
        int dayCount = 0;
        while (dayCount < 30 && dateList.size() < 30) {
            if (currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                dateList.add(currentDate);
                dayCount++;
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // 设置每天的排程数据（使用实际日期匹配）
        for (int i = 0; i < Math.min(30, dateList.size()); i++) {
            LocalDate scheduleDate = dateList.get(i);
            ProductionSchedule schedule = scheduleMap.get(scheduleDate);
            String dayValue = "-";
            if (schedule != null && schedule.getProductName() != null && !schedule.getProductName().equals("-")) {
                // 格式：产品名称 / 排产数量 / 剩余数量
                dayValue = String.format("%s / %d / %d",
                    schedule.getProductName(),
                    schedule.getProductionQuantity(),
                    schedule.getRemainingQuantity());
            }
            
            // 根据索引设置对应的日期列
            switch (i + 1) {
                case 1: exportVO.setDay1(dayValue); break;
                case 2: exportVO.setDay2(dayValue); break;
                case 3: exportVO.setDay3(dayValue); break;
                case 4: exportVO.setDay4(dayValue); break;
                case 5: exportVO.setDay5(dayValue); break;
                case 6: exportVO.setDay6(dayValue); break;
                case 7: exportVO.setDay7(dayValue); break;
                case 8: exportVO.setDay8(dayValue); break;
                case 9: exportVO.setDay9(dayValue); break;
                case 10: exportVO.setDay10(dayValue); break;
                case 11: exportVO.setDay11(dayValue); break;
                case 12: exportVO.setDay12(dayValue); break;
                case 13: exportVO.setDay13(dayValue); break;
                case 14: exportVO.setDay14(dayValue); break;
                case 15: exportVO.setDay15(dayValue); break;
                case 16: exportVO.setDay16(dayValue); break;
                case 17: exportVO.setDay17(dayValue); break;
                case 18: exportVO.setDay18(dayValue); break;
                case 19: exportVO.setDay19(dayValue); break;
                case 20: exportVO.setDay20(dayValue); break;
                case 21: exportVO.setDay21(dayValue); break;
                case 22: exportVO.setDay22(dayValue); break;
                case 23: exportVO.setDay23(dayValue); break;
                case 24: exportVO.setDay24(dayValue); break;
                case 25: exportVO.setDay25(dayValue); break;
                case 26: exportVO.setDay26(dayValue); break;
                case 27: exportVO.setDay27(dayValue); break;
                case 28: exportVO.setDay28(dayValue); break;
                case 29: exportVO.setDay29(dayValue); break;
                case 30: exportVO.setDay30(dayValue); break;
            }
        }
        
        return exportVO;
    }

    @Override
    @Transactional
    public void deleteScheduleByMachineNo(String machineNo) {
        scheduleMapper.physicalDeleteByMachineNo(machineNo);
    }

    @Override
    @Transactional
    public void deleteScheduleById(Long id) {
        scheduleMapper.deleteById(id);
    }

    @Override
    public List<ProductionScheduleDetailVO> getScheduleDetailList(ProductionScheduleQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<ProductionSchedule> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getMachineNo() != null && !queryDTO.getMachineNo().isBlank()) {
            wrapper.eq(ProductionSchedule::getMachineNo, queryDTO.getMachineNo());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(ProductionSchedule::getScheduleDate, queryDTO.getStartDate());
        }
        wrapper.orderByAsc(ProductionSchedule::getScheduleDate)
               .orderByAsc(ProductionSchedule::getDayNumber);
        
        List<ProductionSchedule> schedules = scheduleMapper.selectList(wrapper);
        
        // 转换为DetailVO
        return schedules.stream().map(schedule -> {
            ProductionScheduleDetailVO vo = new ProductionScheduleDetailVO();
            vo.setId(schedule.getId());
            vo.setMachineNo(schedule.getMachineNo());
            vo.setEquipmentId(schedule.getEquipmentId());
            vo.setEquipmentNo(schedule.getEquipmentNo());
            vo.setScheduleDate(schedule.getScheduleDate());
            vo.setDayNumber(schedule.getDayNumber());
            vo.setProductCode(schedule.getProductCode());
            vo.setProductName(schedule.getProductName());
            vo.setProductionQuantity(schedule.getProductionQuantity());
            vo.setDailyCapacity(schedule.getDailyCapacity());
            vo.setRemainingQuantity(schedule.getRemainingQuantity());
            vo.setOrderId(schedule.getOrderId());
            
            // 查询设备信息
            if (schedule.getEquipmentId() != null) {
                Equipment equipment = equipmentMapper.selectById(schedule.getEquipmentId());
                if (equipment != null) {
                    vo.setEquipmentName(equipment.getEquipmentName());
                    vo.setGroupName(equipment.getGroupName());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
}
