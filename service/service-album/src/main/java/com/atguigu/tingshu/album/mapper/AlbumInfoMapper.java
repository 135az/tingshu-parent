package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AlbumInfoMapper extends BaseMapper<AlbumInfo> {

    /**
     * 根据条件查询专辑列表
     *
     * @param albumInfoPage
     * @param albumInfoQuery
     * @return
     */
    IPage<AlbumListVo> selectUserAlbumPage(Page<AlbumListVo> albumInfoPage, @Param("vo") AlbumInfoQuery albumInfoQuery);
}
