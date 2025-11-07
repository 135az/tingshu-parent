package com.atguigu.tingshu.account.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.account.mapper.UserAccountDetailMapper;
import com.atguigu.tingshu.account.mapper.UserAccountMapper;
import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.model.account.UserAccount;
import com.atguigu.tingshu.model.account.UserAccountDetail;
import com.atguigu.tingshu.vo.account.AccountLockResultVo;
import com.atguigu.tingshu.vo.account.AccountLockVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserAccountDetailMapper userAccountDetailMapper;

    /**
     * 添加用户账户
     *
     * @param userId
     */
    @Override
    public void addUserAccount(Long userId) {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        userAccountMapper.insert(userAccount);
    }

    @Override
    public BigDecimal getAvailableAmount(Long userId) {
        //	根据用户Id 获取到用户余额对象
        UserAccount userAccount = this.getUserAccountByUserId(userId);
        return userAccount.getAvailableAmount();
    }

    @Override
    public UserAccount getUserAccountByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUserId, userId));
    }

    @Override
    public Result<AccountLockResultVo> checkAndLock(AccountLockVo accountLockVo) {
        //	检查key
        String key = "checkAndLock:" + accountLockVo.getOrderNo();
        //	数据key
        String dataKey = "account:lock:" + accountLockVo.getOrderNo();
        //	防止重复请求
        boolean isExist = redisTemplate.opsForValue().setIfAbsent(key, accountLockVo.getOrderNo(), 1, TimeUnit.HOURS);
        //	isExist = true; 说明第一次执行，如果 isExist=false; 说明不是第一次执行
        if (!isExist) {
            //	获取锁定key对应的数据
            String data = this.redisTemplate.opsForValue().get(dataKey).toString();
            if (!StringUtils.isEmpty(data)) {
                //	获取到计算的数据
                AccountLockResultVo accountLockResultVo = JSONObject.parseObject(data, AccountLockResultVo.class);
                return Result.ok(accountLockResultVo);
            } else {
                // 还未计算出结果就再次提交
                return Result.build(null, ResultCodeEnum.ACCOUNT_LOCK_REPEAT);
            }
        }

        //	核对账户可用金额并锁定账户数据(悲观锁)；查询返回的是满足要求的账户
        UserAccount userAccount = userAccountMapper.check(accountLockVo.getUserId(), accountLockVo.getAmount());
        //	判断
        if (null == userAccount) {
            // 账户可用金额不足
            redisTemplate.delete(key);
            return Result.build(null, ResultCodeEnum.ACCOUNT_LESS);
        }

        //	锁定账户金额
        int lock = userAccountMapper.lock(accountLockVo.getUserId(), accountLockVo.getAmount());
        if (lock == 0) {// 锁定失败
            // 解除去重
            this.redisTemplate.delete(key);
            return Result.build(null, ResultCodeEnum.ACCOUNT_LOCK_ERROR);
        }

        //	添加账户明细
        this.log(accountLockVo.getUserId(), "锁定：" + accountLockVo.getContent(), SystemConstant.ACCOUNT_TRADE_TYPE_LOCK, accountLockVo.getAmount(), "lock:" + accountLockVo.getOrderNo());

        // 返回锁定对象
        AccountLockResultVo accountLockResultVo = new AccountLockResultVo();
        accountLockResultVo.setUserId(accountLockVo.getUserId());
        accountLockResultVo.setAmount(accountLockVo.getAmount());
        accountLockResultVo.setContent(accountLockVo.getContent());
        // 如果账户锁定成功的情况下，需要缓存锁定信息到redis。以方便将来解锁账户金额 或者 减账户金额
        redisTemplate.opsForValue().set(dataKey, JSON.toJSONString(accountLockResultVo), 1, TimeUnit.HOURS);
        //	返回数据
        return Result.ok(accountLockResultVo);
    }

    @Override
    @Transactional
    public void minus(String orderNo) {
        // 防止重复消费
        String key = "minus:" + orderNo;
        // 数据key
        String dataKey = "account:lock:" + orderNo;

        // 业务去重，防止重复消费
        Boolean isExist = redisTemplate.opsForValue().setIfAbsent(key, orderNo, 1, TimeUnit.HOURS);
        if (!isExist) {
            return;
        }

        // 获取锁定库存的缓存信息
        String data = (String) this.redisTemplate.opsForValue().get(dataKey);
        if (StringUtils.isEmpty(data)) {
            return;
        }

        // 扣减账户金额
        AccountLockResultVo accountLockResultVo = JSONObject.parseObject(data, AccountLockResultVo.class);
        int minus = userAccountMapper.minus(accountLockResultVo.getUserId(), accountLockResultVo.getAmount());
        if (minus == 0) {
            // 解除去重
            this.redisTemplate.delete(key);
            throw new GuiguException(ResultCodeEnum.ACCOUNT_MINUSLOCK_ERROR);
        }
        // 记录日志
        this.log(accountLockResultVo.getUserId(), accountLockResultVo.getContent(), SystemConstant.ACCOUNT_TRADE_TYPE_MINUS, accountLockResultVo.getAmount(), orderNo);

        // 解锁账户金额之后，删除锁定缓存。以防止重复解锁
        this.redisTemplate.delete(dataKey);
    }

    @Override
    @Transactional
    public void unlock(String orderNo) {
        // 防止重复消费
        String key = "unlock:" + orderNo;
        // 数据key
        String dataKey = "account:lock:" + orderNo;

        // 业务去重，防止重复消费
        Boolean isExist = redisTemplate.opsForValue().setIfAbsent(key, orderNo, 1, TimeUnit.HOURS);
        if (!isExist) {
            return;
        }

        // 获取锁定库存的缓存信息
        String data = (String) this.redisTemplate.opsForValue().get(dataKey);
        if (StringUtils.isEmpty(data)) {
            return;
        }

        // 解锁账户金额
        AccountLockResultVo accountLockResultVo = JSONObject.parseObject(data, AccountLockResultVo.class);
        // 调用解锁方法
        int unLock = userAccountMapper.unLock(accountLockResultVo.getUserId(), accountLockResultVo.getAmount());
        if (unLock == 0) {
            // 解除去重
            this.redisTemplate.delete(key);
            throw new GuiguException(ResultCodeEnum.ACCOUNT_UNLOCK_ERROR);
        }
        // 记录日志
        this.log(accountLockResultVo.getUserId(), "解锁：" + accountLockResultVo.getContent(), SystemConstant.ACCOUNT_TRADE_TYPE_UNLOCK, accountLockResultVo.getAmount(), "unlock:" + orderNo);

        // 解锁账户金额之后，删除锁定缓存。以防止重复解锁
        this.redisTemplate.delete(dataKey);
    }

    /**
     * 记录账户明细
     *
     * @param userId
     * @param title
     * @param tradeType
     * @param amount
     * @param orderNo
     */
    private void log(Long userId, String title, String tradeType, BigDecimal amount, String orderNo) {
        // 添加账户明细
        UserAccountDetail userAccountDetail = new UserAccountDetail();
        userAccountDetail.setUserId(userId);
        userAccountDetail.setTitle(title);
        userAccountDetail.setTradeType(tradeType);
        userAccountDetail.setAmount(amount);
        userAccountDetail.setOrderNo(orderNo);
        //	添加数据
        userAccountDetailMapper.insert(userAccountDetail);
    }
}
