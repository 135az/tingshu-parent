package com.atguigu.tingshu.search.receiver;

import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author yjz
 * @Date 2025/11/1 18:02
 * @Description
 *
 */
@Component
@Slf4j
public class SearchReceiver {
    @Autowired
    private SearchService searchService;

    /**
     * 监听专辑上架
     *
     * @param consumerRecord
     */
    @KafkaListener(topics = KafkaConstant.QUEUE_ALBUM_UPPER)
    public void upperGoods(ConsumerRecord<String, String> consumerRecord) {
        //  获取到发送的消息
        Long albumId = Long.parseLong(consumerRecord.value());
        if (null != albumId) {
            searchService.upperAlbum(albumId);
        }
    }

    /**
     * 监听专辑下架
     *
     * @param consumerRecord
     */
    @KafkaListener(topics = KafkaConstant.QUEUE_ALBUM_LOWER)
    public void lowerGoods(ConsumerRecord<String, String> consumerRecord) {
        //  获取到发送的消息
        Long albumId = Long.parseLong(consumerRecord.value());
        if (null != albumId) {
            searchService.lowerAlbum(albumId);
        }
    }
}
