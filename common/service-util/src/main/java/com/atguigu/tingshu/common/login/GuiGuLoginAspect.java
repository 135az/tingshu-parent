package com.atguigu.tingshu.common.login;

import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.user.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author yjz
 * @Date 2025/10/29 20:25
 * @Description
 *
 */
@Aspect
@Component
public class GuiGuLoginAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @SneakyThrows
    @Around("execution(* com.atguigu.tingshu.*.api.*.*(..)) && @annotation(guiGuLogin)")
    public Object loginAspect(ProceedingJoinPoint point, GuiGuLogin guiGuLogin) {
        //  获取请求对象
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //  转化为ServletRequestAttributes
        ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
        //  获取到HttpServletRequest
        HttpServletRequest request = sra.getRequest();
        String token = request.getHeader("token");
        //  判断是否需要登录
        if (guiGuLogin.required()) {
            //  必须要登录，token 为空是抛出异常
            if (StringUtils.isEmpty(token)) {
                //  没有token 要抛出异常
                throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
            }
            //  如果token 不为空，从缓存中获取信息.
            UserInfo userInfo = (UserInfo) this.redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
            //  判断对象是否为空
            if (null == userInfo) {
                //  抛出异常信息
                throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
            }
        }
        //  不需要强制登录,但是，有可能需要用信息.
        if (!StringUtils.isEmpty(token)) {
            //  如果token 不为空，从缓存中获取信息.
            UserInfo userInfo = (UserInfo) this.redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
            if (null != userInfo) {
                //  将用户信息存储到请求头中
                AuthContextHolder.setUserId(userInfo.getId());
                AuthContextHolder.setUsername(userInfo.getNickname());
            }
        }
        //  执行业务逻辑
        return point.proceed();
    }
}
