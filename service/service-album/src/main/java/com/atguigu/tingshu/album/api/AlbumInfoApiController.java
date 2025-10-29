package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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


}

