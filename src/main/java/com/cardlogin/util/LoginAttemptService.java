package com.cardlogin.util;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPT = 5;
    private static final int LOCK_TIME_MINUTES = 30;
    
    private LoadingCache<String, Integer> attemptsCache;
    private Map<String, Long> lockTimeMap = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(LOCK_TIME_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }
    
    /**
     * 记录登录失败
     */
    public void loginFailed(String key) {
        int attempts;
        try {
            attempts = attemptsCache.get(key);
            attempts++;
            attemptsCache.put(key, attempts);
            
            if (attempts >= MAX_ATTEMPT) {
                lockAccount(key);
            }
        } catch (ExecutionException e) {
            attempts = 0;
        }
    }
    
    /**
     * 锁定账号
     */
    private void lockAccount(String key) {
        lockTimeMap.put(key, System.currentTimeMillis() + (LOCK_TIME_MINUTES * 60 * 1000));
    }
    
    /**
     * 检查账号是否被锁定
     */
    public boolean isLocked(String key) {
        Long lockTime = lockTimeMap.get(key);
        return lockTime != null && System.currentTimeMillis() < lockTime;
    }
    
    /**
     * 获取账号剩余锁定时间（分钟）
     */
    public long getRemainingLockTime(String key) {
        Long lockTime = lockTimeMap.get(key);
        if (lockTime == null) {
            return 0;
        }
        
        long remainingTimeMs = lockTime - System.currentTimeMillis();
        return remainingTimeMs > 0 ? remainingTimeMs / (60 * 1000) + 1 : 0;
    }
    
    /**
     * 登录成功后重置尝试次数
     */
    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
        lockTimeMap.remove(key);
    }
    
    /**
     * 获取剩余尝试次数
     */
    public int getRemainingAttempts(String key) {
        try {
            int attempts = attemptsCache.get(key);
            return Math.max(MAX_ATTEMPT - attempts, 0);
        } catch (ExecutionException e) {
            return MAX_ATTEMPT;
        }
    }
} 