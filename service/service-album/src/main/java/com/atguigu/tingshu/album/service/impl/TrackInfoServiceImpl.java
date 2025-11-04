package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.album.AlbumTrackListVo;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {

    @Autowired
    private TrackInfoMapper trackInfoMapper;

    @Autowired
    private VodService vodService;

    @Autowired
    private AlbumInfoService albumInfoService;

    @Autowired
    private TrackStatMapper trackStatMapper;

    @Autowired
    private UserInfoFeignClient userInfoFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTrackInfo(TrackInfoVo trackInfoVo, Long userId) {
        TrackInfo trackInfo = new TrackInfo();
        // 属性拷贝
        BeanUtils.copyProperties(trackInfoVo, trackInfo);
        trackInfo.setStatus(SystemConstant.TRACK_STATUS_PASS);
        trackInfo.setUserId(userId);
        // 获取上一条声音
        TrackInfo preTrackInfo = this.getOne(
                new LambdaQueryWrapper<TrackInfo>()
                        .eq(TrackInfo::getAlbumId, trackInfoVo.getAlbumId())
                        .orderByDesc(TrackInfo::getId)
                        .select(TrackInfo::getOrderNum)
                        .last(" limit 1 ")
        );
        int orderNum = 1;
        if (null != preTrackInfo) {
            orderNum = preTrackInfo.getOrderNum() + 1;
        }
        // 获取流媒体信息.
        TrackMediaInfoVo trackMediaInfo = vodService.getTrackMediaInfo(trackInfoVo.getMediaFileId());
        // 赋值排序值
        trackInfo.setOrderNum(orderNum);
        // 赋值声音
        trackInfo.setMediaSize(trackMediaInfo.getSize());
        trackInfo.setMediaUrl(trackMediaInfo.getMediaUrl());
        trackInfo.setMediaDuration(trackMediaInfo.getDuration());
        trackInfo.setMediaType(trackMediaInfo.getType());

        this.save(trackInfo);

        // 更新专辑声音总数
        AlbumInfo albumInfo = albumInfoService.getById(trackInfo.getAlbumId());
        int includeTrackCount = albumInfo.getIncludeTrackCount() + 1;
        albumInfo.setIncludeTrackCount(includeTrackCount);
        albumInfoService.updateById(albumInfo);

        // 初始化统计数据
        this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_PLAY);
        this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_COLLECT);
        this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_PRAISE);
        this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_COMMENT);
    }

    /**
     * 查询声音专辑列表
     *
     * @param trackListVoPage
     * @param trackInfoQuery
     * @return
     */
    @Override
    public IPage<TrackListVo> findUserTrackPage(Page<TrackListVo> trackListVoPage, TrackInfoQuery trackInfoQuery) {
        //	调用mapper层方法
        return trackInfoMapper.selectUserTrackPage(trackListVoPage, trackInfoQuery);
    }

    @Override
    public void updateTrackInfo(Long id, TrackInfoVo trackInfoVo) {
        //	获取到声音对象
        TrackInfo trackInfo = this.getById(id);
        //    获取传递的fileId
        String mediaFileId = trackInfo.getMediaFileId();
        //	进行属性拷贝
        BeanUtils.copyProperties(trackInfoVo, trackInfo);
        //	获取声音信息 页面传递的fileId 与 数据库的 fileId 不相等就修改
        if (!trackInfoVo.getMediaFileId().equals(mediaFileId)) {
            //	说明已经修改过了.
            TrackMediaInfoVo trackMediaInfoVo = vodService.getTrackMediaInfo(trackInfoVo.getMediaFileId());
            //	判断对象不为空.
            if (null == trackMediaInfoVo) {
                //	抛出异常
                throw new GuiguException(ResultCodeEnum.VOD_FILE_ID_ERROR);
            }
            trackInfo.setMediaUrl(trackMediaInfoVo.getMediaUrl());
            trackInfo.setMediaType(trackMediaInfoVo.getType());
            trackInfo.setMediaDuration(trackMediaInfoVo.getDuration());
            trackInfo.setMediaSize(trackMediaInfoVo.getSize());

            //	修改声音播放量等信息
            // LambdaUpdateWrapper<TrackStat> wrapper = new LambdaUpdateWrapper<>();
            // update track_stat track set track.stat_num = 0 where track.track_id = 51956;
            // wrapper.eq(TrackStat::getTrackId, trackId);
            // TrackStat trackStat = new TrackStat();
            // trackStat.setStatNum(0);
            // trackStatMapper.update(trackStat, wrapper);
        }
        //	修改数据
        this.updateById(trackInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTrackInfo(Long id) {
        // 获取声音对象数据
        TrackInfo trackInfo = this.getById(id);
        // 删除
        this.removeById(id);
        // 删除统计数据
        trackStatMapper.delete(new LambdaQueryWrapper<TrackStat>().eq(TrackStat::getTrackId, id));
        // 更新专辑声音总数
        AlbumInfo albumInfo = this.albumInfoService.getById(trackInfo.getAlbumId());
        int includeTrackCount = albumInfo.getIncludeTrackCount() - 1;
        albumInfo.setIncludeTrackCount(includeTrackCount);
        albumInfoService.updateById(albumInfo);
        // 序号重新计算
        trackInfoMapper.updateTrackNum(trackInfo.getAlbumId(), trackInfo.getOrderNum());

        // 删除声音媒体
        vodService.removeTrack(trackInfo.getMediaFileId());
    }

    /**
     * 根据专辑Id获取声音列表
     * 具体步骤：
     * 根据专辑Id 获取到声音列表 ，将数据封装到 AlbumTrackListVo 类中
     * <p>
     * 1. 用户为空的时候，只要是付费专辑，都需要在页面显示要付款的标识 {**但是，要除去试听集数**} isShowPaidMark=true
     * <p>
     * 付费类型： 0101-免费 0102-vip付费 0103-付费
     * <p>
     * 2. 用户不为空的时候
     * <p>
     * 判断专辑的支付类型
     * <p>
     * 3. 专辑类型属于 vip 免费类型   0102-vip付费
     * <p>
     * 判断用户如果不是vip ，则需要付费
     * <p>
     * 判断用户如果是vip 但是已经过期了, 也需要显示付费
     * <p>
     * 4. 需要付费  0103-付费
     * <p>
     * 需要显示付费
     * <p>
     * 3. 统一处理需要付费业务
     * <p>
     * ​    获取到声音Id列表集合 与 用户购买声音Id集合进行比较  [ 将用户购买的声音存储到map中，key=trackId value = 1或0; 1:表示购买过，0：表示没有购买过
     * <p>
     * 如果声音Id集合不包含购买的声音Id，则将显示为付费，否则不需要付费
     *
     * @param pageParam
     * @param albumId
     * @param userId
     * @return
     */
    @Override
    public IPage<AlbumTrackListVo> findAlbumTrackPage(Page<AlbumTrackListVo> pageParam, Long albumId, Long userId) {
        //	根据专辑Id 获取到声音集合
        IPage<AlbumTrackListVo> pageInfo = trackInfoMapper.selectAlbumTrackPage(pageParam, albumId);

        //	判断用户是否需要付费：0101-免费 0102-vip付费 0103-付费
        AlbumInfo albumInfo = albumInfoService.getById(albumId);
        Assert.notNull(albumInfo, "专辑对象不能为空");
        //	判断用户是否登录
        if (null == userId) {
            //	除免费的专辑都需要显示付费表示
            if (!SystemConstant.ALBUM_PAY_TYPE_FREE.equals(albumInfo.getPayType())) {
                //	处理试听声音，获取需要付费的声音列表
                List<AlbumTrackListVo> albumTrackNeedPaidListVoList = pageInfo.getRecords().stream().filter(albumTrackListVo -> albumTrackListVo.getOrderNum().intValue() > albumInfo.getTracksForFree()).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(albumTrackNeedPaidListVoList)) {
                    albumTrackNeedPaidListVoList.forEach(albumTrackListVo -> {
                        //	显示付费通知
                        albumTrackListVo.setIsShowPaidMark(true);
                    });
                }
            }
        } else {
            //	用户已登录
            //	声明变量是否需要付费，默认不需要付费
            boolean isNeedPaid = false;
            //  vip 付费情况
            if (SystemConstant.ALBUM_PAY_TYPE_VIPFREE.equals(albumInfo.getPayType())) {
                //	获取用户信息
                Result<UserInfoVo> userInfoVoResult = userInfoFeignClient.getUserInfoVo(userId);
                Assert.notNull(userInfoVoResult, "用户信息不能为空");
                UserInfoVo userInfoVo = userInfoVoResult.getData();
                //	1.	VIP 免费,如果不是vip则需要付费，将这个变量设置为true，需要购买
                if (userInfoVo.getIsVip().intValue() == 0) {
                    isNeedPaid = true;
                }
                // 1.1 如果是vip但是vip过期了（定时任务还为更新状态）
                if (userInfoVo.getIsVip().intValue() == 1 && userInfoVo.getVipExpireTime().before(new Date())) {
                    isNeedPaid = true;
                }
            } else if (SystemConstant.ALBUM_PAY_TYPE_REQUIRE.equals(albumInfo.getPayType())) {
                //	2.	付费
                isNeedPaid = true;
            }
            // 需要付费，判断用户是否购买过专辑或声音
            if (isNeedPaid) {
                //	处理试听声音，获取需要付费的声音列表
                List<AlbumTrackListVo> albumTrackNeedPaidListVoList = pageInfo.getRecords().stream().filter(albumTrackListVo -> albumTrackListVo.getOrderNum().intValue() > albumInfo.getTracksForFree()).collect(Collectors.toList());
                //	判断
                if (!CollectionUtils.isEmpty(albumTrackNeedPaidListVoList)) {
                    //	判断用户是否购买该声音
                    //	获取到声音Id 集合列表
                    List<Long> trackIdList = albumTrackNeedPaidListVoList.stream().map(AlbumTrackListVo::getTrackId).collect(Collectors.toList());
                    //	获取用户购买的声音列表
                    Result<Map<Long, Integer>> mapResult = userInfoFeignClient.userIsPaidTrack(albumId, trackIdList);
                    Assert.notNull(mapResult, "声音集合不能为空.");
                    Map<Long, Integer> map = mapResult.getData();
                    Assert.notNull(map, "map集合不能为空.");
                    albumTrackNeedPaidListVoList.forEach(albumTrackListVo -> {
                        //	如果map.get(albumTrackListVo.getTrackId()) == 1 已经购买过，则不显示付费标识;
                        boolean isBuy = map.get(albumTrackListVo.getTrackId()) != 1;
                        albumTrackListVo.setIsShowPaidMark(isBuy);
                    });
                }
            }
        }
        // 返回集合数据
        return pageInfo;
    }

    /**
     * 初始化统计数量
     *
     * @param trackId
     * @param trackType
     */
    private void saveTrackStat(Long trackId, String trackType) {
        TrackStat trackStat = new TrackStat();
        trackStat.setTrackId(trackId);
        trackStat.setStatType(trackType);
        trackStat.setStatNum(0);
        this.trackStatMapper.insert(trackStat);
    }
}
