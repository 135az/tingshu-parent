package com.atguigu.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Tag(name = "分类管理")
@RestController
@RequestMapping(value = "/api/album/category")
@SuppressWarnings({"unchecked", "rawtypes"})
public class BaseCategoryApiController {

    @Autowired
    private BaseCategoryService baseCategoryService;

    /**
     * 查询所有分类数据
     *
     * @return
     */
    @Operation(tags = "查询所有分类数据")
    @GetMapping("getBaseCategoryList")
    public Result getBaseCategoryList() {
        //	调用服务层的查询分类方法
        List<JSONObject> categoryList = this.baseCategoryService.getBaseCategoryList();
        //	将数据返回给页面使用
        return Result.ok(categoryList);
    }

}

