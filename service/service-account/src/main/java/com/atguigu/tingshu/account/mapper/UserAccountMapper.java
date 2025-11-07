package com.atguigu.tingshu.account.mapper;

import com.atguigu.tingshu.model.account.UserAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    /**
     * 检查用户账户余额
     *
     * @param userId
     * @param amount
     * @return
     */
    UserAccount check(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /**
     * 锁定用户账户余额
     *
     * @param userId
     * @param amount
     * @return
     */
    int lock(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /**
     * 减去用户账户余额
     *
     * @param userId
     * @param amount
     * @return
     */
    int minus(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /**
     * 解锁用户账户余额
     *
     * @param userId
     * @param amount
     * @return
     */
    int unLock(Long userId, BigDecimal amount);
}
