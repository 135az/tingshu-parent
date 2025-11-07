package com.atguigu.tingshu.user.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserPaidRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author yjz
 * @Date 2025/11/6 19:52
 * @Description
 *
 */
@Slf4j
@Component
public class UserReceiver {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 更新用户支付记录
     *
     * @param record
     */
    @KafkaListener(topics = KafkaConstant.QUEUE_USER_PAY_RECORD)
    public void updateUserPaidRecord(ConsumerRecord<String, String> record) {
        UserPaidRecordVo userPaidRecordVo = JSON.parseObject(record.value(), UserPaidRecordVo.class);
        log.info("更新用户支付记录: {}", JSON.toJSONString(userPaidRecordVo));
        // 通知更新用户账号
        userInfoService.updateUserPaidRecord(userPaidRecordVo);
    }
}
