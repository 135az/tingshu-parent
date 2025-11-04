package com.atguigu.tingshu.album.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.vo.album.TrackStatMqVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author yjz
 * @Date 2025/11/4 16:19
 * @Description
 *
 */
@Component
@Slf4j
public class TrackReceiver {

    @Autowired
    private TrackInfoService trackInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 监听更新统计状态
     *
     * @param consumerRecord
     */
    @KafkaListener(topics = KafkaConstant.QUEUE_TRACK_STAT_UPDATE)
    public void updateStat(ConsumerRecord<String, String> consumerRecord) {
        //  获取发送的数据
        TrackStatMqVo trackStatMqVo = JSON.parseObject(consumerRecord.value(), TrackStatMqVo.class);
        log.info(" 更新声音统计：" + JSON.toJSONString(trackStatMqVo));
        //  业务去重
        String key = trackStatMqVo.getBusinessNo();
        boolean isExist = redisTemplate.opsForValue().setIfAbsent(key, 1, 1, TimeUnit.HOURS);
        //  第一次执行

        if (isExist) {
            //  调用服务层方法
            try {
                trackInfoService.updateStat(trackStatMqVo.getAlbumId(), trackStatMqVo.getTrackId(), trackStatMqVo.getStatType(), trackStatMqVo.getCount());
            } catch (Exception e) {
                this.redisTemplate.delete(key);
            }
        }
    }
}
