package com.atguigu.tingshu.common.login;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yjz
 * @Date 2025/10/29 19:19
 * @Description
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GuiGuLogin {
    /**
     * 是否必须要登录
     *
     * @return
     */
    boolean required() default true;
}
