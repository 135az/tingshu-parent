package com.atguigu.tingshu.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author yjz
 * @Date 2025/10/29 20:36
 * @Description
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.login")
public class WechatAccountConfig {
    //  公众平台的appId
    private String appId;
    //  小程序微信公众平台秘钥
    private String appSecret;
}
