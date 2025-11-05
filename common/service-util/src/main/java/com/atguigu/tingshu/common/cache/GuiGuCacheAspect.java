package com.atguigu.tingshu.common.cache;

import com.atguigu.tingshu.common.constant.RedisConstant;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author yjz
 * @Date 2025/11/5 10:16
 * @Description
 *
 */
@Aspect
@Component
public class GuiGuCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @SneakyThrows
    @Around("@annotation(com.atguigu.tingshu.common.cache.GuiGuCache)")
    public Object cacheAspect(ProceedingJoinPoint point) {
        Object obj = null;
        //  获取参数列表
        Object[] args = point.getArgs();
        //  获取方法上的主键
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        GuiGuCache guiGuCache = methodSignature.getMethod().getAnnotation(GuiGuCache.class);
        //  获取主键前缀
        String prefix = guiGuCache.prefix();
        //  组成缓存的key
        String key = prefix + Arrays.asList(args).toString();
        try {
            //  获取缓存数据
            obj = this.redisTemplate.opsForValue().get(key);
            if (obj == null) {
                //  声明分布式锁key
                RLock lock = redissonClient.getLock(key + ":lock");
                boolean result = lock.tryLock(RedisConstant.CACHE_LOCK_EXPIRE_PX1, RedisConstant.CACHE_LOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (result) {
                    try {
                        //  执行带有注解的方法体
                        obj = point.proceed(args);
                        if (null == obj) {
                            // 并把结果放入缓存
                            Object o = new Object();
                            this.redisTemplate.opsForValue().set(key, o, RedisConstant.CACHE_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return o;
                        }
                        // 并把结果放入缓存
                        this.redisTemplate.opsForValue().set(key, obj, RedisConstant.CACHE_TIMEOUT, TimeUnit.SECONDS);
                        return obj;
                    } finally {
                        //  释放资源
                        lock.unlock();
                    }
                } else {
                    //  没有获取到锁的用户自旋
                    return cacheAspect(point);
                }
            } else {
                return obj;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //  如果缓存实现了异常，暂时从数据库获取数据
        return point.proceed(args);
    }
}
