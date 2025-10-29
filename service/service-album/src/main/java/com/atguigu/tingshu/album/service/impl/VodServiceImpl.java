package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.util.UploadFileUtil;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;


@Service
public class VodServiceImpl implements VodService {

    @Autowired
    private VodConstantProperties vodConstantProperties;

    @SneakyThrows
    @Override
    public Map<String, Object> uploadTrack(MultipartFile file) {
        //  声音上传临时目录：
        String tempPath = UploadFileUtil.uploadTempPath(vodConstantProperties.getTempPath(), file);
        //  创建上传声音客户端
        VodUploadClient client = new VodUploadClient(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());
        //  构建上传请求对象
        VodUploadRequest request = new VodUploadRequest();
        //  设置视频本地地址
        request.setMediaFilePath(tempPath);
        //  指定任务流
        //  request.setProcedure(vodConstantProperties.getProcedure());
        //  调用上传方法
        VodUploadResponse response = client.upload(vodConstantProperties.getRegion(), request);
        //  创建map 对象
        HashMap<String, Object> map = new HashMap<>();
        map.put("mediaFileId", response.getFileId());
        map.put("mediaUrl", response.getMediaUrl());
        //  返回map 数据
        return map;
    }
}
