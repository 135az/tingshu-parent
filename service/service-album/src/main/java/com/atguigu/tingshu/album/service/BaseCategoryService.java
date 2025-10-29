package com.atguigu.tingshu.album.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BaseCategoryService extends IService<BaseCategory1> {

    /**
     * 查询所有的分类数据
     *
     * @return
     */
    List<JSONObject> getBaseCategoryList();

    /**
     * 根据一级id获取分类属性
     *
     * @param category1Id
     * @return
     */
    List<BaseAttribute> findAttributeByCategory1Id(Long category1Id);
}
