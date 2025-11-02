package com.atguigu.tingshu.search.api;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "搜索专辑管理")
@RestController
@RequestMapping("api/search/albumInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class SearchApiController {

    @Autowired
    private SearchService searchService;

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

}

