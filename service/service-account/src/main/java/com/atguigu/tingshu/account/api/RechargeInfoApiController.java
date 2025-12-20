package com.atguigu.tingshu.account.api;

import com.atguigu.tingshu.account.service.RechargeInfoService;
import com.atguigu.tingshu.common.login.GuiGuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.account.RechargeInfo;
import com.atguigu.tingshu.vo.account.RechargeInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@Tag(name = "充值管理")
@RestController
@RequestMapping("api/account/rechargeInfo")
@SuppressWarnings({"rawtypes"})
public class RechargeInfoApiController {

    @Autowired
    private RechargeInfoService rechargeInfoService;

    /**
     * 根据订单号获取充值信息
     *
     * @param orderNo
     * @return
     */
    @GuiGuLogin
    @Operation(summary = "根据订单号获取充值信息")
    @GetMapping("/getRechargeInfo/{orderNo}")
    public Result<RechargeInfo> getRechargeInfo(@PathVariable("orderNo") String orderNo) {
        // 调用服务层方法
        RechargeInfo rechargeInfo = rechargeInfoService.getRechargeInfoByOrderNo(orderNo);
        // 返回对象
        return Result.ok(rechargeInfo);
    }

    /**
     * 给用户充值
     *
     * @param rechargeInfoVo
     * @return
     */
    @GuiGuLogin
    @Operation(summary = "充值")
    @PostMapping("submitRecharge")
    public Result submitRecharge(@RequestBody RechargeInfoVo rechargeInfoVo) {
        //	获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        //	调用充值方法
        String orderNo = this.rechargeInfoService.submitRecharge(rechargeInfoVo, userId);
        //	创建map 集合对象
        HashMap<String, Object> map = new HashMap<>();
        //	存储订单Id
        map.put("orderNo", orderNo);
        //	返回数据
        return Result.ok(map);
    }

}

