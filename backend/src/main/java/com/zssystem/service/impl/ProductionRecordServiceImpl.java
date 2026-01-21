package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.ProductionRecordQueryDTO;
import com.zssystem.dto.ProductionRecordSaveDTO;
import com.zssystem.entity.Employee;
import com.zssystem.entity.ProductionOrder;
import com.zssystem.entity.ProductionPlan;
import com.zssystem.entity.ProductionRecord;
import com.zssystem.mapper.EmployeeMapper;
import com.zssystem.mapper.ProductionOrderMapper;
import com.zssystem.mapper.ProductionPlanMapper;
import com.zssystem.mapper.ProductionRecordMapper;
import com.zssystem.service.ProductionOrderService;
import com.zssystem.service.ProductionPlanService;
import com.zssystem.service.ProductionRecordService;
import com.zssystem.util.BeanUtil;
import com.zssystem.util.CodeGenerator;
import com.zssystem.vo.ProductionRecordVO;
import com.zssystem.vo.ProductionStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
            // 填充订单信息
            if (record.getOrderId() != null) {
                ProductionOrder order = orderMapper.selectById(record.getOrderId());
                if (order != null) {
                    vo.setOrderNo(order.getOrderNo());
                    vo.setProductName(order.getProductName());
                }
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
        // 填充订单信息
        if (record.getOrderId() != null) {
            ProductionOrder order = orderMapper.selectById(record.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
                vo.setProductName(order.getProductName());
            }
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
}
