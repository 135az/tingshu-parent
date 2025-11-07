package com.atguigu.tingshu.order.service;

import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.vo.order.OrderInfoVo;
import com.atguigu.tingshu.vo.order.TradeVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderInfoService extends IService<OrderInfo> {


    /**
     * 确定订单
     *
     * @param tradeVo
     * @param userId
     * @return
     */
    OrderInfoVo trade(TradeVo tradeVo, Long userId);

    /**
     * 提交订单
     *
     * @param orderInfoVo
     * @param userId
     * @return
     */
    String submitOrder(OrderInfoVo orderInfoVo, Long userId);

    /**
     * 保存订单
     *
     * @param orderInfoVo
     * @param userId
     * @param orderNo
     */
    void saveOrder(OrderInfoVo orderInfoVo, Long userId, String orderNo);

    /**
     * 余额支付成功保存交易数据
     *
     * @param orderNo
     */
    void orderPaySuccess(String orderNo);

    /**
     * 取消订单
     *
     * @param orderId
     */
    void orderCancel(long orderId);

    /**
     * 根据订单编号获取订单信息
     *
     * @param orderNo
     * @return
     */
    OrderInfo getOrderInfoByOrderNo(String orderNo);

    /**
     * 获取用户订单列表
     *
     * @param pageParam
     * @param userId
     * @return
     */
    IPage<OrderInfo> findUserPage(Page<OrderInfo> pageParam, Long userId);
}
