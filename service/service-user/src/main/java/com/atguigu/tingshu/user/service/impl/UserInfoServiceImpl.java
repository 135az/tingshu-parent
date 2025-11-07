package com.atguigu.tingshu.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.album.client.TrackInfoFeignClient;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.model.user.UserPaidAlbum;
import com.atguigu.tingshu.model.user.UserPaidTrack;
import com.atguigu.tingshu.model.user.UserVipService;
import com.atguigu.tingshu.model.user.VipServiceConfig;
import com.atguigu.tingshu.user.mapper.UserInfoMapper;
import com.atguigu.tingshu.user.mapper.UserPaidAlbumMapper;
import com.atguigu.tingshu.user.mapper.UserPaidTrackMapper;
import com.atguigu.tingshu.user.mapper.UserVipServiceMapper;
import com.atguigu.tingshu.user.mapper.VipServiceConfigMapper;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.user.service.UserPaidTrackService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.atguigu.tingshu.vo.user.UserPaidRecordVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
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

    @Autowired
    private UserPaidTrackMapper userPaidTrackMapper;

    @Autowired
    private TrackInfoFeignClient trackInfoFeignClient;

    @Autowired
    private UserVipServiceMapper userVipServiceMapper;

    @Autowired
    private VipServiceConfigMapper vipServiceConfigMapper;

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

    @Override
    public List<Long> findUserPaidTrackList(Long userId, Long albumId) {
        // 根据用户Id 与 专辑Id 获取到已购买的声音集合
        List<UserPaidTrack> userPaidTrackList = userPaidTrackMapper.selectList(
                new LambdaQueryWrapper<UserPaidTrack>()
                        .eq(UserPaidTrack::getUserId, userId)
                        .eq(UserPaidTrack::getAlbumId, albumId)
        );
        // 获取到已购买的声音集合Id 列表
        List<Long> trackIdList = userPaidTrackList.stream().map(UserPaidTrack::getTrackId).collect(Collectors.toList());
        // 返回集合数据
        return trackIdList;
    }

    @Override
    @Transactional
    public void updateUserPaidRecord(UserPaidRecordVo userPaidRecordVo) {
        // 项目类型 1001-专辑 1002-声音 1003-vip会员
        // 购买专辑
        if (userPaidRecordVo.getItemType().equals(SystemConstant.ORDER_ITEM_TYPE_ALBUM)) {
            // 防止重复消费，如果有记录则直接停止
            long count = userPaidAlbumMapper.selectCount(new LambdaQueryWrapper<UserPaidAlbum>().eq(UserPaidAlbum::getOrderNo, userPaidRecordVo.getOrderNo()));
            if (count > 0) return;
            // 创建用户支付记录对象
            UserPaidAlbum userPaidAlbum = new UserPaidAlbum();
            userPaidAlbum.setUserId(userPaidRecordVo.getUserId());
            userPaidAlbum.setAlbumId(userPaidRecordVo.getItemIdList().get(0));
            userPaidAlbum.setOrderNo(userPaidRecordVo.getOrderNo());
            userPaidAlbumMapper.insert(userPaidAlbum);
            // 购买声音
        } else if (userPaidRecordVo.getItemType().equals(SystemConstant.ORDER_ITEM_TYPE_TRACK)) {
            // 防止重复消费
            long count = userPaidTrackService.count(new LambdaQueryWrapper<UserPaidTrack>().eq(UserPaidTrack::getOrderNo, userPaidRecordVo.getOrderNo()));
            if (count > 0) return;

            Result<TrackInfo> trackInfoResult = trackInfoFeignClient.getTrackInfo(userPaidRecordVo.getItemIdList().get(0));
            Assert.notNull(trackInfoResult, "专辑结果集不能为空");
            TrackInfo trackInfo = trackInfoResult.getData();
            Assert.notNull(trackInfo, "专辑不能为空");
            List<UserPaidTrack> userPaidTrackList = userPaidRecordVo.getItemIdList().stream().map(itemId -> {
                UserPaidTrack userPaidTrack = new UserPaidTrack();
                userPaidTrack.setUserId(userPaidRecordVo.getUserId());
                userPaidTrack.setAlbumId(trackInfo.getAlbumId());
                userPaidTrack.setTrackId(itemId);
                userPaidTrack.setOrderNo(userPaidRecordVo.getOrderNo());
                return userPaidTrack;
            }).collect(Collectors.toList());
            userPaidTrackService.saveBatch(userPaidTrackList);
            // vip 充值购买
        } else if (userPaidRecordVo.getItemType().equals(SystemConstant.ORDER_ITEM_TYPE_VIP)) {
            // 防止重复消费
            long count = userVipServiceMapper.selectCount(new LambdaQueryWrapper<UserVipService>().eq(UserVipService::getOrderNo, userPaidRecordVo.getOrderNo()));
            if (count > 0) return;

            Long itemId = userPaidRecordVo.getItemIdList().get(0);
            VipServiceConfig vipServiceConfig = vipServiceConfigMapper.selectById(itemId);
            UserVipService userVipService = new UserVipService();
            userVipService.setUserId(userPaidRecordVo.getUserId());
            userVipService.setOrderNo(userPaidRecordVo.getOrderNo());
            //  获取当前系统时间
            Date startTime = new Date();
            //  vip 续期：
            //  获取用户信息
            UserInfo userInfo = this.getById(userPaidRecordVo.getUserId());
            // 当前用户属于vip 并 用户的过期时间大于当前系统时间，则需要重新计算vip 的过期时间。
            if (userInfo.getIsVip().intValue() == 1 && userInfo.getVipExpireTime().after(new Date())) {
                startTime = userInfo.getVipExpireTime();
            }
            //  获取过期时间
            Date expireTime = new DateTime(startTime).plusMonths(vipServiceConfig.getServiceMonth()).toDate();
            userVipService.setStartTime(new Date());
            userVipService.setExpireTime(expireTime);
            userVipServiceMapper.insert(userVipService);

            // 更新用户vip信息
            UserInfo userInfoUpt = new UserInfo();
            userInfoUpt.setId(userPaidRecordVo.getUserId());
            userInfoUpt.setIsVip(1);
            userInfoUpt.setVipExpireTime(expireTime);
            this.updateById(userInfoUpt);

        } else {
            log.info("无该项目类型：{}", JSON.toJSONString(userPaidRecordVo));
        }
    }
}
