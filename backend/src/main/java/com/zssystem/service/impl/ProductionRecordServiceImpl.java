package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.ProductionRecordQueryDTO;
import com.zssystem.dto.ProductionRecordSaveDTO;
import com.zssystem.entity.Employee;
import com.zssystem.entity.Equipment;
import com.zssystem.entity.ProductionOrder;
import com.zssystem.entity.ProductionOrderProduct;
import com.zssystem.entity.ProductionPlan;
import com.zssystem.entity.ProductionRecord;
import com.zssystem.entity.ProductionSchedule;
import com.zssystem.mapper.EmployeeMapper;
import com.zssystem.mapper.EquipmentMapper;
import com.zssystem.mapper.ProductionOrderMapper;
import com.zssystem.mapper.ProductionOrderProductMapper;
import com.zssystem.mapper.ProductionPlanMapper;
import com.zssystem.mapper.ProductionRecordMapper;
import com.zssystem.mapper.ProductionScheduleMapper;
import com.zssystem.service.ProductionOrderService;
import com.zssystem.service.ProductionPlanService;
import com.zssystem.service.ProductionRecordService;
import com.zssystem.util.BeanUtil;
import com.zssystem.util.CodeGenerator;
import com.zssystem.vo.ProductionRecordVO;
import com.zssystem.vo.ProductionStatisticsVO;
import com.zssystem.vo.excel.ProductionRecordExportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductionRecordServiceImpl implements ProductionRecordService {

    @Autowired
    private ProductionRecordMapper recordMapper;

    @Autowired
    private ProductionOrderMapper orderMapper;

    @Autowired
    private ProductionPlanMapper planMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private ProductionOrderProductMapper orderProductMapper;

    @Autowired
    private ProductionScheduleMapper scheduleMapper;

    @Autowired
    private ProductionOrderService orderService;

    @Autowired
    private ProductionPlanService planService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<ProductionRecordVO> getRecordList(ProductionRecordQueryDTO queryDTO) {
        Page<ProductionRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ProductionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getRecordNo() != null && !queryDTO.getRecordNo().isBlank(), 
                        ProductionRecord::getRecordNo, queryDTO.getRecordNo())
                .eq(queryDTO.getOrderId() != null, ProductionRecord::getOrderId, queryDTO.getOrderId())
                .eq(queryDTO.getPlanId() != null, ProductionRecord::getPlanId, queryDTO.getPlanId())
                .eq(queryDTO.getEquipmentId() != null, ProductionRecord::getEquipmentId, queryDTO.getEquipmentId())
                .eq(queryDTO.getProductionDate() != null, ProductionRecord::getProductionDate, queryDTO.getProductionDate())
                .ge(queryDTO.getStartDate() != null, ProductionRecord::getProductionDate, queryDTO.getStartDate())
                .le(queryDTO.getEndDate() != null, ProductionRecord::getProductionDate, queryDTO.getEndDate())
                .orderByDesc(ProductionRecord::getProductionDate)
                .orderByDesc(ProductionRecord::getCreateTime);

        IPage<ProductionRecord> recordPage = recordMapper.selectPage(page, wrapper);
        return recordPage.convert(record -> {
            ProductionRecordVO vo = BeanUtil.copyProperties(record, ProductionRecordVO.class);
            
            // 填充设备详细信息
            if (record.getEquipmentId() != null) {
                Equipment equipment = equipmentMapper.selectById(record.getEquipmentId());
                if (equipment != null) {
                    vo.setEquipmentNo(equipment.getEquipmentNo());
                    vo.setEquipmentName(equipment.getEquipmentName());
                    vo.setGroupName(equipment.getGroupName());
                    vo.setMachineNo(equipment.getMachineNo());
                    vo.setEquipmentModel(equipment.getEquipmentModel());
                    vo.setRobotModel(equipment.getRobotModel());
                    vo.setEnableDate(equipment.getEnableDate());
                    vo.setServiceLife(equipment.getServiceLife());
                    vo.setMoldTempMachine(equipment.getMoldTempMachine());
                    vo.setChiller(equipment.getChiller());
                    vo.setBasicMold(equipment.getBasicMold());
                    vo.setSpareMold1(equipment.getSpareMold1());
                    vo.setSpareMold2(equipment.getSpareMold2());
                    vo.setSpareMold3(equipment.getSpareMold3());
                }
            }
            
            // 填充订单信息和产品列表
            if (record.getOrderId() != null) {
                ProductionOrder order = orderMapper.selectById(record.getOrderId());
                if (order != null) {
                    vo.setOrderNo(order.getOrderNo());
                    
                    // 查询订单产品列表
                    List<ProductionOrderProduct> products = orderProductMapper.selectList(
                        new LambdaQueryWrapper<ProductionOrderProduct>()
                            .eq(ProductionOrderProduct::getOrderId, order.getId())
                            .orderByAsc(ProductionOrderProduct::getSortOrder)
                    );
                    
                    // 转换为VO
                    List<ProductionRecordVO.ProductInfo> productInfos = products.stream().map(p -> {
                        ProductionRecordVO.ProductInfo info = new ProductionRecordVO.ProductInfo();
                        info.setProductName(p.getProductName());
                        info.setProductCode(p.getProductCode());
                        info.setOrderQuantity(p.getOrderQuantity());
                        info.setDailyCapacity(p.getDailyCapacity());
                        
                        // 从排程中获取剩余数量（最新一天的剩余数量）
                        if (order.getMachineNo() != null) {
                            ProductionSchedule latestSchedule = scheduleMapper.selectOne(
                                new LambdaQueryWrapper<ProductionSchedule>()
                                    .eq(ProductionSchedule::getMachineNo, order.getMachineNo())
                                    .eq(ProductionSchedule::getProductName, p.getProductName())
                                    .eq(ProductionSchedule::getIsSunday, 0)
                                    .orderByDesc(ProductionSchedule::getScheduleDate)
                                    .last("LIMIT 1")
                            );
                            if (latestSchedule != null) {
                                info.setRemainingQuantity(latestSchedule.getRemainingQuantity());
                            }
                        }
                        
                        return info;
                    }).collect(Collectors.toList());
                    vo.setProducts(productInfos);
                    
                    // 设置第一个产品名称（兼容旧代码）
                    if (!productInfos.isEmpty()) {
                        vo.setProductName(productInfos.get(0).getProductName());
                    }
                }
            }
            
            // 填充排程情况（根据机台号查询）
            if (vo.getMachineNo() != null) {
                List<ProductionSchedule> schedules = scheduleMapper.selectList(
                    new LambdaQueryWrapper<ProductionSchedule>()
                        .eq(ProductionSchedule::getMachineNo, vo.getMachineNo())
                        .eq(ProductionSchedule::getIsSunday, 0)
                        .orderByAsc(ProductionSchedule::getScheduleDate)
                        .last("LIMIT 30") // 最多显示30天的排程
                );
                
                List<ProductionRecordVO.ScheduleInfo> scheduleInfos = schedules.stream().map(s -> {
                    ProductionRecordVO.ScheduleInfo info = new ProductionRecordVO.ScheduleInfo();
                    info.setScheduleDate(s.getScheduleDate());
                    info.setDayNumber(s.getDayNumber());
                    info.setProductName(s.getProductName());
                    info.setProductionQuantity(s.getProductionQuantity());
                    info.setDailyCapacity(s.getDailyCapacity());
                    info.setRemainingQuantity(s.getRemainingQuantity());
                    return info;
                }).collect(Collectors.toList());
                vo.setSchedules(scheduleInfos);
            }
            
            // 填充计划信息
            if (record.getPlanId() != null) {
                ProductionPlan plan = planMapper.selectById(record.getPlanId());
                if (plan != null) {
                    vo.setPlanNo(plan.getPlanNo());
                }
            }
            
            // 填充操作员信息
            if (record.getOperatorId() != null) {
                Employee employee = employeeMapper.selectById(record.getOperatorId());
                if (employee != null) {
                    vo.setOperatorName(employee.getName());
                }
            }
            
            return vo;
        });
    }

    @Override
    public ProductionRecordVO getRecordById(Long id) {
        ProductionRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("生产记录不存在");
        }
        ProductionRecordVO vo = BeanUtil.copyProperties(record, ProductionRecordVO.class);
        
        // 填充设备详细信息
        if (record.getEquipmentId() != null) {
            Equipment equipment = equipmentMapper.selectById(record.getEquipmentId());
            if (equipment != null) {
                vo.setEquipmentNo(equipment.getEquipmentNo());
                vo.setEquipmentName(equipment.getEquipmentName());
                vo.setGroupName(equipment.getGroupName());
                vo.setMachineNo(equipment.getMachineNo());
                vo.setEquipmentModel(equipment.getEquipmentModel());
                vo.setRobotModel(equipment.getRobotModel());
                vo.setEnableDate(equipment.getEnableDate());
                vo.setServiceLife(equipment.getServiceLife());
                vo.setMoldTempMachine(equipment.getMoldTempMachine());
                vo.setChiller(equipment.getChiller());
                vo.setBasicMold(equipment.getBasicMold());
                vo.setSpareMold1(equipment.getSpareMold1());
                vo.setSpareMold2(equipment.getSpareMold2());
                vo.setSpareMold3(equipment.getSpareMold3());
            }
        }
        
        // 填充订单信息和产品列表
        if (record.getOrderId() != null) {
            ProductionOrder order = orderMapper.selectById(record.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
                
                // 查询订单产品列表
                List<ProductionOrderProduct> products = orderProductMapper.selectList(
                    new LambdaQueryWrapper<ProductionOrderProduct>()
                        .eq(ProductionOrderProduct::getOrderId, order.getId())
                        .orderByAsc(ProductionOrderProduct::getSortOrder)
                );
                
                // 转换为VO
                List<ProductionRecordVO.ProductInfo> productInfos = products.stream().map(p -> {
                    ProductionRecordVO.ProductInfo info = new ProductionRecordVO.ProductInfo();
                    info.setProductName(p.getProductName());
                    info.setProductCode(p.getProductCode());
                    info.setOrderQuantity(p.getOrderQuantity());
                    info.setDailyCapacity(p.getDailyCapacity());
                    
                    // 从排程中获取剩余数量
                    if (order.getMachineNo() != null) {
                        ProductionSchedule latestSchedule = scheduleMapper.selectOne(
                            new LambdaQueryWrapper<ProductionSchedule>()
                                .eq(ProductionSchedule::getMachineNo, order.getMachineNo())
                                .eq(ProductionSchedule::getProductName, p.getProductName())
                                .eq(ProductionSchedule::getIsSunday, 0)
                                .orderByDesc(ProductionSchedule::getScheduleDate)
                                .last("LIMIT 1")
                        );
                        if (latestSchedule != null) {
                            info.setRemainingQuantity(latestSchedule.getRemainingQuantity());
                        }
                    }
                    
                    return info;
                }).collect(Collectors.toList());
                vo.setProducts(productInfos);
                
                if (!productInfos.isEmpty()) {
                    vo.setProductName(productInfos.get(0).getProductName());
                }
            }
        }
        
        // 填充排程情况
        if (vo.getMachineNo() != null) {
            List<ProductionSchedule> schedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<ProductionSchedule>()
                    .eq(ProductionSchedule::getMachineNo, vo.getMachineNo())
                    .eq(ProductionSchedule::getIsSunday, 0)
                    .orderByAsc(ProductionSchedule::getScheduleDate)
                    .last("LIMIT 30")
            );
            
            List<ProductionRecordVO.ScheduleInfo> scheduleInfos = schedules.stream().map(s -> {
                ProductionRecordVO.ScheduleInfo info = new ProductionRecordVO.ScheduleInfo();
                info.setScheduleDate(s.getScheduleDate());
                info.setDayNumber(s.getDayNumber());
                info.setProductName(s.getProductName());
                info.setProductionQuantity(s.getProductionQuantity());
                info.setDailyCapacity(s.getDailyCapacity());
                info.setRemainingQuantity(s.getRemainingQuantity());
                return info;
            }).collect(Collectors.toList());
            vo.setSchedules(scheduleInfos);
        }
        
        // 填充计划信息
        if (record.getPlanId() != null) {
            ProductionPlan plan = planMapper.selectById(record.getPlanId());
            if (plan != null) {
                vo.setPlanNo(plan.getPlanNo());
            }
        }
        
        // 填充操作员信息
        if (record.getOperatorId() != null) {
            Employee employee = employeeMapper.selectById(record.getOperatorId());
            if (employee != null) {
                vo.setOperatorName(employee.getName());
            }
        }
        
        return vo;
    }

    @Override
    @Transactional
    public void createRecord(ProductionRecordSaveDTO saveDTO) {
        // 校验订单是否存在
        ProductionOrder order = orderMapper.selectById(saveDTO.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 校验计划是否存在（如果提供了计划ID）
        if (saveDTO.getPlanId() != null) {
            ProductionPlan plan = planMapper.selectById(saveDTO.getPlanId());
            if (plan == null) {
                throw new RuntimeException("计划不存在");
            }
        }

        // 生成记录编号
        String datePrefix = "RECORD" + LocalDate.now().format(DATE_FORMATTER);
        Integer count = recordMapper.countByPrefix(datePrefix);
        String recordNo = CodeGenerator.generateRecordNo(count + 1);

        ProductionRecord record = BeanUtil.copyProperties(saveDTO, ProductionRecord.class);
        record.setRecordNo(recordNo);
        if (record.getDefectQuantity() == null) {
            record.setDefectQuantity(0);
        }
        
        // 填充设备编号
        if (record.getEquipmentId() != null) {
            Equipment equipment = equipmentMapper.selectById(record.getEquipmentId());
            if (equipment != null) {
                record.setEquipmentNo(equipment.getEquipmentNo());
            }
        }
        
        // 填充产品信息（如果有关联订单，使用上面已经查询的order对象）
        if (order != null) {
            // 从订单产品表获取第一个产品信息
            List<ProductionOrderProduct> products = orderProductMapper.selectList(
                new LambdaQueryWrapper<ProductionOrderProduct>()
                    .eq(ProductionOrderProduct::getOrderId, order.getId())
                    .orderByAsc(ProductionOrderProduct::getSortOrder)
                    .last("LIMIT 1")
            );
            if (!products.isEmpty()) {
                ProductionOrderProduct firstProduct = products.get(0);
                if (record.getProductName() == null) {
                    record.setProductName(firstProduct.getProductName());
                }
                if (record.getProductCode() == null) {
                    record.setProductCode(firstProduct.getProductCode());
                }
            }
        }
        
        recordMapper.insert(record);

        // 更新订单完成数量
        orderService.updateCompletedQuantity(saveDTO.getOrderId(), saveDTO.getQuantity());

        // 更新计划完成数量（如果有关联计划）
        if (saveDTO.getPlanId() != null) {
            planService.updateCompletedQuantity(saveDTO.getPlanId(), saveDTO.getQuantity());
        }
    }

    @Override
    @Transactional
    public void updateRecord(Long id, ProductionRecordSaveDTO saveDTO) {
        ProductionRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("生产记录不存在");
        }

        // 计算数量变化
        Integer quantityDiff = saveDTO.getQuantity() - record.getQuantity();

        BeanUtil.copyProperties(saveDTO, record, "id", "recordNo", "createTime", "updateTime", "deleted");
        
        // 更新设备编号（如果设备ID变化）
        if (saveDTO.getEquipmentId() != null && !saveDTO.getEquipmentId().equals(record.getEquipmentId())) {
            Equipment equipment = equipmentMapper.selectById(saveDTO.getEquipmentId());
            if (equipment != null) {
                record.setEquipmentNo(equipment.getEquipmentNo());
            }
        }
        
        // 更新产品信息（如果订单ID变化）
        if (saveDTO.getOrderId() != null && !saveDTO.getOrderId().equals(record.getOrderId())) {
            ProductionOrder order = orderMapper.selectById(saveDTO.getOrderId());
            if (order != null) {
                // 从订单产品表获取第一个产品信息
                List<ProductionOrderProduct> products = orderProductMapper.selectList(
                    new LambdaQueryWrapper<ProductionOrderProduct>()
                        .eq(ProductionOrderProduct::getOrderId, order.getId())
                        .orderByAsc(ProductionOrderProduct::getSortOrder)
                        .last("LIMIT 1")
                );
                if (!products.isEmpty()) {
                    ProductionOrderProduct firstProduct = products.get(0);
                    record.setProductName(firstProduct.getProductName());
                    record.setProductCode(firstProduct.getProductCode());
                }
            }
        }
        
        recordMapper.updateById(record);

        // 如果数量有变化，更新订单和计划的完成数量
        if (quantityDiff != 0) {
            orderService.updateCompletedQuantity(record.getOrderId(), quantityDiff);
            if (record.getPlanId() != null) {
                planService.updateCompletedQuantity(record.getPlanId(), quantityDiff);
            }
        }
    }

    @Override
    @Transactional
    public void deleteRecord(Long id) {
        ProductionRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("生产记录不存在");
        }

        // 删除记录
        recordMapper.deleteById(id);

        // 更新订单完成数量（减去删除的记录数量）
        orderService.updateCompletedQuantity(record.getOrderId(), -record.getQuantity());

        // 更新计划完成数量（如果有关联计划）
        if (record.getPlanId() != null) {
            planService.updateCompletedQuantity(record.getPlanId(), -record.getQuantity());
        }
    }

    @Override
    public List<ProductionStatisticsVO> getStatistics(String dimension, LocalDate startDate, LocalDate endDate) {
        List<ProductionStatisticsVO> statistics = new ArrayList<>();
        
        // 这里简化实现，实际应该使用SQL聚合查询
        // 按日期统计
        if ("date".equals(dimension)) {
            LambdaQueryWrapper<ProductionRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(startDate != null, ProductionRecord::getProductionDate, startDate)
                    .le(endDate != null, ProductionRecord::getProductionDate, endDate);
            
            List<ProductionRecord> records = recordMapper.selectList(wrapper);
            // 按日期分组统计
            // 简化实现，实际应该使用SQL GROUP BY
            // 这里只是示例，实际应该使用MyBatis的SQL查询
        }
        
        // 其他维度的统计类似实现
        return statistics;
    }

    @Override
    public List<ProductionRecordExportVO> getExportData(ProductionRecordQueryDTO queryDTO) {
        // 查询所有符合条件的记录（不分页）
        LambdaQueryWrapper<ProductionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getRecordNo() != null && !queryDTO.getRecordNo().isBlank(), 
                        ProductionRecord::getRecordNo, queryDTO.getRecordNo())
                .eq(queryDTO.getOrderId() != null, ProductionRecord::getOrderId, queryDTO.getOrderId())
                .eq(queryDTO.getPlanId() != null, ProductionRecord::getPlanId, queryDTO.getPlanId())
                .eq(queryDTO.getEquipmentId() != null, ProductionRecord::getEquipmentId, queryDTO.getEquipmentId())
                .eq(queryDTO.getProductionDate() != null, ProductionRecord::getProductionDate, queryDTO.getProductionDate())
                .ge(queryDTO.getStartDate() != null, ProductionRecord::getProductionDate, queryDTO.getStartDate())
                .le(queryDTO.getEndDate() != null, ProductionRecord::getProductionDate, queryDTO.getEndDate())
                .orderByDesc(ProductionRecord::getProductionDate)
                .orderByDesc(ProductionRecord::getCreateTime);

        List<ProductionRecord> records = recordMapper.selectList(wrapper);
        
        return records.stream().map(record -> {
            ProductionRecordExportVO exportVO = new ProductionRecordExportVO();
            exportVO.setRecordNo(record.getRecordNo());
            
            // 填充设备详细信息
            Equipment equipment = null;
            if (record.getEquipmentId() != null) {
                equipment = equipmentMapper.selectById(record.getEquipmentId());
            }
            
            if (equipment != null) {
                exportVO.setGroupName(equipment.getGroupName() != null ? equipment.getGroupName() : "-");
                exportVO.setMachineNo(equipment.getMachineNo() != null ? equipment.getMachineNo() : "-");
                exportVO.setEquipmentNo(equipment.getEquipmentNo());
                exportVO.setEquipmentName(equipment.getEquipmentName());
                exportVO.setEquipmentModel(equipment.getEquipmentModel() != null ? equipment.getEquipmentModel() : "-");
                exportVO.setRobotModel(equipment.getRobotModel() != null ? equipment.getRobotModel() : "-");
                exportVO.setEnableDate(equipment.getEnableDate() != null ? equipment.getEnableDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "-");
                // 直接使用设备的使用年限字段（已根据购买日期计算，格式：X年X个月）
                exportVO.setServiceLife(equipment.getServiceLife() != null ? equipment.getServiceLife() : "-");
                exportVO.setMoldTempMachine(equipment.getMoldTempMachine() != null ? equipment.getMoldTempMachine() : "-");
                exportVO.setChiller(equipment.getChiller() != null ? equipment.getChiller() : "-");
                exportVO.setBasicMold(equipment.getBasicMold() != null ? equipment.getBasicMold() : "-");
                exportVO.setSpareMold1(equipment.getSpareMold1() != null ? equipment.getSpareMold1() : "-");
                exportVO.setSpareMold2(equipment.getSpareMold2() != null ? equipment.getSpareMold2() : "-");
                exportVO.setSpareMold3(equipment.getSpareMold3() != null ? equipment.getSpareMold3() : "-");
            } else {
                exportVO.setGroupName("-");
                exportVO.setMachineNo("-");
                exportVO.setEquipmentNo("-");
                exportVO.setEquipmentName("-");
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
            
            // 填充产品信息和排程情况
            String machineNo = equipment != null ? equipment.getMachineNo() : null;
            if (record.getOrderId() != null) {
                ProductionOrder order = orderMapper.selectById(record.getOrderId());
                if (order != null) {
                    // 查询订单产品列表
                    List<ProductionOrderProduct> products = orderProductMapper.selectList(
                        new LambdaQueryWrapper<ProductionOrderProduct>()
                            .eq(ProductionOrderProduct::getOrderId, order.getId())
                            .orderByAsc(ProductionOrderProduct::getSortOrder)
                    );
                    
                    if (!products.isEmpty()) {
                        // 合并产品信息
                        List<String> productNames = new ArrayList<>();
                        List<String> orderQuantities = new ArrayList<>();
                        List<String> dailyCapacities = new ArrayList<>();
                        List<String> remainingQuantities = new ArrayList<>();
                        
                        for (ProductionOrderProduct p : products) {
                            productNames.add(p.getProductName());
                            orderQuantities.add(String.valueOf(p.getOrderQuantity()));
                            dailyCapacities.add(String.valueOf(p.getDailyCapacity()));
                            
                            // 从排程中获取剩余数量
                            if (machineNo != null) {
                                ProductionSchedule latestSchedule = scheduleMapper.selectOne(
                                    new LambdaQueryWrapper<ProductionSchedule>()
                                        .eq(ProductionSchedule::getMachineNo, machineNo)
                                        .eq(ProductionSchedule::getProductName, p.getProductName())
                                        .eq(ProductionSchedule::getIsSunday, 0)
                                        .orderByDesc(ProductionSchedule::getScheduleDate)
                                        .last("LIMIT 1")
                                );
                                if (latestSchedule != null) {
                                    remainingQuantities.add(String.valueOf(latestSchedule.getRemainingQuantity()));
                                } else {
                                    remainingQuantities.add(String.valueOf(p.getOrderQuantity()));
                                }
                            } else {
                                remainingQuantities.add(String.valueOf(p.getOrderQuantity()));
                            }
                        }
                        
                        exportVO.setProductName(String.join(" / ", productNames));
                        exportVO.setOrderQuantity(String.join(" / ", orderQuantities));
                        exportVO.setDailyCapacity(String.join(" / ", dailyCapacities));
                        exportVO.setRemainingQuantity(String.join(" / ", remainingQuantities));
                    } else {
                        exportVO.setProductName("-");
                        exportVO.setOrderQuantity("-");
                        exportVO.setDailyCapacity("-");
                        exportVO.setRemainingQuantity("-");
                    }
                } else {
                    exportVO.setProductName("-");
                    exportVO.setOrderQuantity("-");
                    exportVO.setDailyCapacity("-");
                    exportVO.setRemainingQuantity("-");
                }
            } else {
                exportVO.setProductName("-");
                exportVO.setOrderQuantity("-");
                exportVO.setDailyCapacity("-");
                exportVO.setRemainingQuantity("-");
            }
            
            // 填充排程情况（汇总）
            if (machineNo != null) {
                List<ProductionSchedule> schedules = scheduleMapper.selectList(
                    new LambdaQueryWrapper<ProductionSchedule>()
                        .eq(ProductionSchedule::getMachineNo, machineNo)
                        .eq(ProductionSchedule::getIsSunday, 0)
                        .orderByAsc(ProductionSchedule::getScheduleDate)
                        .last("LIMIT 30")
                );
                
                if (!schedules.isEmpty()) {
                    List<String> scheduleDates = schedules.stream()
                        .map(s -> s.getScheduleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .collect(Collectors.toList());
                    List<String> scheduleProducts = schedules.stream()
                        .map(s -> s.getProductName() + "(" + s.getProductionQuantity() + "件)")
                        .collect(Collectors.toList());
                    
                    exportVO.setScheduleDates(String.join(", ", scheduleDates));
                    exportVO.setScheduleProducts(String.join(", ", scheduleProducts));
                } else {
                    exportVO.setScheduleDates("-");
                    exportVO.setScheduleProducts("-");
                }
            } else {
                exportVO.setScheduleDates("-");
                exportVO.setScheduleProducts("-");
            }
            
            // 格式化日期和时间
            if (record.getProductionDate() != null) {
                exportVO.setProductionDate(record.getProductionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                exportVO.setProductionDate("-");
            }
            if (record.getStartTime() != null) {
                exportVO.setStartTime(record.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                exportVO.setStartTime("-");
            }
            if (record.getEndTime() != null) {
                exportVO.setEndTime(record.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                exportVO.setEndTime("-");
            }
            
            exportVO.setQuantity(record.getQuantity());
            exportVO.setDefectQuantity(record.getDefectQuantity() != null ? record.getDefectQuantity() : 0);
            
            // 计算合格率
            int total = exportVO.getQuantity() + exportVO.getDefectQuantity();
            if (total > 0) {
                double rate = (double) exportVO.getQuantity() / total * 100;
                exportVO.setPassRate(String.format("%.1f%%", rate));
            } else {
                exportVO.setPassRate("0.0%");
            }
            
            exportVO.setRemark(record.getRemark() != null ? record.getRemark() : "-");
            
            return exportVO;
        }).collect(Collectors.toList());
    }
}
