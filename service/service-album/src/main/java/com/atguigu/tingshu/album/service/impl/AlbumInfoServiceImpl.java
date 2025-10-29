package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumAttributeValueMapper;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.service.AlbumAttributeValueService;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumAttributeValueVo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {

    @Autowired
    private AlbumInfoMapper albumInfoMapper;

    @Autowired
    private AlbumAttributeValueMapper albumAttributeValueMapper;

    @Autowired
    private AlbumStatMapper albumStatMapper;

    @Autowired
    private AlbumAttributeValueService albumAttributeValueService;


    /**
     * 保存专辑信息
     *
     * @param albumInfoVo
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAlbumInfo(AlbumInfoVo albumInfoVo, Long userId) {
        //	创建专辑对象
        AlbumInfo albumInfo = new AlbumInfo();
        //	属性拷贝
        BeanUtils.copyProperties(albumInfoVo, albumInfo);
        //	设置专辑审核状态为：通过
        albumInfo.setStatus(SystemConstant.ALBUM_STATUS_PASS);
        //	设置用户Id
        albumInfo.setUserId(userId);
        //  付费的默认前前5集免费试看
        if (!SystemConstant.ALBUM_PAY_TYPE_FREE.equals(albumInfo.getPayType())) {
            albumInfo.setTracksForFree(5);
        }
        //	保存专辑
        this.save(albumInfo);

        //	保存专辑属性值：
        List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
        //	判断
        if (!CollectionUtils.isEmpty(albumAttributeValueVoList)) {
            // 循环遍历
            List<AlbumAttributeValue> attributeValueList = albumAttributeValueVoList.stream()
                    .map(albumAttributeValueVo -> {
                        // 创建一个实体对象
                        AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();
                        BeanUtils.copyProperties(albumAttributeValueVo, albumAttributeValue);
                        albumAttributeValue.setAlbumId(albumInfo.getId());
                        return albumAttributeValue;
                    }).collect(Collectors.toList());
            // 批量插入
            this.albumAttributeValueService.saveBatch(attributeValueList);
        }

        // 初始化统计数据
        // 播放量
        this.saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_PLAY);
        // 订阅量
        this.saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_SUBSCRIBE);
        // 购买量
        this.saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_BROWSE);
        // 评论数
        this.saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_COMMENT);
    }

    @Override
    public IPage<AlbumListVo> findUserAlbumPage(Page<AlbumListVo> albumInfoPage, AlbumInfoQuery albumInfoQuery) {
        return albumInfoMapper.selectUserAlbumPage(albumInfoPage, albumInfoQuery);
    }

    /**
     * 初始化统计数据
     *
     * @param albumId
     * @param statType
     */
    private void saveAlbumStat(Long albumId, String statType) {
        AlbumStat albumStat = new AlbumStat();
        albumStat.setAlbumId(albumId);
        albumStat.setStatType(statType);
        albumStat.setStatNum(0);
        albumStatMapper.insert(albumStat);
    }
}
