package com.atguigu.tingshu.album.client.impl;


import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CategoryFeignClient熔断类
 *
 * @author yjz
 */
@Component
public class CategoryDegradeFeignClient implements CategoryFeignClient {

    @Override
    public Result<BaseCategoryView> getCategoryView(Long category3Id) {
        return null;
    }

    @Override
    public Result<List<BaseCategory3>> findTopBaseCategory3(Long category1Id) {
        return null;
    }
}
