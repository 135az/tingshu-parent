package com.atguigu.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(summary = "根据一级分类Id 查询分类属性数据")
    @GetMapping("findAttribute/{category1Id}")
    public Result<List<BaseAttribute>> findAttribute(@PathVariable Long category1Id) {
        // 调用服务层方法
        return Result.ok(baseCategoryService.findAttributeByCategory1Id(category1Id));
    }

    /**
     * 根据三级分类Id 获取到分类信息
     *
     * @param category3Id
     * @return
     */
    @Operation(summary = "通过三级分类id查询分类信息")
    @GetMapping("getCategoryView/{category3Id}")
    public Result<BaseCategoryView> getCategoryView(@PathVariable Long category3Id) {
        // 调用服务层方法
        BaseCategoryView baseCategoryView = baseCategoryService.getCategoryViewByCategory3Id(category3Id);
        return Result.ok(baseCategoryView);
    }

    /**
     * 根据一级分类Id 获取全部数据
     *
     * @param category1Id
     * @return
     */
    @Operation(summary = "根据一级分类id获取全部分类信息")
    @GetMapping("getBaseCategoryList/{category1Id}")
    public Result<JSONObject> getBaseCategoryList(@PathVariable Long category1Id) {
        JSONObject jsonObject = baseCategoryService.getAllCategoryList(category1Id);
        return Result.ok(jsonObject);
    }

    /**
     * 根据一级分类Id 查询置顶频道页的三级分类列表
     *
     * @param category1Id
     * @return
     */
    @Operation(summary = "获取一级分类下置顶到频道页的三级分类列表")
    @GetMapping("findTopBaseCategory3/{category1Id}")
    public Result<List<BaseCategory3>> findTopBaseCategory3(@PathVariable Long category1Id) {
        //	获取三级分类列表
        List<BaseCategory3> baseCategory3List = baseCategoryService.findTopBaseCategory3ByCategory1Id(category1Id);
        //	返回数据
        return Result.ok(baseCategory3List);
    }

    /**
     * 查询所有的一级分类数据
     *
     * @return
     */
    @Operation(summary = "查询所有的一级分类信息")
    @GetMapping("findAllCategory1")
    public Result<List<BaseCategory1>> findAllCategory1() {
        //	获取所有的一级分类数据
        List<BaseCategory1> baseCategory1List = baseCategoryService.findAllCategory1();
        return Result.ok(baseCategory1List);
    }

}

