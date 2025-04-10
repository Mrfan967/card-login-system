package com.cardlogin.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    
    private static final String LOGIN_ATTEMPT_KEY = "login:attempt:";
    private static final String LOGIN_LOCK_KEY = "login:lock:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 5;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void loginSucceeded(String cardNumber) {
        String attemptKey = LOGIN_ATTEMPT_KEY + cardNumber;
        String lockKey = LOGIN_LOCK_KEY + cardNumber;
        redisTemplate.delete(attemptKey);
        redisTemplate.delete(lockKey);
    }

    public void loginFailed(String cardNumber) {
        String attemptKey = LOGIN_ATTEMPT_KEY + cardNumber;
        String lockKey = LOGIN_LOCK_KEY + cardNumber;

        // 增加失败次数
        long attempts = redisTemplate.opsForValue().increment(attemptKey, 1);
        
        // 设置失败次数的过期时间
        if (attempts == 1) {
            redisTemplate.expire(attemptKey, 1, TimeUnit.HOURS);
        }

        // 如果达到最大失败次数，设置锁定
        if (attempts >= MAX_ATTEMPTS) {
            redisTemplate.opsForValue().set(lockKey, true);
            redisTemplate.expire(lockKey, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
    }

    public boolean isLocked(String cardNumber) {
        String lockKey = LOGIN_LOCK_KEY + cardNumber;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    public long getRemainingLockTime(String cardNumber) {
        String lockKey = LOGIN_LOCK_KEY + cardNumber;
        Long expire = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
        return expire != null ? expire : 0;
    }

    public long getFailedAttempts(String cardNumber) {
        String attemptKey = LOGIN_ATTEMPT_KEY + cardNumber;
        Object attempts = redisTemplate.opsForValue().get(attemptKey);
        return attempts != null ? Long.parseLong(attempts.toString()) : 0;
    }
} 