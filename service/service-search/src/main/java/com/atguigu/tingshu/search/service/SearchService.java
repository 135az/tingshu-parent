package com.atguigu.tingshu.search.service;

import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;

import java.util.List;
import java.util.Map;

public interface SearchService {

    /**
     * 上架专辑
     *
     * @param albumId
     */
    void upperAlbum(Long albumId);

    /**
     * 下架专辑
     *
     * @param albumId
     */
    void lowerAlbum(Long albumId);

    /**
     * 根据关键词检索
     *
     * @param albumIndexQuery
     * @return
     */
    AlbumSearchResponseVo search(AlbumIndexQuery albumIndexQuery);

    /**
     * 根据一级分类Id 获取置顶数据
     *
     * @param category1Id
     * @return
     */
    List<Map<String, Object>> channel(Long category1Id);
}
