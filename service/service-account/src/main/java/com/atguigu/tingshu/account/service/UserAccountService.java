package com.atguigu.tingshu.account.service;

import com.atguigu.tingshu.model.account.UserAccount;
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
}
