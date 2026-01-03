package com.atguigu.tingshu.user.api;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.login.GuiGuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.service.KafkaService;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 微信登录接口
 *
 * @author yanjiazheng
 */
@Tag(name = "微信授权登录接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/wxLogin")
@SuppressWarnings({"unchecked", "rawtypes"})
@Slf4j
public class WxLoginApiController {

    private final UserInfoService userInfoService;
    private final RedisTemplate redisTemplate;
    private final WxMaService wxMaService;
    private final KafkaService kafkaService;

    @Operation(summary = "小程序授权登录")
    @GetMapping("/wxLogin/{code}")
    public Result wxLogin(@PathVariable String code) throws WxErrorException {
        //  获取openId
        WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
        //  学生端可以直接使用 wxcc651fcbab275e33 教师的 OPENID 即可
        String openId = sessionInfo.getOpenid();
        UserInfo userInfo = userInfoService.getOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getWxOpenId, openId));
        //  如果数据库中没有这个对象
        if (null == userInfo) {
            //  创建对象
            userInfo = new UserInfo();
            //  赋值用户昵称
            userInfo.setNickname("听友" + System.currentTimeMillis());
            //  赋值用户头像图片
            userInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            //  赋值wxOpenId
            userInfo.setWxOpenId(openId);
            //  保存用户信息
            userInfoService.save(userInfo);
            //  初始化账户信息
            kafkaService.sendMessage(KafkaConstant.QUEUE_USER_REGISTER, userInfo.getId().toString());
        }
        //  创建 token
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        //  将这两个数据存储到缓存中。
        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token, userInfo, RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);

        //  将这个数据存储到map中并返回
        HashMap<String, Object> map = new HashMap<>();
        map.put("token", token);
        //  返回数据
        return Result.ok(map);
    }

    /**
     * 根据用户Id获取到用户数据
     *
     * @return
     */
    @GuiGuLogin
    @Operation(summary = "获取登录信息")
    @GetMapping("getUserInfo")
    public Result getUserInfo() {
        //  获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        //  调用服务层方法
        UserInfoVo userInfoVo = userInfoService.getUserInfoVoByUserId(userId);
        //  返回数据
        return Result.ok(userInfoVo);
    }

    /**
     * 更新用户信息
     *
     * @param userInfoVo
     * @return
     */
    @GuiGuLogin
    @Operation(summary = "更新用户信息")
    @PostMapping("updateUser")
    public Result updateUser(@RequestBody UserInfoVo userInfoVo) {
        //  获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setNickname(userInfoVo.getNickname());
        userInfo.setAvatarUrl(userInfoVo.getAvatarUrl());

        //  执行更新方法
        userInfoService.updateById(userInfo);
        return Result.ok();
    }


}
