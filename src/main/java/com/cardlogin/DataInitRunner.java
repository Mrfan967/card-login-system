package com.cardlogin;

import com.cardlogin.model.CardInfo;
import com.cardlogin.mapper.CardInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitRunner implements CommandLineRunner {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private CardInfoMapper cardInfoMapper;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        System.out.println("开始初始化测试卡数据...");
        
        for (int i = 1; i <= 10; i++) {
            String cardNumber = String.format("18084511856197632%01d", i); // 19位卡号
            String username = "user" + i;
            String password = "password123";
            
            CardInfo card = new CardInfo();
            card.setCardNumber(cardNumber);
            card.setUsername(username);
            card.setPassword(passwordEncoder.encode(password)); // 加密存储密码
            card.setExpiryDate(LocalDateTime.now().plusYears(1));
            card.setBound(false);
            card.setBoundDeviceFingerprint(null);
            card.setLastLoginTime(null);
            card.setLoginAttempts(0);
            
            // 保存到MySQL数据库
            try {
                cardInfoMapper.insert(card);
                System.out.println("已将卡 " + cardNumber + " 保存到MySQL数据库");
            } catch (Exception e) {
                System.out.println("保存卡 " + cardNumber + " 到MySQL失败: " + e.getMessage());
            }
            
            // 保存到Redis（如果可用）
            if (redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set("card:info:" + cardNumber, card);
                    System.out.println("已将卡 " + cardNumber + " 保存到Redis");
                } catch (Exception e) {
                    System.out.println("保存卡 " + cardNumber + " 到Redis失败: " + e.getMessage());
                }
            }
        }
        
        System.out.println("测试卡数据初始化完成！");
    }
} 