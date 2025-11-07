package com.atguigu.tingshu.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.account.client.UserAccountFeignClient;
import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.TrackInfoFeignClient;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.service.KafkaService;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.order.OrderDerate;
import com.atguigu.tingshu.model.order.OrderDetail;
import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.model.user.VipServiceConfig;
import com.atguigu.tingshu.order.helper.SignHelper;
import com.atguigu.tingshu.order.mapper.OrderDerateMapper;
import com.atguigu.tingshu.order.mapper.OrderDetailMapper;
import com.atguigu.tingshu.order.mapper.OrderInfoMapper;
import com.atguigu.tingshu.order.service.OrderInfoService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.user.client.VipServiceConfigFeignClient;
import com.atguigu.tingshu.vo.account.AccountLockResultVo;
import com.atguigu.tingshu.vo.account.AccountLockVo;
import com.atguigu.tingshu.vo.order.OrderDerateVo;
import com.atguigu.tingshu.vo.order.OrderDetailVo;
import com.atguigu.tingshu.vo.order.OrderInfoVo;
import com.atguigu.tingshu.vo.order.TradeVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.atguigu.tingshu.vo.user.UserPaidRecordVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private UserInfoFeignClient userInfoFeignClient;

    @Autowired
    private AlbumInfoFeignClient albumInfoFeignClient;

    @Autowired
    private VipServiceConfigFeignClient vipServiceConfigFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TrackInfoFeignClient trackInfoFeignClient;

    @Autowired
    private UserAccountFeignClient userAccountFeignClient;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderDerateMapper orderDerateMapper;

    @Autowired
    private RedissonClient redissonClient;


    @Override
    public OrderInfoVo trade(TradeVo tradeVo, Long userId) {
        // 获取用户信息
        Result<UserInfoVo> userInfoVoResult = userInfoFeignClient.getUserInfoVo(userId);
        Assert.notNull(userInfoVoResult, "用户信息不能为空");
        UserInfoVo userInfoVo = userInfoVoResult.getData();
        Assert.notNull(userInfoVo, "用户信息不能为空");
        //  订单原始金额
        BigDecimal originalAmount = new BigDecimal("0.00");
        //  减免总金额
        BigDecimal derateAmount = new BigDecimal("0.00");
        //  订单总价
        BigDecimal orderAmount = new BigDecimal("0.00");
        //  订单明细集合
        List<OrderDetailVo> orderDetailVoList = new ArrayList<>();
        //  订单减免明细列表
        List<OrderDerateVo> orderDerateVoList = new ArrayList<>();
        //  1001 专辑
        if (tradeVo.getItemType().equals(SystemConstant.ORDER_ITEM_TYPE_ALBUM)) {
            //  判断用户是否购买过专辑
            Result<Boolean> isPaidAlbumResult = this.userInfoFeignClient.isPaidAlbum(tradeVo.getItemId());
            Assert.notNull(isPaidAlbumResult, "返回用户信息结果集不能为空");
            Boolean isPaidAlbum = isPaidAlbumResult.getData();
            Assert.notNull(isPaidAlbum, "用户信息不能为空");
            if (isPaidAlbum) {
                throw new GuiguException(ResultCodeEnum.REPEAT_BUY_ERROR);
            }
            //  根据专辑Id 获取到专辑数据
            Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(tradeVo.getItemId());
            Assert.notNull(albumInfoResult, "返回专辑结果集不能为空");
            AlbumInfo albumInfo = albumInfoResult.getData();
            Assert.notNull(albumInfo, "专辑对象不能为空");
            //  判断当前用户是否是vip
            if (userInfoVo.getIsVip().intValue() == 0) {
                //  非VIP 用户
                originalAmount = albumInfo.getPrice();
                //  判断是否打折 , 不等于-1 就是打折
                if (albumInfo.getDiscount().intValue() != -1) {
                    //  打折 100 8  100*0.8
                    derateAmount = originalAmount.multiply(
                            new BigDecimal("10").subtract(albumInfo.getDiscount())
                    ).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                }
                //  订单总价
                orderAmount = originalAmount.subtract(derateAmount);
            } else {
                // VIP会员
                originalAmount = albumInfo.getPrice();
                // discount=-1,不打折，折扣如：8折 9.5折
                if (albumInfo.getVipDiscount().intValue() != -1) {
                    derateAmount = albumInfo.getPrice().multiply(
                            new BigDecimal(10).subtract(albumInfo.getVipDiscount())
                    ).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                }
                //  订单总价
                orderAmount = originalAmount.subtract(derateAmount);
            }
            //  订单明细
            OrderDetailVo orderDetailVo = new OrderDetailVo();
            orderDetailVo.setItemId(tradeVo.getItemId());
            orderDetailVo.setItemName(albumInfo.getAlbumTitle());
            orderDetailVo.setItemUrl(albumInfo.getCoverUrl());
            orderDetailVo.setItemPrice(albumInfo.getPrice());
            orderDetailVoList.add(orderDetailVo);

            //  添加订单减免
            if (originalAmount.subtract(orderAmount).doubleValue() != 0) {
                OrderDerateVo orderDerateVo = new OrderDerateVo();
                orderDerateVo.setDerateType(SystemConstant.ORDER_DERATE_ALBUM_DISCOUNT);
                orderDerateVo.setDerateAmount(originalAmount.subtract(orderAmount));
                orderDerateVoList.add(orderDerateVo);
            }
        } else if (tradeVo.getItemType().equals(SystemConstant.ORDER_ITEM_TYPE_TRACK)) {
            //  判断用户是否购买过声音
            if (tradeVo.getTrackCount() < 0) {
                throw new GuiguException(ResultCodeEnum.ARGUMENT_VALID_ERROR);
            }
            //  获取下单声音列表
            Result<List<TrackInfo>> trackInfoListResult = trackInfoFeignClient.findPaidTrackInfoList(tradeVo.getItemId(), tradeVo.getTrackCount());
            List<TrackInfo> trackInfoList = trackInfoListResult.getData();
            //  购买声音不支持折扣
            Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(trackInfoList.get(0).getAlbumId());
            AlbumInfo albumInfo = albumInfoResult.getData();
            originalAmount = tradeVo.getTrackCount() > 0 ? albumInfo.getPrice().multiply(new BigDecimal(tradeVo.getTrackCount())) : albumInfo.getPrice();
            //  计算订单总价
            orderAmount = originalAmount;

            //  循环遍历声音集合对象赋值订单明细
            orderDetailVoList = trackInfoList.stream().map(trackInfo -> {
                OrderDetailVo orderDetailVo = new OrderDetailVo();
                orderDetailVo.setItemId(trackInfo.getId());
                orderDetailVo.setItemUrl(trackInfo.getCoverUrl());
                orderDetailVo.setItemPrice(albumInfo.getPrice());
                orderDetailVo.setItemName(trackInfo.getTrackTitle());
                return orderDetailVo;
            }).collect(Collectors.toList());

        } else if (tradeVo.getItemType().equals(SystemConstant.ORDER_ITEM_TYPE_VIP)) {
            //  根据id 获取VIP 服务配置信息
            Result<VipServiceConfig> vipServiceConfigResult = vipServiceConfigFeignClient.getVipServiceConfig(tradeVo.getItemId());
            Assert.notNull(vipServiceConfigResult, "返回vip配置结果集不能为空");
            VipServiceConfig vipServiceConfig = vipServiceConfigResult.getData();
            Assert.notNull(vipServiceConfig, "返回vip配置对象不能为空");

            originalAmount = vipServiceConfig.getPrice();
            derateAmount = vipServiceConfig.getPrice().subtract(vipServiceConfig.getDiscountPrice());
            orderAmount = originalAmount.subtract(derateAmount);

            // 订单明细
            OrderDetailVo orderDetailVo = new OrderDetailVo();
            orderDetailVo.setItemId(tradeVo.getItemId());
            orderDetailVo.setItemName("VIP会员" + vipServiceConfig.getName());
            orderDetailVo.setItemUrl(vipServiceConfig.getImageUrl());
            orderDetailVo.setItemPrice(vipServiceConfig.getDiscountPrice());
            orderDetailVoList.add(orderDetailVo);

            // 添加订单减免
            if (originalAmount.subtract(orderAmount).doubleValue() != 0) {
                OrderDerateVo orderDerateVo = new OrderDerateVo();
                orderDerateVo.setDerateType(SystemConstant.ORDER_DERATE_VIP_SERVICE_DISCOUNT);
                orderDerateVo.setDerateAmount(originalAmount.subtract(orderAmount));
                orderDerateVoList.add(orderDerateVo);
            }
        }
        // 防重：生成一个唯一标识，保存到redis中一份
        String tradeNoKey = "user:trade:" + userId;
        // 定义一个流水号
        String tradeNo = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(tradeNoKey, tradeNo);

        // 构造结果
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        orderInfoVo.setItemType(tradeVo.getItemType());
        orderInfoVo.setOriginalAmount(originalAmount);
        orderInfoVo.setDerateAmount(derateAmount);
        orderInfoVo.setOrderAmount(orderAmount);
        orderInfoVo.setTradeNo(tradeNo);
        orderInfoVo.setOrderDetailVoList(orderDetailVoList);
        orderInfoVo.setOrderDerateVoList(orderDerateVoList);
        orderInfoVo.setTimestamp(SignHelper.getTimestamp());
        //  支付方式默认值 目的是防止用户在前端篡改金额数据
        orderInfoVo.setPayWay(SystemConstant.ORDER_PAY_WAY_WEIXIN);
        //  生成签名
        Map<String, Object> parameterMap = JSON.parseObject(JSON.toJSONString(orderInfoVo), Map.class);
        String sign = SignHelper.getSign(parameterMap);
        orderInfoVo.setSign(sign);
        //  返回对象
        return orderInfoVo;
    }

    @Override
    public String submitOrder(OrderInfoVo orderInfoVo, Long userId) {
        //  校验签名
        Map map = JSON.parseObject(JSON.toJSONString(orderInfoVo), Map.class);
        map.put("payWay", SystemConstant.ORDER_PAY_WAY_WEIXIN);
        SignHelper.checkSign(map);

        //  验证校验好，防止重复提交订单
        String tradeNo = orderInfoVo.getTradeNo();
        if (StringUtils.isEmpty(tradeNo)) {
            //  非法提交
            throw new GuiguException(ResultCodeEnum.ILLEGAL_REQUEST);
        }
        String tradeNoKey = "user:trade:" + userId;
        String script = "if(redis.call('get', KEYS[1]) == ARGV[1]) then return redis.call('del', KEYS[1]) else return 0 end";
        Boolean flag = (Boolean) redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(tradeNoKey), tradeNo);
        if (!flag) {
            // 不能重复提交订单！
            throw new GuiguException(ResultCodeEnum.ORDER_SUBMIT_REPEAT);
        }
        //  3.下单
        //  使用交易号座位订单号，或者重新生成订单号
        String orderNo = UUID.randomUUID().toString().replace("-", "");
        //  支付类型
        if (!SystemConstant.ORDER_PAY_ACCOUNT.equals(orderInfoVo.getPayWay())) {
            //  在线支付
            this.saveOrder(orderInfoVo, userId, orderNo);
        } else {
            try {
                //  余额支付
                //  锁定账户可用金额
                AccountLockVo accountLockVo = new AccountLockVo();
                accountLockVo.setOrderNo(orderNo);
                accountLockVo.setUserId(userId);
                accountLockVo.setAmount(orderInfoVo.getOrderAmount());
                accountLockVo.setContent(orderInfoVo.getOrderDetailVoList().get(0).getItemName());
                //  检查与锁定账户金额
                Result<AccountLockResultVo> result = userAccountFeignClient.checkAndLock(accountLockVo);
                if (200 != result.getCode()) {
                    throw new GuiguException(result.getCode(), result.getMessage());
                }
                //  保存订单
                this.saveOrder(orderInfoVo, userId, orderNo);
                //  支付成功扣减账户金额
                kafkaService.sendMessage(KafkaConstant.QUEUE_ACCOUNT_MINUS, orderNo);
                //  返回订单编号
                return orderNo;

            } catch (GuiguException e) {
                e.printStackTrace();
                // 抛出异常
                throw new GuiguException(e.getCode(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                //  异常手动解锁账户
                kafkaService.sendMessage(KafkaConstant.QUEUE_ACCOUNT_UNLOCK, orderNo);
                // 抛出异常
                throw new GuiguException(ResultCodeEnum.DATA_ERROR);
            }
        }
        return orderNo;
    }

    @Override
    @Transactional
    public void saveOrder(OrderInfoVo orderInfoVo, Long userId, String orderNo) {
        //  创建对象对象
        OrderInfo orderInfo = new OrderInfo();
        //  属性拷贝
        BeanUtils.copyProperties(orderInfoVo, orderInfo);
        orderInfo.setOrderNo(orderNo);
        String orderTitle = orderInfoVo.getOrderDetailVoList().get(0).getItemName();
        orderInfo.setOrderTitle(orderTitle);
        orderInfo.setUserId(userId);
        orderInfo.setOrderStatus(SystemConstant.ORDER_STATUS_UNPAID);
        //  保存订单
        orderInfoMapper.insert(orderInfo);

        // 订单明细
        if (!CollectionUtils.isEmpty(orderInfoVo.getOrderDetailVoList())) {
            orderInfoVo.getOrderDetailVoList().forEach(orderDetailVo -> {
                OrderDetail orderDetail = new OrderDetail();
                BeanUtils.copyProperties(orderDetailVo, orderDetail);
                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insert(orderDetail);
            });
        }

        // 订单减免
        if (!CollectionUtils.isEmpty(orderInfoVo.getOrderDerateVoList())) {
            orderInfoVo.getOrderDerateVoList().forEach(orderDerateVo -> {
                OrderDerate orderDerate = new OrderDerate();
                BeanUtils.copyProperties(orderDerateVo, orderDerate);
                orderDerate.setOrderId(orderInfo.getId());
                orderDerateMapper.insert(orderDerate);
            });
        }

        // 订单支付方式 1101-微信 1102-支付宝 1103-账户余额
        if (SystemConstant.ORDER_PAY_ACCOUNT.equals(orderInfo.getPayWay())) {
            // 余额支付成功保存交易数据
            this.orderPaySuccess(orderNo);
        } else {
            // 发送延迟队列，如果定时未支付，取消订单
            this.sendDelayMessage(orderInfo.getId());
        }
    }

    /**
     * 发送延迟消息
     *
     * @param orderId
     */
    private void sendDelayMessage(Long orderId) {
        try {
            //  创建一个队列
            RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(KafkaConstant.QUEUE_ORDER_CANCEL);
            //  将队列放入延迟队列中
            RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
            //  发送的内容
            delayedQueue.offer(orderId.toString(), KafkaConstant.DELAY_TIME, TimeUnit.SECONDS);
            log.info("添加延时队列成功 ，延迟时间：{}，订单id：{}", KafkaConstant.DELAY_TIME, orderId);
        } catch (Exception e) {
            log.error("添加延时队列失败 ，延迟时间：{}，订单id：{}", KafkaConstant.DELAY_TIME, orderId);
            e.printStackTrace();
        }
    }

    @Override
    public void orderPaySuccess(String orderNo) {
        //  根据 orderNo 修改订单状态数据
        OrderInfo orderInfoUpt = new OrderInfo();
        orderInfoUpt.setOrderStatus(SystemConstant.ORDER_STATUS_PAID);
        this.update(orderInfoUpt, new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));

        //  更新用户支付记录
        OrderInfo orderInfo = this.getOrderInfoByOrderNo(orderNo);

        List<Long> itemIdList = orderInfo.getOrderDetailList().stream().map(OrderDetail::getItemId).collect(Collectors.toList());
        UserPaidRecordVo userPaidRecordVo = new UserPaidRecordVo();
        userPaidRecordVo.setOrderNo(orderNo);
        userPaidRecordVo.setUserId(orderInfo.getUserId());
        userPaidRecordVo.setItemType(orderInfo.getItemType());
        userPaidRecordVo.setItemIdList(itemIdList);
        //  发送用户支付成功消息,监听并更新用户支付记录
        kafkaService.sendMessage(KafkaConstant.QUEUE_USER_PAY_RECORD, JSON.toJSONString(userPaidRecordVo));
    }

    @Override
    public void orderCancel(long orderId) {
        // 设置更新数据
        OrderInfo orderInfoUpt = new OrderInfo();
        orderInfoUpt.setId(orderId);
        orderInfoUpt.setOrderStatus(SystemConstant.ORDER_STATUS_CANCEL);
        // 执行更新方法
        this.updateById(orderInfoUpt);
    }

    /**
     * 根据订单编号获取订单对象
     *
     * @param orderNo
     * @return
     */
    @Override
    public OrderInfo getOrderInfoByOrderNo(String orderNo) {
        //  获取订单对象
        OrderInfo orderInfo = this.getOne(new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderInfo.getId()));

        // 赋值订单明细
        orderInfo.setOrderDetailList(orderDetailList);
        //  获取支付方式：
        orderInfo.setPayWayName(this.getPayWayName(orderInfo.getPayWay()));

        // 返回数据
        return orderInfo;
    }

    @Override
    public IPage<OrderInfo> findUserPage(Page<OrderInfo> pageParam, Long userId) {
        //  调用mapper 层方法
        IPage<OrderInfo> infoIPage = orderInfoMapper.selectUserPage(pageParam, userId);
        infoIPage.getRecords().forEach(item -> {
            //  设置状态名
            item.setOrderStatusName(getOrderStatusName(item.getOrderStatus()));
            item.setPayWayName(getPayWayName(item.getPayWay()));
        });
        return infoIPage;
    }

    /**
     * 根据订单状态获取到订单名称
     *
     * @param orderStatus
     * @return
     */
    private String getOrderStatusName(String orderStatus) {
        //  声明订单状态名称
        String orderStatusName = "";
        //  判断
        if (SystemConstant.ORDER_STATUS_UNPAID.equals(orderStatus)) {
            orderStatusName = "未支付";
        } else if (SystemConstant.ORDER_STATUS_PAID.equals(orderStatus)) {
            orderStatusName = "已支付";
        } else {
            orderStatusName = "已取消";
        }
        //  返回
        return orderStatusName;
    }

    /**
     * 根据payWay 返回支付名称
     *
     * @param payWay
     * @return
     */
    private String getPayWayName(String payWay) {
        //  声明一个对象
        return SystemConstant.ORDER_PAY_WAY_WEIXIN.equals(payWay) ? "微信支付" : "余额支付";
    }
}
