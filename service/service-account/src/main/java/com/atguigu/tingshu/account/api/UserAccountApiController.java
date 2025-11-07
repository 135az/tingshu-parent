package com.atguigu.tingshu.account.api;

import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.login.GuiGuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.account.UserAccountDetail;
import com.atguigu.tingshu.vo.account.AccountLockResultVo;
import com.atguigu.tingshu.vo.account.AccountLockVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "用户账户管理")
@RestController
@RequestMapping("api/account/userAccount")
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserAccountApiController {

    @Autowired
    private UserAccountService userAccountService;

    /**
     * 获取账户可用余额
     *
     * @return
     */
    @GuiGuLogin
    @Operation(summary = "获取账号可用金额")
    @GetMapping("getAvailableAmount")
    public Result<BigDecimal> getAvailableAmount() {
        //	调用服务层方法
        return Result.ok(userAccountService.getAvailableAmount(AuthContextHolder.getUserId()));
    }

    /**
     * 检查锁定账户金额
     *
     * @param accountLockVo
     * @return
     */
    @GuiGuLogin
    @Operation(summary = "检查与锁定账户金额")
    @PostMapping("checkAndLock")
    public Result<AccountLockResultVo> checkAndLock(@RequestBody AccountLockVo accountLockVo) {
        //	调用服务层方法
        return this.userAccountService.checkAndLock(accountLockVo);
    }

    /**
     * 用户充值记录
     *
     * @param page
     * @param limit
     * @return
     */
    @GuiGuLogin
    @Operation(summary = "获取用户充值记录")
    @GetMapping("/findUserRechargePage/{page}/{limit}")
    public Result findUserRechargePage(
            @Parameter(name = "page", description = "当前页码", required = true)
            @PathVariable Long page,

            @Parameter(name = "limit", description = "每页记录数", required = true)
            @PathVariable Long limit) {
        // 获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        // 构建Page对象
        Page<UserAccountDetail> pageParam = new Page<>(page, limit);
        // 调用服务层方法
        IPage<UserAccountDetail> pageModel = userAccountService.findUserRechargePage(pageParam, userId);
        // 返回数据
        return Result.ok(pageModel);
    }

    @GuiGuLogin
    @Operation(summary = "获取用户消费记录")
    @GetMapping("findUserConsumePage/{page}/{limit}")
    public Result findUserConsumePage(
            @Parameter(name = "page", description = "当前页码", required = true)
            @PathVariable Long page,

            @Parameter(name = "limit", description = "每页记录数", required = true)
            @PathVariable Long limit) {
        // 获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        Page<UserAccountDetail> pageParam = new Page<>(page, limit);
        IPage<UserAccountDetail> pageModel = userAccountService.findUserConsumePage(pageParam, userId);
        return Result.ok(pageModel);
    }

}

