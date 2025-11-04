package com.atguigu.tingshu.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yjz
 * @Date 2025/11/4 11:06
 * @Description
 *
 */
@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        //  自定义线程池-不能使用工具类创建线程池. 阻塞队列或最大线程个数是21亿，这样会导致OOM!
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                8,    //  核心线程数 IO:2n  cpu:n+1
                16,   //  最大线程数
                3,  //  空闲线程存活时间
                TimeUnit.SECONDS, // 空闲线程存活时间单位
                new ArrayBlockingQueue<>(10) // 阻塞队列
        );
        return threadPoolExecutor;
    }
}
