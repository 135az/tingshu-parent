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
        //  获取专辑信息
        Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(albumId);
        AlbumInfo albumInfo = albumInfoResult.getData();
        Assert.notNull(albumInfo, "专辑为空");
        //  获取专辑属性信息
        Result<List<AlbumAttributeValue>> albumAttributeValueResult = albumInfoFeignClient.findAlbumAttributeValue(albumId);
        List<AlbumAttributeValue> albumAttributeValueList = albumAttributeValueResult.getData();
        Assert.notNull(albumAttributeValueList, "专辑属性为空");
        //  根据三级分类Id 获取到分类数据
        Result<BaseCategoryView> baseCategoryViewResult = categoryFeignClient.getCategoryView(albumInfo.getCategory3Id());
        BaseCategoryView baseCategoryView = baseCategoryViewResult.getData();
        Assert.notNull(baseCategoryView, "分类为空");
        //  根据用户Id 获取到用户信息
        Result<UserInfoVo> userInfoVoResult = userInfoFeignClient.getUserInfoVo(albumInfo.getUserId());
        UserInfoVo userInfoVo = userInfoVoResult.getData();
        Assert.notNull(userInfoVo, "用户信息为空");

        //  创建索引库对象
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        BeanUtils.copyProperties(albumInfo, albumInfoIndex);

        //  赋值属性值信息
        if (!CollectionUtils.isEmpty(albumAttributeValueList)) {
            List<AttributeValueIndex> attributeValueIndexList = albumAttributeValueList.stream().map(albumAttributeValue -> {
                AttributeValueIndex attributeValueIndex = new AttributeValueIndex();
                BeanUtils.copyProperties(albumAttributeValue, attributeValueIndex);
                return attributeValueIndex;
            }).collect(Collectors.toList());
            //  保存数据
            albumInfoIndex.setAttributeValueIndexList(attributeValueIndexList);
        }
        //  赋值分类数据
        albumInfoIndex.setCategory1Id(baseCategoryView.getCategory1Id());
        albumInfoIndex.setCategory2Id(baseCategoryView.getCategory2Id());
        albumInfoIndex.setCategory3Id(baseCategoryView.getCategory3Id());
        //  赋值主播名称
        albumInfoIndex.setAnnouncerName(userInfoVo.getNickname());

        // 更新统计量与得分，默认随机，方便测试
        int num1 = new Random().nextInt(1000);
        int num2 = new Random().nextInt(100);
        int num3 = new Random().nextInt(50);
        int num4 = new Random().nextInt(300);
        albumInfoIndex.setPlayStatNum(num1);
        albumInfoIndex.setSubscribeStatNum(num2);
        albumInfoIndex.setBuyStatNum(num3);
        albumInfoIndex.setCommentStatNum(num4);
        double hotScore = num1 * 0.2 + num2 * 0.3 + num3 * 0.4 + num4 * 0.1;
        //  设置热度排名
        albumInfoIndex.setHotScore(hotScore);
        //  保存商品上架信息
        albumIndexRepository.save(albumInfoIndex);
    }

    @Override
    public void lowerAlbum(Long albumId) {
        albumIndexRepository.deleteById(albumId);
    }
}
