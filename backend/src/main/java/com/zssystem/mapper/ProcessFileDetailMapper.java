package com.zssystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zssystem.entity.ProcessFileDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 工艺文件详细内容Mapper
 */
@Mapper
public interface ProcessFileDetailMapper extends BaseMapper<ProcessFileDetail> {
    
    /**
     * 物理删除指定工艺文件的所有详情（绕过逻辑删除）
     */
    @Delete("DELETE FROM process_file_detail WHERE file_id = #{fileId}")
    int deletePhysicalByFileId(@Param("fileId") Long fileId);
}
