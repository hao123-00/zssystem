package com.zssystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zssystem.entity.Site5sCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface Site5sCheckMapper extends BaseMapper<Site5sCheck> {
    
    /**
     * 查询指定前缀的最大序号（忽略删除状态）
     */
    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(check_no, LENGTH(#{prefix}) + 1) AS UNSIGNED)), 0) " +
            "FROM site_5s_check " +
            "WHERE check_no LIKE CONCAT(#{prefix}, '%')")
    Integer getMaxSequenceByPrefix(@Param("prefix") String prefix);
    
    /**
     * 根据检查单号查询（忽略删除状态）
     */
    @Select("SELECT * FROM site_5s_check WHERE check_no = #{checkNo} LIMIT 1")
    Site5sCheck selectByCheckNoIgnoreDeleted(@Param("checkNo") String checkNo);
}
