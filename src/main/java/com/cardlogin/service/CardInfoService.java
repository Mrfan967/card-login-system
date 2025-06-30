package com.cardlogin.service;

import com.cardlogin.mapper.CardInfoMapper;
import com.cardlogin.model.CardInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class CardInfoService {
    @Autowired
    private CardInfoMapper cardInfoMapper;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String CARD_INFO_KEY = "card:info:";

    // 注册
    public boolean register(CardInfo cardInfo) {
        if (cardInfoMapper.selectByCardNumber(cardInfo.getCardNumber()) != null) {
            throw new RuntimeException("卡号已存在");
        }
        cardInfo.setPassword(passwordEncoder.encode(cardInfo.getPassword()));
        boolean result = cardInfoMapper.insert(cardInfo) > 0;
        
        // 同步保存到Redis
        if (result && redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(CARD_INFO_KEY + cardInfo.getCardNumber(), cardInfo);
            } catch (Exception e) {
                System.out.println("保存到 Redis 失败: " + e.getMessage());
            }
        }
        
        return result;
    }

    // 登录
    public CardInfo login(String cardNumber, String password) {
        CardInfo card = getByCardNumber(cardNumber);
        if (card == null) {
            throw new RuntimeException("卡号不存在");
        }
        if (!passwordEncoder.matches(password, card.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return card;
    }

    // 解绑
    public boolean unbind(String cardNumber) {
        CardInfo card = getByCardNumber(cardNumber);
        if (card == null) {
            throw new RuntimeException("卡号不存在");
        }
        card.setBound(false);
        card.setBoundDeviceFingerprint(null);
        return update(card);
    }
    
    // 根据卡号查询卡信息
    public CardInfo getByCardNumber(String cardNumber) {
        CardInfo card = null;

        // 先从Redis中查询（如果可用）
        if (redisTemplate != null) {
            try {
                card = (CardInfo) redisTemplate.opsForValue().get(CARD_INFO_KEY + cardNumber);
            } catch (Exception e) {
                System.out.println("从 Redis 查询失败: " + e.getMessage());
            }
        }

        // 如果Redis中没有，则从数据库查询
        if (card == null) {
            card = cardInfoMapper.selectByCardNumber(cardNumber);

            // 如果数据库中存在，则同步到Redis（如果可用）
            if (card != null && redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set(CARD_INFO_KEY + cardNumber, card);
                } catch (Exception e) {
                    System.out.println("同步到 Redis 失败: " + e.getMessage());
                }
            }
        }

        return card;
    }
    
    // 更新卡信息
    public boolean update(CardInfo cardInfo) {
        boolean result = cardInfoMapper.update(cardInfo) > 0;

        // 同步更新Redis（如果可用）
        if (result && redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(CARD_INFO_KEY + cardInfo.getCardNumber(), cardInfo);
            } catch (Exception e) {
                System.out.println("更新 Redis 失败: " + e.getMessage());
            }
        }

        return result;
    }
    
    // 获取所有卡
    public List<CardInfo> getAllCards() {
        return cardInfoMapper.selectAll();
    }
} 