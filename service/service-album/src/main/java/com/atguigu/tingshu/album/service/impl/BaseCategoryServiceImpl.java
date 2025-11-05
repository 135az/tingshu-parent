package com.atguigu.tingshu.album.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.mapper.BaseAttributeMapper;
import com.atguigu.tingshu.album.mapper.BaseCategory1Mapper;
import com.atguigu.tingshu.album.mapper.BaseCategory2Mapper;
import com.atguigu.tingshu.album.mapper.BaseCategory3Mapper;
import com.atguigu.tingshu.album.mapper.BaseCategoryViewMapper;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.cache.GuiGuCache;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.atguigu.tingshu.model.album.BaseCategory2;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class BaseCategoryServiceImpl extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategoryService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private BaseAttributeMapper baseAttributeMapper;


    @Override
    public List<JSONObject> getBaseCategoryList() {
        //	创建集合对象
        List<JSONObject> list = new ArrayList<>();
        //	查看所有分类数据
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //	按照一级分类Id 进行分组 key:一级分类Id， value:一级分类Id 对应的集合数据
        Map<Long, List<BaseCategoryView>> map = baseCategoryViewList.stream()
                .collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //	循环遍历数据
        for (Map.Entry<Long, List<BaseCategoryView>> entry : map.entrySet()) {
            //	获取到一级分类Id
            Long category1Id = entry.getKey();
            //	获取到一级分类Id 对应的集合数据
            List<BaseCategoryView> category1ViewList = entry.getValue();
            // 声明一级分类对象
            JSONObject category1 = new JSONObject();
            category1.put("categoryId", category1Id);
            category1.put("categoryName", category1ViewList.get(0).getCategory1Name());

            //	按照二级分类Id 进行分组
            Map<Long, List<BaseCategoryView>> map2 = category1ViewList.stream()
                    .collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            // 声明二级分类对象集合
            List<JSONObject> category2Child = new ArrayList<>();
            //	循环遍历
            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : map2.entrySet()) {
                //	获取到二级分类Id
                Long category2Id = entry2.getKey();
                //	获取到二级分类Id 对应的集合数据
                List<BaseCategoryView> category2ViewList = entry2.getValue();
                //	创建二级分类对象
                JSONObject category2 = new JSONObject();
                category2.put("categoryId", category2Id);
                category2.put("categoryName", category2ViewList.get(0).getCategory2Name());

                // 循环三级分类数据
                List<JSONObject> category3Child = category2ViewList.stream()
                        .map(baseCategoryView -> {
                            JSONObject category3 = new JSONObject();
                            category3.put("categoryId", baseCategoryView.getCategory3Id());
                            category3.put("categoryName", baseCategoryView.getCategory3Name());
                            return category3;
                        }).collect(Collectors.toList());
                // 将三级数据放入二级里面
                category2.put("categoryChild", category3Child);
                //	将二级分类对象添加到集合中
                category2Child.add(category2);
            }
            // 将三级数据放入二级里面
            category1.put("categoryChild", category2Child);
            //	将一级分类数据放入到集合中。
            list.add(category1);
        }
        return list;
    }

    @Override
    public List<BaseAttribute> findAttributeByCategory1Id(Long category1Id) {
        //	调用mapper层方法
        return baseAttributeMapper.selectBaseAttributeList(category1Id);
    }

    @Override
    @GuiGuCache(prefix = "category:")
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    /**
     * 根据一级分类Id 获取数据
     *
     * @param category1Id
     * @return
     */
    @Override
    public JSONObject getAllCategoryList(Long category1Id) {
        BaseCategory1 baseCategory1 = baseCategory1Mapper.selectById(category1Id);
        // 声明一级分类对象
        JSONObject category1 = new JSONObject();
        category1.put("categoryId", category1Id);
        category1.put("categoryName", baseCategory1.getName());

        // 获取全部分类信息
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(
                new LambdaQueryWrapper<BaseCategoryView>()
                        .eq(BaseCategoryView::getCategory1Id, category1Id)
        );

        // 根据二级分类ID分组转换数据
        Map<Long, List<BaseCategoryView>> category2Map = baseCategoryViewList.stream()
                .collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
        List<JSONObject> category2Child = new ArrayList<>();
        for (Map.Entry<Long, List<BaseCategoryView>> entry2 : category2Map.entrySet()) {
            // 二级分类ID
            Long category2Id = entry2.getKey();
            // 二级分类对应的全部数据（三级数据）
            List<BaseCategoryView> category3List = entry2.getValue();

            // 声明二级分类对象
            JSONObject category2 = new JSONObject();
            category2.put("categoryId", category2Id);
            category2.put("categoryName", category3List.get(0).getCategory2Name());

            // 循环三级分类数据
            List<JSONObject> category3Child = new ArrayList<>();
            category3List.forEach(category3View -> {
                JSONObject category3 = new JSONObject();
                category3.put("categoryId", category3View.getCategory3Id());
                category3.put("categoryName", category3View.getCategory3Name());
                category3Child.add(category3);
            });
            category2Child.add(category2);
            // 将三级数据放入二级里面
            category2.put("categoryChild", category3Child);
        }
        // 将二级数据放入一级里面
        category1.put("categoryChild", category2Child);
        return category1;
    }

    /**
     * 根据一级分类Id 查询置顶频道页的三级分类列表
     *
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory3> findTopBaseCategory3ByCategory1Id(Long category1Id) {
        //	select * from base_category3 where base_category3.category2_id in (101,102,103) and is_top = 1 limit 7;
        //	先根据一级分类Id 找到二级分类集合
        LambdaQueryWrapper<BaseCategory2> baseCategory2LambdaQueryWrapper = new LambdaQueryWrapper<>();
        baseCategory2LambdaQueryWrapper.eq(BaseCategory2::getCategory1Id, category1Id);
        List<BaseCategory2> baseCategory2List = baseCategory2Mapper.selectList(baseCategory2LambdaQueryWrapper);
        List<Long> category2IdList = baseCategory2List.stream().map(BaseCategory2::getId).collect(Collectors.toList());
        //	查询置顶消息，每页显示7条数据；
        LambdaQueryWrapper<BaseCategory3> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BaseCategory3::getCategory2Id, category2IdList).eq(BaseCategory3::getIsTop, 1).last("limit 7");
        return baseCategory3Mapper.selectList(wrapper);
    }

    @Override
    public List<BaseCategory1> findAllCategory1() {
        return baseCategory1Mapper.selectList(null);
    }
}
