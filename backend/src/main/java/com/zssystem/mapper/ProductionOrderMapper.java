package com.zssystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zssystem.entity.ProductionOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductionOrderMapper extends BaseMapper<ProductionOrder> {

    @Select("SELECT * FROM production_order WHERE order_no = #{orderNo} AND deleted = 0 LIMIT 1")
    ProductionOrder selectByOrderNo(String orderNo);

    @Select("SELECT COUNT(*) FROM production_order WHERE order_no LIKE CONCAT(#{prefix}, '%') AND deleted = 0")
    Integer countByPrefix(String prefix);
}
