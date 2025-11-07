package com.atguigu.tingshu.account.receiver;

import com.atguigu.tingshu.account.service.RechargeInfoService;
import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
@Component
public class AccountReceiver {

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private RechargeInfoService rechargeInfoService;

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

    /**
     * 扣减锁定金额
     *
     * @param record
     */
    @KafkaListener(topics = KafkaConstant.QUEUE_ACCOUNT_MINUS)
    public void minus(ConsumerRecord<String, String> record) {
        String orderNo = record.value();
        if (StringUtils.isEmpty(orderNo)) {
            return;
        }
        // 扣减锁定金额
        userAccountService.minus(orderNo);
    }

    /**
     * 解锁锁定金额
     *
     * @param record
     */
    @KafkaListener(topics = KafkaConstant.QUEUE_ACCOUNT_UNLOCK)
    public void unlock(ConsumerRecord<String, String> record) {
        String orderNo = record.value();
        if (StringUtils.isEmpty(orderNo)) {
            return;
        }
        //  调用解除锁定
        userAccountService.unlock(orderNo);
    }

    /**
     * 充值成功通知
     *
     * @param record
     */
    @KafkaListener(topics = KafkaConstant.QUEUE_RECHARGE_PAY_SUCCESS)
    public void rechargePaySuccess(ConsumerRecord<String, String> record) {
        String orderNo = record.value();
        log.info("充值成功通知: {}", orderNo);
        // 通知更新用户账号
        rechargeInfoService.rechargePaySuccess(orderNo);
    }
}
