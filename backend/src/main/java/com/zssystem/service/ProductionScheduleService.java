package com.zssystem.service;

import com.zssystem.dto.ProductionScheduleQueryDTO;
import com.zssystem.vo.ProductionScheduleVO;

import java.time.LocalDate;
import java.util.List;

public interface ProductionScheduleService {
    /**
     * 按机台号生成生产计划排程（避开星期天）
     * @param machineNo 机台号
     * @param startDate 排程开始日期
     * @return 排程结果
     */
    ProductionScheduleVO generateSchedule(String machineNo, LocalDate startDate);
    
    /**
     * 查询排程列表
     * @param queryDTO 查询条件
     * @return 排程列表
     */
    List<ProductionScheduleVO> getScheduleList(ProductionScheduleQueryDTO queryDTO);
    
    /**
     * 根据机台号查询排程
     * @param machineNo 机台号
     * @param startDate 开始日期
     * @return 排程结果
     */
    ProductionScheduleVO getScheduleByMachineNo(String machineNo, LocalDate startDate);
}
