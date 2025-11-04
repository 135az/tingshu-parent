package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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

    /**
     * 根据id删除专辑信息
     *
     * @param id
     */
    void removeAlbumInfoById(Long id);

    /**
     * 根据id查询专辑信息
     *
     * @param id
     * @return
     */
    AlbumInfo getAlbumInfoById(Long id);

    /**
     * 修改专辑信息
     *
     * @param id
     * @param albumInfoVo
     */
    void updateAlbumInfo(Long id, AlbumInfoVo albumInfoVo);

    /**
     * 查询用户所有专辑列表
     *
     * @param userId
     * @return
     */
    List<AlbumInfo> findUserAllAlbumList(Long userId);

    /**
     * 根据专辑id查询专辑属性值列表
     *
     * @param albumId
     * @return
     */
    List<AlbumAttributeValue> findAlbumAttributeValueByAlbumId(Long albumId);

    /**
     * 根据专辑id查询专辑统计信息
     *
     * @param albumId
     * @return
     */
    AlbumStatVo getAlbumStatVoByAlbumId(Long albumId);

    /**
     * 更新专辑统计信息
     *
     * @param albumId
     * @param statType
     * @param count
     */
    void updateStat(Long albumId, String statType, Integer count);
}
