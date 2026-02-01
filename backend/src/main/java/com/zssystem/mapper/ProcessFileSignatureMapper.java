package com.zssystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zssystem.entity.ProcessFileSignature;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 工艺文件电子签名Mapper
 */
@Mapper
public interface ProcessFileSignatureMapper extends BaseMapper<ProcessFileSignature> {
    
    /**
     * 根据文件ID和签名类型查询签名
     */
    @Select("SELECT * FROM process_file_signature WHERE file_id = #{fileId} AND signature_type = #{signatureType} AND deleted = 0 LIMIT 1")
    ProcessFileSignature selectByFileIdAndType(Long fileId, String signatureType);
    
    /**
     * 物理删除指定工艺文件的所有签名（绕过逻辑删除）
     */
    @Delete("DELETE FROM process_file_signature WHERE file_id = #{fileId}")
    int deletePhysicalByFileId(@Param("fileId") Long fileId);
}
