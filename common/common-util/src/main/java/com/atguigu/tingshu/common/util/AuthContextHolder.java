package com.atguigu.tingshu.common.util;

/**
 * 获取当前用户信息帮助类
 *
 * @author yjz
 */
public class AuthContextHolder {

    private static ThreadLocal<Long> userId = new ThreadLocal<>();
    private static ThreadLocal<String> username = new ThreadLocal<>();

    public static Long getUserId() {
        return userId.get();
    }

    public static void setUserId(Long _userId) {
        userId.set(_userId);
    }

    public static void removeUserId() {
        userId.remove();
    }

    public static String getUsername() {
        return username.get();
    }

    public static void setUsername(String _username) {
        username.set(_username);
    }

    public static void removeUsername() {
        username.remove();
    }

}
