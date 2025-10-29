package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yjz
 */
@Tag(name = "专辑管理")
@RestController
@RequestMapping("api/album/albumInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class AlbumInfoApiController {

    @Autowired
    private AlbumInfoService albumInfoService;

    /**
     * 新增专辑方法
     *
     * @param albumInfoVo
     * @return
     */
    @Operation(summary = "新增专辑")
    @PostMapping("saveAlbumInfo")
    public Result save(@RequestBody @Validated AlbumInfoVo albumInfoVo) {
        //	调用服务层保存方法
        albumInfoService.saveAlbumInfo(albumInfoVo, AuthContextHolder.getUserId());
        return Result.ok();
    }

    /**
     * 根据条件查询专辑列表
     *
     * @param page
     * @param limit
     * @param albumInfoQuery
     * @return
     */
    @Operation(summary = "获取当前用户专辑分页列表")
    @PostMapping("findUserAlbumPage/{page}/{limit}")
    public Result findUserAlbumPage(@Parameter(name = "page", description = "当前页码", required = true)
                                    @PathVariable Long page,
                                    @Parameter(name = "limit", description = "每页记录数", required = true)
                                    @PathVariable Long limit,
                                    @Parameter(name = "albumInfoQuery", description = "查询对象", required = false)
                                    @RequestBody AlbumInfoQuery albumInfoQuery
    ) {
        //	获取数据：
        Long userId = AuthContextHolder.getUserId() == null ? 1L : AuthContextHolder.getUserId();
        albumInfoQuery.setUserId(userId);
        Page<AlbumListVo> albumInfoPage = new Page<>(page, limit);
        //	调用服务层方法
        IPage<AlbumListVo> iPage = this.albumInfoService.findUserAlbumPage(albumInfoPage, albumInfoQuery);
        //	返回数据集
        return Result.ok(iPage);
    }

    /**
     * 根据专辑id删除专辑数据
     *
     * @param id
     * @return
     */
    @Operation(summary = "删除专辑信息")
    @DeleteMapping("removeAlbumInfo/{id}")
    public Result removeAlbumInfoById(@PathVariable Long id) {
        albumInfoService.removeAlbumInfoById(id);
        return Result.ok();
    }


}

