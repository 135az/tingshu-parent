package com.atguigu.tingshu.user.service.impl;

import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.model.user.UserPaidAlbum;
import com.atguigu.tingshu.model.user.UserPaidTrack;
import com.atguigu.tingshu.user.mapper.UserInfoMapper;
import com.atguigu.tingshu.user.mapper.UserPaidAlbumMapper;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.user.service.UserPaidTrackService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserPaidAlbumMapper userPaidAlbumMapper;

    @Autowired
    private UserPaidTrackService userPaidTrackService;

    @Override
    public UserInfoVo getUserInfoVoByUserId(Long userId) {
        UserInfo userInfo = this.getById(userId);
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        return userInfoVo;
    }

    /**
     * 判断用户是否付费
     * 具体步骤：
     * <p>
     * 根据用户Id，专辑Id 获取到用户付款专辑对象
     * <p>
     * 1. 如果对象不为空，说明购买过专辑，则设置在map集合中 声音Id为1  map.put(trackId,1); 并返回集合map
     * 2. 如果对象为空，说明用户没有购买过专辑，但是，需要查询当前用户Id是否购买过声音Id集合中的声音
     * 1. 判断当前专辑声音Id集合列表 中是否包含已购买的声音Id。
     * 1. true: 设置为 trackId 1
     * 2. false: 设置为 trackId 0
     *
     * @param userId
     * @param albumId
     * @param trackIdList
     * @return
     */
    @Override
    public Map<Long, Integer> userIsPaidTrack(Long userId, Long albumId, List<Long> trackIdList) {
        //	根据UserId,albumId 获取到用户付款专辑对象
        LambdaQueryWrapper<UserPaidAlbum> userPaidAlbumLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userPaidAlbumLambdaQueryWrapper.eq(UserPaidAlbum::getUserId, userId).eq(UserPaidAlbum::getAlbumId, albumId);
        UserPaidAlbum userPaidAlbum = userPaidAlbumMapper.selectOne(userPaidAlbumLambdaQueryWrapper);
        //	判断
        if (null != userPaidAlbum) {
            //	创建一个map 集合
            HashMap<Long, Integer> map = new HashMap<>();
            //	如果查询到对应的专辑购买记录，则默认将声音Id 赋值为 1
            trackIdList.forEach(trackId -> {
                map.put(trackId, 1);
            });
            return map;
        } else {
            //	根据用户Id 与专辑Id 查询用户购买声音记录
            LambdaQueryWrapper<UserPaidTrack> userPaidTrackLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userPaidTrackLambdaQueryWrapper.eq(UserPaidTrack::getUserId, userId).in(UserPaidTrack::getTrackId, trackIdList);
            List<UserPaidTrack> userPaidTrackList = userPaidTrackService.list(userPaidTrackLambdaQueryWrapper);
            //	获取到用户购买声音Id 集合
            List<Long> userPaidTrackIdList = userPaidTrackList.stream().map(UserPaidTrack::getTrackId).collect(Collectors.toList());
            //	创建一个map 集合
            HashMap<Long, Integer> map = new HashMap<>();
            trackIdList.forEach(trackId -> {
                if (userPaidTrackIdList.contains(trackId)) {
                    //	用户已购买声音
                    map.put(trackId, 1);
                } else {
                    map.put(trackId, 0);
                }
            });
            return map;
        }
    }

    @Override
    public Boolean isPaidAlbum(Long userId, Long albumId) {
        // 根据用户Id 与专辑Id 查询是否有记录
        Long count = userPaidAlbumMapper.selectCount(new LambdaQueryWrapper<UserPaidAlbum>()
                .eq(UserPaidAlbum::getUserId, userId)
                .eq(UserPaidAlbum::getAlbumId, albumId)
        );
        return count > 0;
    }
}
