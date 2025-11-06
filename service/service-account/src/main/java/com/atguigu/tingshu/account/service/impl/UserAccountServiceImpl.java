package com.atguigu.tingshu.account.service.impl;

import com.atguigu.tingshu.account.mapper.UserAccountMapper;
import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.model.account.UserAccount;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Autowired
    private UserAccountMapper userAccountMapper;

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
}
