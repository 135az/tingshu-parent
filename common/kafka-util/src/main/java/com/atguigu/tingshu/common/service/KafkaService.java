package com.atguigu.tingshu.common.service;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 发送消息
     *
     * @param topic
     * @param value
     * @return
     */
    public boolean sendMessage(String topic, Object value) {
        return this.sendMessage(topic, null, value);
    }

    /**
     * 封装发送消息方法
     *
     * @param topic
     * @param key
     * @param value
     * @return
     */
    private boolean sendMessage(String topic, String key, Object value) {
        // 发送消息对象
        CompletableFuture completableFuture = kafkaTemplate.send(topic, key, value);
        //  执行成功回调方法
        completableFuture.thenAccept(result -> {
            logger.debug("发送消息成功: topic={}，key={},value={}", topic, key, JSON.toJSONString(value));
        }).exceptionally(e -> {
            logger.error("发送消息失败: topic={}，key={},value={}", topic, key, JSON.toJSONString(value));
            return false;
        });
        return true;
    }
}
