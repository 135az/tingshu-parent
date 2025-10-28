package com.atguigu.tingshu.album.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author yjz
 * @Date 2025/10/28 14:57
 * @Description
 *
 */
public interface FileUploadService {
    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    String upload(MultipartFile file);
}
