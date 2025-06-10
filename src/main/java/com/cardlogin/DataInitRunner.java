package com.cardlogin;

import com.cardlogin.model.CardInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitRunner implements CommandLineRunner {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(String... args) {
        for (int i = 1; i <= 10; i++) {
            String cardNumber = String.format("18084511856197632%02d", i); // 18位卡号
            String username = "user" + i;
            String password = "pass" + i;
            CardInfo card = new CardInfo();
            card.setCardNumber(cardNumber);
            card.setUsername(username);
            card.setPassword(password);
            card.setExpiryDate(LocalDateTime.now().plusYears(1));
            card.setBound(false);
            card.setBoundDeviceFingerprint(null);
            card.setLastLoginTime(null);
            card.setLoginAttempts(0);
            redisTemplate.opsForValue().set("card:info:" + cardNumber, card);
        }
        System.out.println("批量测试卡信息已写入Redis");
    }
} 