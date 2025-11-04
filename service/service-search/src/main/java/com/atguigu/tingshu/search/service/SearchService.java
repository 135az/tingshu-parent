package com.atguigu.tingshu.search.service;

import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.vo.search.AlbumInfoIndexVo;
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

    /**
     * 根据关键字自动补全功能
     *
     * @param keyword
     * @return
     */
    List<String> completeSuggest(String keyword);

    /**
     * 更新最近播放专辑排行
     */
    void updateLatelyAlbumRanking();

    /**
     * 根据一级分类Id 获取排行榜数据
     *
     * @param category1Id
     * @param dimension
     * @return
     */
    List<AlbumInfoIndexVo> findRankingList(Long category1Id, String dimension);
}
