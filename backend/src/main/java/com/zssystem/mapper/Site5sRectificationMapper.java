package com.zssystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zssystem.entity.Site5sRectification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface Site5sRectificationMapper extends BaseMapper<Site5sRectification> {
    
    /**
     * 查询指定前缀的最大序号（忽略删除状态）
     */
    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(task_no, LENGTH(#{prefix}) + 1) AS UNSIGNED)), 0) " +
            "FROM site_5s_rectification " +
            "WHERE task_no LIKE CONCAT(#{prefix}, '%')")
    Integer getMaxSequenceByPrefix(@Param("prefix") String prefix);
    
    /**
     * 根据任务编号查询（忽略删除状态）
     */
    @Select("SELECT * FROM site_5s_rectification WHERE task_no = #{taskNo} LIMIT 1")
    Site5sRectification selectByTaskNoIgnoreDeleted(@Param("taskNo") String taskNo);
}
