package com.zssystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zssystem.entity.ProductionPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductionPlanMapper extends BaseMapper<ProductionPlan> {

    @Select("SELECT * FROM production_plan WHERE plan_no = #{planNo} AND deleted = 0 LIMIT 1")
    ProductionPlan selectByPlanNo(String planNo);

    @Select("SELECT COUNT(*) FROM production_plan WHERE plan_no LIKE CONCAT(#{prefix}, '%') AND deleted = 0")
    Integer countByPrefix(String prefix);
}
