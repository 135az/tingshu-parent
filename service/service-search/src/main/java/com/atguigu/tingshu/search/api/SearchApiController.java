package com.atguigu.tingshu.search.api;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.vo.search.AlbumInfoIndexVo;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 搜索专辑管理控制器
 *
 * @author yanjiazheng
 */
@Tag(name = "搜索专辑管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/search/albumInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class SearchApiController {

    private final SearchService searchService;

    /**
     * 上架专辑
     *
     * @param albumId
     * @return
     */
    @Operation(summary = "上架专辑")
    @GetMapping("/upperAlbum/{albumId}")
    public Result upperAlbum(@PathVariable Long albumId) {
        //  调用服务层方法.
        this.searchService.upperAlbum(albumId);
        //  默认返回
        return Result.ok();
    }

    /**
     * 批量上架
     *
     * @return
     */
    @Operation(summary = "批量上架")
    @GetMapping("batchUpperAlbum")
    public Result batchUpperAlbum() {
        //  循环
        for (long i = 1; i <= 1500; i++) {
            searchService.upperAlbum(i);
        }
        //  返回数据
        return Result.ok();
    }

    /**
     * 下架专辑
     *
     * @param albumId
     * @return
     */
    @Operation(summary = "下架专辑")
    @GetMapping("lowerAlbum/{albumId}")
    public Result lowerAlbum(@PathVariable Long albumId) {
        searchService.lowerAlbum(albumId);
        return Result.ok();
    }

    /**
     * 批量下架
     *
     * @return
     */
    @Operation(summary = "批量下架")
    @GetMapping("batchLowerAlbum")
    public Result batchLowerAlbum() {
        for (long i = 1; i <= 1500; i++) {
            searchService.lowerAlbum(i);
        }
        return Result.ok();
    }

    /**
     * 根据关键词检索
     *
     * @param albumIndexQuery
     * @return
     * @throws IOException
     */
    @Operation(summary = "专辑搜索列表")
    @PostMapping
    public Result search(@RequestBody AlbumIndexQuery albumIndexQuery) throws IOException {
        //  调用服务层方法.
        AlbumSearchResponseVo albumSearchResponseVo = searchService.search(albumIndexQuery);
        return Result.ok(albumSearchResponseVo);
    }

    /**
     * 根据一级分类Id获取数据
     *
     * @param category1Id
     * @return
     */
    @Operation(summary = "获取频道页数据")
    @GetMapping("channel/{category1Id}")
    public Result channel(@PathVariable Long category1Id) {

        //  调用服务层方法
        List<Map<String, Object>> mapList = searchService.channel(category1Id);
        return Result.ok(mapList);
    }

    /**
     * 自动补全功能
     *
     * @param keyword
     * @return
     */
    @Operation(summary = "关键字自动补全")
    @GetMapping("completeSuggest/{keyword}")
    public Result completeSuggest(@PathVariable String keyword) {
        //  根据关键词查询补全
        List<String> list = searchService.completeSuggest(keyword);
        //  返回数据
        return Result.ok(list);
    }

    /**
     * 更新排行榜
     *
     * @return
     */
    @SneakyThrows
    @Operation(summary = "更新排行榜")
    @GetMapping("updateLatelyAlbumRanking")
    public Result updateLatelyAlbumRanking() {
        //  调用服务层方法
        searchService.updateLatelyAlbumRanking();
        //  默认返回
        return Result.ok();
    }

    /**
     * 获取排行榜列表
     *
     * @param category1Id
     * @param dimension
     * @return
     */
    @Operation(summary = "获取排行榜列表")
    @Parameters({
            @Parameter(name = "category1Id", description = "一级分类", in = ParameterIn.PATH, required = true),
            @Parameter(name = "dimension", description = "热度:hotScore、播放量:playStatNum、订阅量:subscribeStatNum、购买量:buyStatNum、评论数:albumCommentStatNum", required = true, in = ParameterIn.PATH),
    })
    @GetMapping("findRankingList/{category1Id}/{dimension}")
    public Result<List<AlbumInfoIndexVo>> findRankingList(@PathVariable Long category1Id, @PathVariable String dimension) {
        //  调用服务层方法
        List<AlbumInfoIndexVo> infoIndexVoList = searchService.findRankingList(category1Id, dimension);
        //  返回结果集
        return Result.ok(infoIndexVoList);
    }

}

