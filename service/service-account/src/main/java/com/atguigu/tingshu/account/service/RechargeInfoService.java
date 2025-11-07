package com.atguigu.tingshu.account.service;

import com.atguigu.tingshu.model.account.RechargeInfo;
import com.atguigu.tingshu.vo.account.RechargeInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface RechargeInfoService extends IService<RechargeInfo> {

    /**
     * 根据订单号查询充值信息
     *
     * @param orderNo
     * @return
     */
    RechargeInfo getRechargeInfoByOrderNo(String orderNo);

    /**
     * 提交充值
     *
     * @param rechargeInfoVo
     * @param userId
     * @return
     */
    String submitRecharge(RechargeInfoVo rechargeInfoVo, Long userId);

    /**
     * 充值成功
     *
     * @param orderNo
     */
    void rechargePaySuccess(String orderNo);
}
