package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.ProductionPlanQueryDTO;
import com.zssystem.dto.ProductionPlanSaveDTO;
import com.zssystem.entity.Employee;
import com.zssystem.entity.ProductionOrder;
import com.zssystem.entity.ProductionOrderProduct;
import com.zssystem.entity.ProductionPlan;
import com.zssystem.entity.ProductionRecord;
import com.zssystem.mapper.EmployeeMapper;
import com.zssystem.mapper.ProductionOrderMapper;
import com.zssystem.mapper.ProductionOrderProductMapper;
import com.zssystem.mapper.ProductionPlanMapper;
import com.zssystem.mapper.ProductionRecordMapper;
import com.zssystem.service.ProductionPlanService;
import com.zssystem.util.BeanUtil;
import com.zssystem.util.CodeGenerator;
import com.zssystem.vo.ProductionPlanVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProductionPlanServiceImpl implements ProductionPlanService {

    @Autowired
    private ProductionPlanMapper planMapper;

    @Autowired
    private ProductionOrderMapper orderMapper;

    @Autowired
    private ProductionOrderProductMapper orderProductMapper;

    @Autowired
    private ProductionRecordMapper recordMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<ProductionPlanVO> getPlanList(ProductionPlanQueryDTO queryDTO) {
        Page<ProductionPlan> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ProductionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getPlanNo() != null && !queryDTO.getPlanNo().isBlank(), 
                        ProductionPlan::getPlanNo, queryDTO.getPlanNo())
                .eq(queryDTO.getOrderId() != null, ProductionPlan::getOrderId, queryDTO.getOrderId())
                .eq(queryDTO.getEquipmentId() != null, ProductionPlan::getEquipmentId, queryDTO.getEquipmentId())
                .eq(queryDTO.getStatus() != null, ProductionPlan::getStatus, queryDTO.getStatus())
                .orderByDesc(ProductionPlan::getCreateTime);

        IPage<ProductionPlan> planPage = planMapper.selectPage(page, wrapper);
        return planPage.convert(plan -> {
            ProductionPlanVO vo = BeanUtil.copyProperties(plan, ProductionPlanVO.class);
        // 填充订单信息
        if (plan.getOrderId() != null) {
            ProductionOrder order = orderMapper.selectById(plan.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
                // 从订单产品表获取第一个产品名称
                List<ProductionOrderProduct> products = orderProductMapper.selectList(
                    new LambdaQueryWrapper<ProductionOrderProduct>()
                        .eq(ProductionOrderProduct::getOrderId, order.getId())
                        .orderByAsc(ProductionOrderProduct::getSortOrder)
                        .last("LIMIT 1")
                );
                if (!products.isEmpty()) {
                    vo.setProductName(products.get(0).getProductName());
                }
            }
        }
            // 填充操作员信息
            if (plan.getOperatorId() != null) {
                Employee employee = employeeMapper.selectById(plan.getOperatorId());
                if (employee != null) {
                    vo.setOperatorName(employee.getName());
                }
            }
            // 计算已完成数量
            Long completedCount = recordMapper.selectCount(new LambdaQueryWrapper<ProductionRecord>()
                    .eq(ProductionRecord::getPlanId, plan.getId()));
            vo.setCompletedQuantity(completedCount != null ? completedCount.intValue() : 0);
            return vo;
        });
    }

    @Override
    public ProductionPlanVO getPlanById(Long id) {
        ProductionPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("计划不存在");
        }
        ProductionPlanVO vo = BeanUtil.copyProperties(plan, ProductionPlanVO.class);
        // 填充订单信息
        if (plan.getOrderId() != null) {
            ProductionOrder order = orderMapper.selectById(plan.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
                // 从订单产品表获取第一个产品名称
                List<ProductionOrderProduct> products = orderProductMapper.selectList(
                    new LambdaQueryWrapper<ProductionOrderProduct>()
                        .eq(ProductionOrderProduct::getOrderId, order.getId())
                        .orderByAsc(ProductionOrderProduct::getSortOrder)
                        .last("LIMIT 1")
                );
                if (!products.isEmpty()) {
                    vo.setProductName(products.get(0).getProductName());
                }
            }
        }
        // 填充操作员信息
        if (plan.getOperatorId() != null) {
            Employee employee = employeeMapper.selectById(plan.getOperatorId());
            if (employee != null) {
                vo.setOperatorName(employee.getName());
            }
        }
        // 计算已完成数量
        Long completedCount = recordMapper.selectCount(new LambdaQueryWrapper<ProductionRecord>()
                .eq(ProductionRecord::getPlanId, plan.getId()));
        vo.setCompletedQuantity(completedCount != null ? completedCount.intValue() : 0);
        return vo;
    }

    @Override
    @Transactional
    public void createPlan(ProductionPlanSaveDTO saveDTO) {
        // 校验订单是否存在
        ProductionOrder order = orderMapper.selectById(saveDTO.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 生成计划编号
        String datePrefix = "PLAN" + LocalDate.now().format(DATE_FORMATTER);
        Integer count = planMapper.countByPrefix(datePrefix);
        String planNo = CodeGenerator.generatePlanNo(count + 1);

        ProductionPlan plan = BeanUtil.copyProperties(saveDTO, ProductionPlan.class);
        plan.setPlanNo(planNo);
        if (plan.getStatus() == null) {
            plan.setStatus(0); // 默认待执行
        }
        planMapper.insert(plan);
    }

    @Override
    @Transactional
    public void updatePlan(Long id, ProductionPlanSaveDTO saveDTO) {
        ProductionPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("计划不存在");
        }

        // 校验订单是否存在
        if (saveDTO.getOrderId() != null) {
            ProductionOrder order = orderMapper.selectById(saveDTO.getOrderId());
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
        }

        BeanUtil.copyProperties(saveDTO, plan, "id", "planNo", "createTime", "updateTime", "deleted");
        planMapper.updateById(plan);
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        ProductionPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("计划不存在");
        }

        // 检查是否有生产记录
        Long recordCount = recordMapper.selectCount(new LambdaQueryWrapper<ProductionRecord>()
                .eq(ProductionRecord::getPlanId, id));
        if (recordCount != null && recordCount > 0) {
            throw new RuntimeException("计划已有生产记录，无法删除");
        }

        planMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateCompletedQuantity(Long planId, Integer quantity) {
        ProductionPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            return;
        }

        // 计算已完成数量
        Long completedCount = recordMapper.selectCount(new LambdaQueryWrapper<ProductionRecord>()
                .eq(ProductionRecord::getPlanId, planId));
        Integer completedQuantity = completedCount != null ? completedCount.intValue() : 0;

        // 如果完成数量达到计划数量，自动更新状态为已完成
        if (completedQuantity >= plan.getPlanQuantity()) {
            plan.setStatus(2); // 已完成
        } else if (plan.getStatus() == 0 && completedQuantity > 0) {
            // 如果从待执行状态开始有记录，更新为执行中
            plan.setStatus(1); // 执行中
        }

        planMapper.updateById(plan);
    }
}
