package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
