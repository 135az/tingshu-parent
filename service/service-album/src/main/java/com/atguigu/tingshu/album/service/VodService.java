package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface VodService {

    /**
     * 上传音频
     *
     * @param file
     * @return
     */
    Map<String, Object> uploadTrack(MultipartFile file);

    /**
     * 获取音频信息
     *
     * @param mediaFileId
     * @return
     */
    TrackMediaInfoVo getTrackMediaInfo(@NotEmpty(message = "媒体文件Id不能为空") String mediaFileId);
}
