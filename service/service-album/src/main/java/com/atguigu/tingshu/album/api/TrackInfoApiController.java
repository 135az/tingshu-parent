package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author yjz
 */
@Tag(name = "声音管理")
@RestController
@RequestMapping("api/album/trackInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class TrackInfoApiController {

    @Autowired
    private TrackInfoService trackInfoService;

    @Autowired
    private VodService vodService;

    /**
     * 上传声音
     *
     * @param file
     * @return
     */
    @Operation(summary = "上传声音")
    @PostMapping("uploadTrack")
    public Result<Map<String, Object>> uploadTrack(MultipartFile file) {
        //	调用服务层方法
        Map<String, Object> map = vodService.uploadTrack(file);
        return Result.ok(map);
    }

    /**
     * 保存声音
     *
     * @param trackInfoVo
     * @return
     */
    @Operation(summary = "新增声音")
    @PostMapping("saveTrackInfo")
    public Result saveTrackInfo(@RequestBody @Validated TrackInfoVo trackInfoVo) {
        //	调用服务层方法
        trackInfoService.saveTrackInfo(trackInfoVo, AuthContextHolder.getUserId());
        return Result.ok();
    }

    /**
     * 查看声音专辑列表
     *
     * @param page
     * @param limit
     * @param trackInfoQuery
     * @return
     */
    @Operation(summary = "获取当前用户声音分页列表")
    @PostMapping("findUserTrackPage/{page}/{limit}")
    public Result<IPage<TrackListVo>> findUserTrackPage(
            @Parameter(name = "page", description = "当前页面", required = true)
            @PathVariable Long page,
            @Parameter(name = "limit", description = "每页记录数", required = true)
            @PathVariable Long limit,
            @Parameter(name = "trackInfoQuery", description = "查询对象", required = false)
            @RequestBody TrackInfoQuery trackInfoQuery) {
        //	设置当前用户Id
        trackInfoQuery.setUserId(AuthContextHolder.getUserId());
        //	创建对象
        Page<TrackListVo> trackListVoPage = new Page<>(page, limit);
        IPage<TrackListVo> trackListVoIPage = trackInfoService.findUserTrackPage(trackListVoPage, trackInfoQuery);
        //	返回数据
        return Result.ok(trackListVoIPage);
    }

}

