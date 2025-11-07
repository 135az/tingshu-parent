package com.atguigu.tingshu.order.api;

import com.atguigu.tingshu.common.login.GuiGuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.order.service.OrderInfoService;
import com.atguigu.tingshu.vo.order.OrderInfoVo;
import com.atguigu.tingshu.vo.order.TradeVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "订单管理")
@RestController
@RequestMapping("api/order/orderInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoApiController {

    @Autowired
    private OrderInfoService orderInfoService;

    /**
     * 确认订单
     *
     * @param tradeVo
     * @return
     */
    @GuiGuLogin
    @Operation(summary = "确认订单")
    @PostMapping("trade")
    public Result<OrderInfoVo> trade(@RequestBody @Validated TradeVo tradeVo) {
        // 获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        //	调用服务层方法
        OrderInfoVo orderInfoVo = orderInfoService.trade(tradeVo, userId);
        //	返回数据
        return Result.ok(orderInfoVo);
    }

    /**
     * 提交订单
     *
     * @param orderInfoVo
     * @return
     */
    @GuiGuLogin
    @Operation(summary = "提交订单")
    @PostMapping("submitOrder")
    public Result<Map<String, Object>> submitOrder(@RequestBody @Validated OrderInfoVo orderInfoVo) {
        //  获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        //  调用服务层方法
        String orderNo = orderInfoService.submitOrder(orderInfoVo, userId);
        Map<String, Object> map = new HashMap();
        map.put("orderNo", orderNo);
        //	返回数据
        return Result.ok(map);
    }

    /**
     * 根据订单号获取订单信息
     *
     * @param orderNo
     * @return
     */
    @GuiGuLogin
    @Operation(summary = "根据订单号获取订单信息")
    @GetMapping("getOrderInfo/{orderNo}")
    public Result<OrderInfo> getOrderInfo(@PathVariable String orderNo) {
        OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
        return Result.ok(orderInfo);
    }

    /**
     * 分页获取用户订单
     *
     * @param page
     * @param limit
     * @return
     */
    @GuiGuLogin
    @Operation(summary = "分页获取用户订单")
    @GetMapping("findUserPage/{page}/{limit}")
    public Result index(
            @Parameter(name = "page", description = "当前页码", required = true)
            @PathVariable Long page,
            @Parameter(name = "limit", description = "每页记录数", required = true)
            @PathVariable Long limit) {
        // 获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        Page<OrderInfo> pageParam = new Page<>(page, limit);
        // 调用服务层方法
        IPage<OrderInfo> pageModel = orderInfoService.findUserPage(pageParam, userId);
        return Result.ok(pageModel);
    }

}

