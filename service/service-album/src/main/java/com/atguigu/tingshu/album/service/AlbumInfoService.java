package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlbumInfoService extends IService<AlbumInfo> {

    /**
     * 保存专辑信息
     *
     * @param albumInfoVo
     * @param userId
     */
    void saveAlbumInfo(AlbumInfoVo albumInfoVo, Long userId);

}
