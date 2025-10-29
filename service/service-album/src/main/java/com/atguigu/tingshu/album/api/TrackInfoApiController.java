package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
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

}

