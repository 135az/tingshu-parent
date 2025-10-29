package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.BaseAttribute;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yjz
 */
@Mapper
public interface BaseAttributeMapper extends BaseMapper<BaseAttribute> {

    /**
     * 根据一级分类id查询属性
     *
     * @param category1Id 一级分类id
     * @return 属性列表
     */
    List<BaseAttribute> selectBaseAttributeList(@Param("category1Id") Long category1Id);
}
