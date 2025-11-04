package com.atguigu.tingshu.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.service.KafkaService;
import com.atguigu.tingshu.common.util.MongoUtil;
import com.atguigu.tingshu.model.user.UserListenProcess;
import com.atguigu.tingshu.user.service.UserListenProcessService;
import com.atguigu.tingshu.vo.album.TrackStatMqVo;
import com.atguigu.tingshu.vo.user.UserListenProcessVo;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserListenProcessServiceImpl implements UserListenProcessService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private KafkaService kafkaService;

    @Override
    public BigDecimal getTrackBreakSecond(Long userId, Long trackId) {
        //	根据用户Id,声音Id获取播放进度对象
        Query query = Query.query(Criteria.where("userId").is(userId).and("trackId").is(trackId));
        UserListenProcess userListenProcess = mongoTemplate.findOne(query,
                UserListenProcess.class,
                MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        //	判断
        if (null != userListenProcess) {
            //	获取到播放的跳出时间
            return userListenProcess.getBreakSecond();
        }
        return new BigDecimal("0");
    }

    @Override
    public void updateListenProcess(Long userId, UserListenProcessVo userListenProcessVo) {
        // 根据用户Id，声音Id 设置查询条件
        Query query = Query.query(Criteria.where("userId").is(userId).and("trackId").is(userListenProcessVo.getTrackId()));
        UserListenProcess userListenProcess = this.mongoTemplate.findOne(query, UserListenProcess.class, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        //	判断
        if (null != userListenProcess) {
            //	设置更新时间
            userListenProcess.setUpdateTime(new Date());
            //	设置跳出时间
            userListenProcess.setBreakSecond(userListenProcessVo.getBreakSecond());
            //	存储数据
            mongoTemplate.save(userListenProcess, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        } else {
            //	创建对象
            userListenProcess = new UserListenProcess();
            //	进行属性拷贝
            BeanUtils.copyProperties(userListenProcessVo, userListenProcess);
            //	设置Id
            userListenProcess.setId(ObjectId.get().toString());
            //	设置用户Id
            userListenProcess.setUserId(userId);
            //	设置是否显示
            userListenProcess.setIsShow(1);
            //	创建时间
            userListenProcess.setCreateTime(new Date());
            //	更新时间
            userListenProcess.setUpdateTime(new Date());
            //	保存数据
            mongoTemplate.save(userListenProcess, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        }
        // 记录专辑与声音播放量，同一个用户同一个声音每天只记录一次播放量
        // 专辑的播放量 = 声音播放量的总和   声音：用户
        String key = "user:track:" + userId;
        // get bit key offset offset=trackId; 
        Boolean isExist = redisTemplate.opsForValue().getBit(key, userListenProcessVo.getTrackId());
        if (!isExist) {
            redisTemplate.opsForValue().setBit(key, userListenProcessVo.getTrackId(), true);

            //  设置key 的过期时间
            redisTemplate.expire(key, 24 * 60 * 60, TimeUnit.SECONDS);

            // 发送消息，更新播放量统计
            TrackStatMqVo trackStatMqVo = new TrackStatMqVo();
            trackStatMqVo.setBusinessNo(UUID.randomUUID().toString().replaceAll("-", ""));
            trackStatMqVo.setAlbumId(userListenProcessVo.getAlbumId());
            trackStatMqVo.setTrackId(userListenProcessVo.getTrackId());
            trackStatMqVo.setStatType(SystemConstant.TRACK_STAT_PLAY);
            trackStatMqVo.setCount(1);
            kafkaService.sendMessage(KafkaConstant.QUEUE_TRACK_STAT_UPDATE, JSON.toJSONString(trackStatMqVo));
        }
    }
}
