package com.cardlogin.service.impl;

import com.cardlogin.model.ApiResponse;
import com.cardlogin.model.CardInfo;
import com.cardlogin.model.LoginRequest;
import com.cardlogin.service.LoginService;
import com.cardlogin.util.LoginAttemptService;
import com.cardlogin.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    private static final String DEVICE_BINDING_KEY = "device:binding:";
    private static final String CARD_INFO_KEY = "card:info:";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 5;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public ApiResponse login(LoginRequest loginRequest, String deviceFingerprint) {
        String cardNumber = loginRequest.getCardNumber();
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
        // 检查账号是否被锁定
        if (loginAttemptService.isLocked(cardNumber)) {
            long remainingLockTime = loginAttemptService.getRemainingLockTime(cardNumber);
            return new ApiResponse(false, 
                String.format("账号已被锁定，请等待%d分钟后重试", remainingLockTime));
        }

        // 验证卡号是否存在并获取卡片信息
        CardInfo cardInfo = getCardInfo(cardNumber);
        if (cardInfo == null || !isValidCard(cardNumber)) {
            loginAttemptService.loginFailed(cardNumber);
            return new ApiResponse(false, "卡号不存在或已失效");
        }

        // 检查卡片是否过期
        if (cardInfo.getExpiryDate() != null && cardInfo.getExpiryDate().isBefore(LocalDateTime.now())) {
            loginAttemptService.loginFailed(cardNumber);
            return new ApiResponse(false, "卡片已过期");
        }

        // 检查设备绑定
        if (cardInfo.isBound() && !deviceFingerprint.equals(cardInfo.getBoundDeviceFingerprint())) {
            loginAttemptService.loginFailed(cardNumber);
            return new ApiResponse(false, "该卡已绑定其他设备，请先解绑");
        }

        // 校验用户名和密码
        if (username == null || password == null ||
            !username.equals(cardInfo.getUsername()) ||
            !password.equals(cardInfo.getPassword())) {
            loginAttemptService.loginFailed(cardNumber);
            return new ApiResponse(false, "用户名或密码错误");
        }

        // 更新卡片信息
        cardInfo.setBound(true);
        cardInfo.setBoundDeviceFingerprint(deviceFingerprint);
        cardInfo.setLastLoginTime(LocalDateTime.now());
        cardInfo.setLoginAttempts(0);
        
        // 保存更新后的卡片信息
        saveCardInfo(cardInfo);

        // 登录成功，重置失败计数
        loginAttemptService.loginSucceeded(cardNumber);

        // 生成JWT token
        String token = JwtUtil.generateToken(username, cardNumber);

        return new ApiResponse(true, "登录成功", token);
    }

    @Override
    public ApiResponse unbind(String cardNumber) {
        // 获取卡片信息
        CardInfo cardInfo = getCardInfo(cardNumber);
        if (cardInfo == null || !isValidCard(cardNumber)) {
            return new ApiResponse(false, "卡号不存在或已失效");
        }

        // 检查是否已绑定
        if (!cardInfo.isBound()) {
            return new ApiResponse(false, "该卡未绑定任何设备");
        }

        // 解除绑定
        cardInfo.setBound(false);
        cardInfo.setBoundDeviceFingerprint(null);
        saveCardInfo(cardInfo);

        return new ApiResponse(true, "解绑成功");
    }

    @Override
    public ApiResponse query(String cardNumber) {
        // 获取卡片信息
        CardInfo cardInfo = getCardInfo(cardNumber);
        if (cardInfo == null || !isValidCard(cardNumber)) {
            return new ApiResponse(false, "卡号不存在或已失效");
        }

        return new ApiResponse(true, "查询成功", cardInfo);
    }

    // 验证卡号是否有效
    private boolean isValidCard(String cardNumber) {
        return cardNumber != null && cardNumber.matches("^\\d{18,19}$");
    }

    // 获取卡片信息
    private CardInfo getCardInfo(String cardNumber) {
        String key = CARD_INFO_KEY + cardNumber;
        return (CardInfo) redisTemplate.opsForValue().get(key);
    }

    // 保存卡片信息
    private void saveCardInfo(CardInfo cardInfo) {
        String key = CARD_INFO_KEY + cardInfo.getCardNumber();
        redisTemplate.opsForValue().set(key, cardInfo);
    }
} 