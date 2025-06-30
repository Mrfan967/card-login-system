package com.cardlogin.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiter {

    private static final String RATE_LIMIT_KEY = "rate:limit:";
    private static final int MAX_REQUESTS = 10; // 最大请求次数
    private static final int TIME_WINDOW = 60; // 时间窗口（秒）

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    // 本地缓存作为 Redis 的备用方案
    private LoadingCache<String, AtomicInteger> localCache;

    @PostConstruct
    public void init() {
        localCache = CacheBuilder.newBuilder()
                .expireAfterWrite(TIME_WINDOW, TimeUnit.SECONDS)
                .build(new CacheLoader<String, AtomicInteger>() {
                    @Override
                    public AtomicInteger load(String key) {
                        return new AtomicInteger(0);
                    }
                });
    }

    public boolean tryAcquire(String clientId) {
        // 如果 Redis 可用，使用 Redis
        if (redisTemplate != null) {
            try {
                String key = RATE_LIMIT_KEY + clientId;

                // 获取当前请求次数
                Long currentRequests = redisTemplate.opsForValue().increment(key, 1);

                // 如果是第一次请求，设置过期时间
                if (currentRequests != null && currentRequests.equals(1L)) {
                    redisTemplate.expire(key, TIME_WINDOW, TimeUnit.SECONDS);
                }

                // 如果超过最大请求次数，返回false
                return currentRequests != null && currentRequests <= MAX_REQUESTS;
            } catch (Exception e) {
                System.out.println("Redis 限流失败，使用本地缓存: " + e.getMessage());
            }
        }

        // Redis 不可用时，使用本地缓存
        try {
            AtomicInteger count = localCache.get(clientId);
            return count.incrementAndGet() <= MAX_REQUESTS;
        } catch (ExecutionException e) {
            return true; // 如果出错，允许请求通过
        }
    }
}