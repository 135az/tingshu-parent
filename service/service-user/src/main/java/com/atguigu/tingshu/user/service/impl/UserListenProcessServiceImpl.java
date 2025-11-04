package com.atguigu.tingshu.user.service.impl;

import com.atguigu.tingshu.common.util.MongoUtil;
import com.atguigu.tingshu.model.user.UserListenProcess;
import com.atguigu.tingshu.user.service.UserListenProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserListenProcessServiceImpl implements UserListenProcessService {

    @Autowired
    private MongoTemplate mongoTemplate;

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
}
