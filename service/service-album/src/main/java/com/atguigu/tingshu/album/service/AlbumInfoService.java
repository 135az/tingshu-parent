package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlbumInfoService extends IService<AlbumInfo> {

    /**
     * 保存专辑信息
     *
     * @param albumInfoVo
     * @param userId
     */
    void saveAlbumInfo(AlbumInfoVo albumInfoVo, Long userId);

    /**
     * 查询专辑列表
     *
     * @param albumInfoPage
     * @param albumInfoQuery
     * @return
     */
    IPage<AlbumListVo> findUserAlbumPage(Page<AlbumListVo> albumInfoPage, AlbumInfoQuery albumInfoQuery);
}
