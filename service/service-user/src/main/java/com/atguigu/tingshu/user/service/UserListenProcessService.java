package com.atguigu.tingshu.user.service;

import java.math.BigDecimal;

public interface UserListenProcessService {

    /**
     * 获取用户当前播放的音频的播放进度
     *
     * @param userId
     * @param trackId
     * @return
     */
    BigDecimal getTrackBreakSecond(Long userId, Long trackId);
}
