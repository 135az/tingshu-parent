package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.login.GuiGuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理接口")
@RestController
@RequestMapping("api/user/userInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserInfoApiController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 根据用户Id获取用户信息
     *
     * @param userId
     * @return
     */
    @Operation(summary = "根据用户id获取用户信息")
    @GetMapping("getUserInfoVo/{userId}")
    public Result<UserInfoVo> getUserInfoVo(@PathVariable Long userId) {
        // 获取用户信息
        UserInfoVo userInfoVo = userInfoService.getUserInfoVoByUserId(userId);
        return Result.ok(userInfoVo);
    }

    /**
     * 判断用户是否购买声音列表
     *
     * @param albumId
     * @param trackIdList
     * @return
     */
    @GuiGuLogin(required = false)
    @Operation(summary = "判断用户是否购买声音列表")
    @PostMapping("userIsPaidTrack/{albumId}")
    public Result<Map<Long, Integer>> userIsPaidTrack(@PathVariable Long albumId, @RequestBody List<Long> trackIdList) {
        //	获取用户Id
        Long userId = AuthContextHolder.getUserId();
        //	调用服务层方法
        Map<Long, Integer> map = userInfoService.userIsPaidTrack(userId, albumId, trackIdList);
        //	返回map 集合数据
        return Result.ok(map);
    }

    /**
     * 判断用户是否购买过专辑
     *
     * @param albumId
     * @return
     */
    @GuiGuLogin
    @Operation(summary = "判断用户是否购买过专辑")
    @GetMapping("isPaidAlbum/{albumId}")
    public Result<Boolean> isPaidAlbum(@PathVariable Long albumId) {
        // 获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        // 调用服务层方法
        Boolean flag = userInfoService.isPaidAlbum(userId, albumId);
        return Result.ok(flag);
    }

}

