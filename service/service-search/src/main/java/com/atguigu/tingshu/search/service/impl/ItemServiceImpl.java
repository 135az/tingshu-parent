package com.atguigu.tingshu.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.album.client.TrackInfoFeignClient;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.search.service.ItemService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.user.client.UserListenProcessFeignClient;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final AlbumInfoFeignClient albumInfoFeignClient;
    private final TrackInfoFeignClient trackInfoFeignClient;
    private final CategoryFeignClient categoryFeignClient;
    private final UserInfoFeignClient userInfoFeignClient;
    private final UserListenProcessFeignClient userListenProcessFeignClient;
    private final RedissonClient redissonClient;
    private final ThreadPoolExecutor threadPoolExecutor;

    @Override
    public Map<String, Object> getItem(Long albumId) {
        // 创建map集合对象
        Map<String, Object> result = new HashMap<>();

        // 远程调用接口之前 提前知道用户访问的专辑id是否存在与布隆过滤器
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstant.ALBUM_BLOOM_FILTER);
        if (!bloomFilter.contains(albumId)) {
            log.error("用户查询专辑不存在：{}", albumId);
            // 查询数据不存在直接返回空对象
            return result;
        }

        // 通过albumId 查询albumInfo
        CompletableFuture<AlbumInfo> albumCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(albumId);
            Assert.notNull(albumInfoResult, "albumInfoResult这个对象不为空");
            AlbumInfo albumInfo = albumInfoResult.getData();
            // 保存albumInfo
            result.put("albumInfo", albumInfo);
            log.info("albumInfo:{}", JSON.toJSONString(albumInfo));
            return albumInfo;
        }, threadPoolExecutor);

        CompletableFuture<Void> albumStatCompletableFuture = CompletableFuture.runAsync(() -> {
            Result<AlbumStatVo> albumStatVoResult = albumInfoFeignClient.getAlbumStatVo(albumId);
            AlbumStatVo albumStatVo = albumStatVoResult.getData();
            result.put("albumStatVo", albumStatVo);
            log.info("albumStatVo:{}", JSON.toJSONString(albumStatVo));
        }, threadPoolExecutor);

        CompletableFuture<Void> baseCategoryViewCompletableFuture = albumCompletableFuture.thenAcceptAsync(albumInfo -> {
            Result<BaseCategoryView> baseCategoryViewResult = categoryFeignClient.getCategoryView(albumInfo.getCategory3Id());
            BaseCategoryView baseCategoryView = baseCategoryViewResult.getData();
            result.put("baseCategoryView", baseCategoryView);
            log.info("baseCategoryView:{}", JSON.toJSONString(baseCategoryView));
        }, threadPoolExecutor);

        CompletableFuture<Void> announcerCompletableFuture = albumCompletableFuture.thenAcceptAsync(albumInfo -> {
            Result<UserInfoVo> userInfoVoResult = userInfoFeignClient.getUserInfoVo(albumInfo.getUserId());
            UserInfoVo userInfoVo = userInfoVoResult.getData();
            result.put("announcer", userInfoVo);
            log.info("announcer:{}", JSON.toJSONString(userInfoVo));
        }, threadPoolExecutor);

        CompletableFuture.allOf(
                albumCompletableFuture,
                albumStatCompletableFuture,
                baseCategoryViewCompletableFuture,
                announcerCompletableFuture
        ).join();
        return result;
    }
}
