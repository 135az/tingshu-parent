package com.atguigu.tingshu.album.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * Minio配置类
 *
 * @author yjz
 */
@Configuration
@ConfigurationProperties(prefix = "minio") // 读取节点
@RefreshScope
@Data
public class MinioConstantProperties {
    //  服务器的地址
    private String endpointUrl;
    //  用户名
    private String accessKey;
    //  密码
    private String secreKey;
    //  存储桶名称
    private String bucketName;
}
