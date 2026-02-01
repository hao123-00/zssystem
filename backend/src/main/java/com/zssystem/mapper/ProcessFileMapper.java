package com.zssystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zssystem.entity.ProcessFile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 工艺文件Mapper接口
 */
@Mapper
public interface ProcessFileMapper extends BaseMapper<ProcessFile> {
    
    /**
     * 根据前缀查询最大文件编号（用于生成新编号）
     */
    @Select("SELECT file_no FROM process_file WHERE file_no LIKE CONCAT(#{prefix}, '%') ORDER BY file_no DESC LIMIT 1")
    String getMaxFileNoByPrefix(@Param("prefix") String prefix);
    
    /**
     * 物理删除工艺文件（绕过逻辑删除）
     */
    @Delete("DELETE FROM process_file WHERE id = #{id}")
    int deletePhysicalById(@Param("id") Long id);
}
