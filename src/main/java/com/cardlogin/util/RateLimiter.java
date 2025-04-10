package com.cardlogin.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {
    
    private static final String RATE_LIMIT_KEY = "rate:limit:";
    private static final int MAX_REQUESTS = 10; // 最大请求次数
    private static final int TIME_WINDOW = 60; // 时间窗口（秒）

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public boolean tryAcquire(String clientId) {
        String key = RATE_LIMIT_KEY + clientId;
        
        // 获取当前请求次数
        Long currentRequests = redisTemplate.opsForValue().increment(key, 1);
        
        // 如果是第一次请求，设置过期时间
        if (currentRequests != null && currentRequests.equals(1L)) {
            redisTemplate.expire(key, TIME_WINDOW, TimeUnit.SECONDS);
        }
        
        // 如果超过最大请求次数，返回false
        return currentRequests != null && currentRequests <= MAX_REQUESTS;
    }
} 