package com.atguigu.tingshu.album.service;

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
}
