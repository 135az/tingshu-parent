package com.atguigu.tingshu.account.service;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.account.UserAccount;
import com.atguigu.tingshu.model.account.UserAccountDetail;
import com.atguigu.tingshu.vo.account.AccountLockResultVo;
import com.atguigu.tingshu.vo.account.AccountLockVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface UserAccountService extends IService<UserAccount> {

    /**
     * 添加用户账户
     *
     * @param userId
     */
    void addUserAccount(Long userId);

    /**
     * 获取用户账户可用余额
     *
     * @param userId
     * @return
     */
    BigDecimal getAvailableAmount(Long userId);

    /**
     * 根据用户Id 获取到可用余额对象
     *
     * @param userId
     * @return
     */
    UserAccount getUserAccountByUserId(Long userId);

    /**
     * 检查与锁定账户金额
     *
     * @param accountLockVo
     * @return
     */
    Result<AccountLockResultVo> checkAndLock(AccountLockVo accountLockVo);

    /**
     * 减去账户金额
     *
     * @param orderNo
     */
    void minus(String orderNo);

    /**
     * 解锁账户金额
     *
     * @param orderNo
     */
    void unlock(String orderNo);

    /**
     * 添加账户金额
     *
     * @param userId
     * @param amount
     * @param orderNo
     * @param tradeType
     * @param title
     */
    void add(Long userId, BigDecimal amount, String orderNo, String tradeType, String title);

    /**
     * 获取用户充值记录
     *
     * @param pageParam
     * @param userId
     * @return
     */
    IPage<UserAccountDetail> findUserRechargePage(Page<UserAccountDetail> pageParam, Long userId);

    /**
     * 获取用户消费记录
     *
     * @param pageParam
     * @param userId
     * @return
     */
    IPage<UserAccountDetail> findUserConsumePage(Page<UserAccountDetail> pageParam, Long userId);
}
