package com.zssystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zssystem.entity.ProductionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductionRecordMapper extends BaseMapper<ProductionRecord> {

    @Select("SELECT * FROM production_record WHERE record_no = #{recordNo} AND deleted = 0 LIMIT 1")
    ProductionRecord selectByRecordNo(String recordNo);

    @Select("SELECT COUNT(*) FROM production_record WHERE record_no LIKE CONCAT(#{prefix}, '%') AND deleted = 0")
    Integer countByPrefix(String prefix);
}
