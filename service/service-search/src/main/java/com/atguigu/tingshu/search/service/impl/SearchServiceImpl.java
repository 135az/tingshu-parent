package com.atguigu.tingshu.search.service.impl;

import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.search.repository.AlbumIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/**
 * 专辑信息索引服务实现类
 *
 * @author yjz
 */
@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class SearchServiceImpl implements SearchService {

    @Autowired
    private AlbumInfoFeignClient albumInfoFeignClient;

    @Autowired
    private CategoryFeignClient categoryFeignClient;

    @Autowired
    private UserInfoFeignClient userInfoFeignClient;

    @Autowired
    private AlbumIndexRepository albumIndexRepository;

    @Override
    public void upperAlbum(Long albumId) {
        //  专辑上架时都应该给 AlbumInfoIndex
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        //  singleSave(albumId);
        //  创建异步编排对象
        CompletableFuture<AlbumInfo> albumInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //  远程获取数据
            Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(albumId);
            //  判断
            Assert.notNull(albumInfoResult, "albumInfoResult这个对象不为空");
            AlbumInfo albumInfo = albumInfoResult.getData();
            //  判断
            Assert.notNull(albumInfo, "albumInfo 这个对象不为空");
            //  给albumInfoIndex 对象中的专辑部分数据进行赋值.
            BeanUtils.copyProperties(albumInfo, albumInfoIndex);
            //  返回对象
            return albumInfo;
        });

        //  获取分类数据：需要使用三级分类Id  albumInfo.getCategory3Id();
        CompletableFuture<Void> categoryCompletableFuture = albumInfoCompletableFuture.thenAcceptAsync(albumInfo -> {
            Result<BaseCategoryView> categoryViewResult = categoryFeignClient.getCategoryView(albumInfo.getCategory3Id());
            BaseCategoryView baseCategoryView = categoryViewResult.getData();
            //  赋值：
            albumInfoIndex.setCategory1Id(baseCategoryView.getCategory1Id());
            albumInfoIndex.setCategory2Id(baseCategoryView.getCategory2Id());
            albumInfoIndex.setCategory3Id(baseCategoryView.getCategory3Id());
        });

        //  获取属性集合
        CompletableFuture<Void> attributeCompletableFuture = CompletableFuture.runAsync(() -> {
            //  获取专辑属性信息.
            Result<List<AlbumAttributeValue>> albumAttributeValueResult = albumInfoFeignClient.findAlbumAttributeValue(albumId);
            List<AlbumAttributeValue> albumAttributeValueList = albumAttributeValueResult.getData();
            //  遍历数据
            if (!CollectionUtils.isEmpty(albumAttributeValueList)) {
                //  获取到当前albumAttributeValue 对象中的  attributeId  valueId 给  AttributeValueIndex 这个对象 赋值
                List<AttributeValueIndex> attributeValueIndexList = albumAttributeValueList.stream()
                        .map(albumAttributeValue -> {
                            //  获取  attributeId  valueId 给  AttributeValueIndex 这个对象 赋值
                            AttributeValueIndex attributeValueIndex = new AttributeValueIndex();
                            attributeValueIndex.setAttributeId(albumAttributeValue.getAttributeId());
                            attributeValueIndex.setValueId(albumAttributeValue.getValueId());
                            return attributeValueIndex;
                        }).collect(Collectors.toList());
                //  赋值：
                albumInfoIndex.setAttributeValueIndexList(attributeValueIndexList);
            }
        });

        //  获取主播数据
        CompletableFuture<Void> userCompletableFuture = albumInfoCompletableFuture.thenAcceptAsync(albumInfo -> {
            //  赋值主播名称.album_info.user_id
            Result<UserInfoVo> userInfoVoResult = userInfoFeignClient.getUserInfoVo(albumInfo.getUserId());
            UserInfoVo userInfoVo = userInfoVoResult.getData();
            //  赋值主播
            albumInfoIndex.setAnnouncerName(userInfoVo.getNickname());
        });

        //  赋值播放量，，订阅量，购买量, 评论数
        int playStatNum = new Random().nextInt(10000);
        int subscribeStatNum = new Random().nextInt(100);
        int buyStatNum = new Random().nextInt(1000);
        int commentStatNum = new Random().nextInt(100);
        albumInfoIndex.setPlayStatNum(playStatNum);
        albumInfoIndex.setSubscribeStatNum(subscribeStatNum);
        albumInfoIndex.setBuyStatNum(buyStatNum);
        albumInfoIndex.setCommentStatNum(commentStatNum);

        //  随机生成一个热度值。
        double hotScore = new Random().nextInt(100);
        albumInfoIndex.setHotScore(hotScore);

        //  多任务组合：
        CompletableFuture.allOf(
                albumInfoCompletableFuture,
                attributeCompletableFuture,
                categoryCompletableFuture,
                userCompletableFuture).join();
        //  保存数据：
        albumIndexRepository.save(albumInfoIndex);
    }

    @Override
    public void lowerAlbum(Long albumId) {
        albumIndexRepository.deleteById(albumId);
    }
}
