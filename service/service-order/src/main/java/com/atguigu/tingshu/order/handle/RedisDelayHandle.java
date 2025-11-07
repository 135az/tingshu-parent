package com.atguigu.tingshu.order.handle;

import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.order.service.OrderInfoService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yjz
 * @Date 2025/11/6 20:00
 * @Description
 *
 */
@Slf4j
@Component
public class RedisDelayHandle {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private OrderInfoService orderInfoService;

    /**
     * 监听延时队列
     */
    @PostConstruct
    public void listener() {
        new Thread(() -> {
            while (true) {
                // 获取到阻塞队列
                RBlockingDeque<String> blockingDeque = redissonClient.getBlockingDeque(KafkaConstant.QUEUE_ORDER_CANCEL);
                try {
                    // 从阻塞队列中获取到订单Id
                    String orderId = blockingDeque.take();
                    // 订单Id 不为空的时候，调用取消订单方法
                    if (!StringUtils.isEmpty(orderId)) {
                        log.info("接收延时队列成功，订单id：{}", orderId);
                        orderInfoService.orderCancel(Long.parseLong(orderId));
                    }
                } catch (InterruptedException e) {
                    log.error("接收延时队列失败");
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
