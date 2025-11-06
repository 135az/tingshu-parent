package com.atguigu.tingshu.user.service;

import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {

    /**
     * 根据用户id查询用户信息
     *
     * @param userId
     * @return
     */
    UserInfoVo getUserInfoVoByUserId(Long userId);

    /**
     * 判断用户是否购买声音列表
     *
     * @param userId
     * @param albumId
     * @param trackIdList
     * @return
     */
    Map<Long, Integer> userIsPaidTrack(Long userId, Long albumId, List<Long> trackIdList);

    /**
     * 判断用户是否购买专辑
     *
     * @param userId
     * @param albumId
     * @return
     */
    Boolean isPaidAlbum(Long userId, Long albumId);
}
