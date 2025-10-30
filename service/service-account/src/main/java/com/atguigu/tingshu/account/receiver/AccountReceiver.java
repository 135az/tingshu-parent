package com.atguigu.tingshu.account.receiver;

import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author yjz
 * @Date 2025/10/30 10:03
 * @Description
 *
 */
@Component
public class AccountReceiver {

    @Autowired
    private UserAccountService userAccountService;

    /**
     * 监听消息并添加账户信息
     *
     * @param record
     */
    @KafkaListener(topics = KafkaConstant.QUEUE_USER_REGISTER)
    public void addUserAccount(ConsumerRecord<String, String> record) {
        //  获取用户Id
        Long userId = Long.parseLong(record.value());
        if (null == userId) {
            return;
        }
        //  添加账户信息
        userAccountService.addUserAccount(userId);
    }
}
