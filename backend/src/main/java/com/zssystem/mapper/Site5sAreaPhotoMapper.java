package com.zssystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zssystem.entity.Site5sAreaPhoto;
import org.apache.ibatis.annotations.Delete;

public interface Site5sAreaPhotoMapper extends BaseMapper<Site5sAreaPhoto> {
    @Delete("DELETE FROM site_5s_area_photo WHERE id = #{id}")
    int deleteByIdPhysical(Long id);
}
