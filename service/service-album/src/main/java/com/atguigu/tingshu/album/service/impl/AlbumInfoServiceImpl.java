package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumAttributeValueMapper;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.service.AlbumAttributeValueService;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.cache.GuiGuCache;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.service.KafkaService;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumAttributeValueVo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


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

        //	发送上架消息
        if ("1".equals(albumInfo.getIsOpen())) {
            this.kafkaService.sendMessage(KafkaConstant.QUEUE_ALBUM_UPPER, String.valueOf(albumInfo.getId()));
        }
    }

    /**
     * 查询专辑列表
     *
     * @param albumInfoPage
     * @param albumInfoQuery
     * @return
     */
    @Override
    public IPage<AlbumListVo> findUserAlbumPage(Page<AlbumListVo> albumInfoPage, AlbumInfoQuery albumInfoQuery) {
        return albumInfoMapper.selectUserAlbumPage(albumInfoPage, albumInfoQuery);
    }

    /**
     * 删除专辑信息
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAlbumInfoById(Long id) {
        // 删除专辑信息
        this.removeById(id);
        // 删除专辑属性值
        albumAttributeValueMapper.delete(
                new LambdaQueryWrapper<AlbumAttributeValue>()
                        .eq(AlbumAttributeValue::getAlbumId, id)
        );
        // 删除专辑统计数据
        albumStatMapper.delete(
                new LambdaQueryWrapper<AlbumStat>()
                        .eq(AlbumStat::getAlbumId, id)
        );
        // 下架
        kafkaService.sendMessage(KafkaConstant.QUEUE_ALBUM_LOWER, String.valueOf(id));
    }

    /**
     * 根据Id查询专辑信息
     *
     * @param id
     * @return
     */
    @GuiGuCache(prefix = RedisConstant.ALBUM_INFO_PREFIX)
    @Override
    public AlbumInfo getAlbumInfoById(Long id) {
        return getAlbumInfoDB(id);
    }

    /**
     * 从数据库中根据Id查询专辑信息
     *
     * @param id
     * @return
     */
    @Nullable
    private AlbumInfo getAlbumInfoDB(Long id) {
        // 查询专辑信息
        AlbumInfo albumInfo = this.getById(id);
        // 获取专辑属性值
        if (albumInfo != null) {
            albumInfo.setAlbumAttributeValueVoList(
                    albumAttributeValueMapper.selectList(
                            new LambdaQueryWrapper<AlbumAttributeValue>()
                                    .eq(AlbumAttributeValue::getAlbumId, id)
                    )
            );
        }
        return albumInfo;
    }

    /**
     * 修改专辑信息
     *
     * @param id
     * @param albumInfoVo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAlbumInfo(Long id, AlbumInfoVo albumInfoVo) {
        AlbumInfo albumInfo = this.getById(id);
        BeanUtils.copyProperties(albumInfoVo, albumInfo);
        // 根据id 修改专辑信息
        this.updateById(albumInfo);
        // 先删除专辑属性值
        albumAttributeValueMapper.delete(
                new LambdaQueryWrapper<AlbumAttributeValue>()
                        .eq(AlbumAttributeValue::getAlbumId, id)
        );
        // 添加专辑属性值
        List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
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
        //	更新上架下架
        if ("1".equals(albumInfo.getIsOpen())) {
            kafkaService.sendMessage(KafkaConstant.QUEUE_ALBUM_UPPER, String.valueOf(albumInfo.getId()));
        } else {
            kafkaService.sendMessage(KafkaConstant.QUEUE_ALBUM_LOWER, String.valueOf(albumInfo.getId()));
        }
    }

    @Override
    public List<AlbumInfo> findUserAllAlbumList(Long userId) {
        // 默认查看第一页
        Page<AlbumInfo> albumInfoPage = new Page<>(1, 10);
        // 设置查询条件
        LambdaQueryWrapper<AlbumInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlbumInfo::getUserId, userId);
        queryWrapper.select(AlbumInfo::getId, AlbumInfo::getAlbumTitle);
        queryWrapper.orderByDesc(AlbumInfo::getId);
        // 返回查询结果
        return albumInfoMapper.selectPage(albumInfoPage, queryWrapper).getRecords();
    }

    @Override
    public List<AlbumAttributeValue> findAlbumAttributeValueByAlbumId(Long albumId) {
        return albumAttributeValueMapper.selectList(
                new LambdaQueryWrapper<AlbumAttributeValue>()
                        .eq(AlbumAttributeValue::getAlbumId, albumId)
        );
    }

    @Override
    @GuiGuCache(prefix = "albumStat:")
    public AlbumStatVo getAlbumStatVoByAlbumId(Long albumId) {
        return albumInfoMapper.selectAlbumStat(albumId);
    }

    @Override
    public void updateStat(Long albumId, String statType, Integer count) {
        //	更新数据
        albumInfoMapper.updateStat(albumId, statType, count);
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
