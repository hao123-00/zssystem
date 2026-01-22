package com.zssystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zssystem.entity.ProductionSchedule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductionScheduleMapper extends BaseMapper<ProductionSchedule> {
    
    /**
     * 物理删除指定机台号的排程记录（绕过逻辑删除）
     */
    @Delete("DELETE FROM production_schedule WHERE machine_no = #{machineNo}")
    void physicalDeleteByMachineNo(@Param("machineNo") String machineNo);
}
